package models

import javax.inject._
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import play.api.Logger
import java.util.UUID
import java.net.InetSocketAddress
import com.loopfor.zookeeper._
import akka.actor.ActorRef
import models.entities.Shard
import models.StateAgent._

@Singleton
class LockManager @Inject() (
  conf: play.api.Configuration,
  @Named("globalstate") globalState: ActorRef,
  implicit val ec: ExecutionContext
) {
  val zookeeperConfiguration = Configuration {
    conf.get[String]("zookeeper.servers")
      .split(',').toSeq
      .map(x => x.split(':') match { case Array(host, port) => new InetSocketAddress(host, port.toInt) })
  } withTimeout {
    5 seconds
  }

  lazy val zookeeper = AsynchronousZookeeper(zookeeperConfiguration)
  lazy val watchedZk = zookeeper watch {
    case e: NodeEvent => handleNodeEvent(e, globalLocks)
  }
  val globalLocks = collection.mutable.MutableList[(Shard, String)]()
  val instanceId = conf.getOptional[String]("instanceid")
    .map(UUID.fromString)
    .getOrElse(UUID.randomUUID())

  if(conf.get[Boolean]("scheduler.enabled") && conf.get[Boolean]("sharding.enabled")) {
    Logger.info("Scheduler is enabled; sharding is enabled => starting lock manager")

    for {
      path <- createInstanceNode(zookeeper, instanceId)
      instances <- getInstancesList(zookeeper)
      myShards = getShards(instances, instanceId)
      locks <- setupLocks(zookeeper, myShards)
    } yield {
      globalLocks ++= locks
      locks foreach { l => getOrWatchLockedShard(zookeeper, watchedZk, l._1, l._2) }
      watchInstancesList(watchedZk)
    }
  }

  def handleNodeEvent(e: NodeEvent, locks: collection.mutable.MutableList[(Shard, String)]): Unit = {
    if(e.path.startsWith("/poke/scheduler/shards")) {
      locks.find(_._2.startsWith(e.path.split("/lock-").headOption.getOrElse(""))) map { lock =>
        getOrWatchLockedShard(zookeeper, watchedZk, lock._1, lock._2)
      }
    } else if(e.path.startsWith("/poke/scheduler/instances")) {
      for {
        instances <- getInstancesList(zookeeper)
        myShards = getShards(instances, instanceId)
        locks <- setupLocks(zookeeper, myShards)
      } yield {
        globalLocks foreach { case (shard, path) =>
          Logger.info("Removing lock " + path)
          zookeeper.delete(path, None)
        }
        globalLocks.clear
        globalLocks ++= locks
        locks foreach { l => getOrWatchLockedShard(zookeeper, watchedZk, l._1, l._2) }
        watchInstancesList(watchedZk)
      }
    } else {
      Logger.error(s"Unhandled zookeeper event on ${e.path}")
    }
  }

  def createInstanceNode(zk: AsynchronousZookeeper, instanceId: UUID): Future[String] = {
    val path = "/poke/scheduler/instances/" + instanceId.toString()
    Logger.info(s"Registering on $path")
    zk.create(
      path = path,
      data = Array.empty[Byte],
      acl = ACL.AnyoneRead,
      disp = Ephemeral
    )
  }

  def getInstancesList(zk: AsynchronousZookeeper): Future[Seq[String]] = {
    val fChildren: Future[(Seq[String], Status)] = zk.children("/poke/scheduler/instances")
    fChildren.map(_._1)
  }

  def watchInstancesList(zk: AsynchronousWatchableZookeeper): Future[Seq[String]] = {
    val fChildren: Future[(Seq[String], Status)] = zk.children("/poke/scheduler/instances")
    fChildren.map(_._1)
  }

  def getShards(instances: Seq[String], instanceId: UUID): List[Shard] = {
    val instanceNumber = instances.indexOf(instanceId.toString)
    shards.zipWithIndex.filter(_._2 % instances.length == instanceNumber).map(_._1)
  }

  def setupLocks(zk: AsynchronousZookeeper, shards: List[Shard]): Future[List[(Shard, String)]] = {
    Logger.info("Setting up locks")
    Future.sequence(shards map { s =>
      zk.create(
        path = "/poke/scheduler/shards/" + s.znode + "/lock-",
        data = Array.empty[Byte],
        acl = ACL.AnyoneRead,
        disp = EphemeralSequential
      )
        .map(p => Success((s, p)))
        .recover({ case e =>
          Logger.error(s"Could not create lock for shard ${s}. This is most likely because the zookeeper path does not exist. Run the prepare script and/or make sure you are using the correct number of shards.")
          Failure(e)
        })
    }).map(_.collect({ case Success(path) => path}))
  }

  def getLockedShard(zk: AsynchronousZookeeper, shard: Shard, path: String): Future[Either[List[String], Shard]] = {
    zk.children("/poke/scheduler/shards/" + shard.znode).map({ case (paths, status) =>
      if(paths.sorted.headOption == path.split("/").lastOption) {
        Logger.info(s"Lock acquired on $shard")
        Right(shard)
      } else Left(paths.toList.map(p => "/poke/scheduler/shards/" + shard.znode + "/" + p))
    })
  }

  def watchLockedShard(zk: AsynchronousWatchableZookeeper, shard: Shard, path: String): Future[Option[Status]] = {
    Logger.info("watching locked shard: " + path)
    zk.exists(path)
  }

  def getOrWatchLockedShard(zk: AsynchronousZookeeper, watchedZk: AsynchronousWatchableZookeeper, shard: Shard, path: String): Unit = {
    getLockedShard(zk, shard, path) map { _ match {
      case Right(shard) => globalState ! AddShard(shard)
      case Left(paths) => {
        val previousPath = paths.sorted.sliding(2, 1)
          .find(_ match {
            case List(_, path) => true
            case _ => false
          })
          .flatMap(_.headOption)
          .getOrElse(throw new Exception("Failed to find path with previous sequence number to watch"))
        watchLockedShard(watchedZk, shard, previousPath)
      }
    }}
    ()
  }

  lazy val shards = {
    val nbshards = conf.get[Int]("sharding.nbshards")
    Shard.generateShards(nbshards)
  }
}
