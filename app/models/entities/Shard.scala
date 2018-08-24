package models.entities

import java.util.UUID

case class Shard (
  start: UUID,
  end: UUID
) {
  def znode = start.toString + "_" + end.toString
  def contains(x: UUID): Boolean = {
    this.start.getMostSignificantBits <= x.getMostSignificantBits &&
    this.end.getMostSignificantBits >= x.getMostSignificantBits
  }
}
object Shard {
  def fromZpath(path: String): Option[Shard] = {
    path.split("/").lastOption
      .map(x => (x.split("_").headOption, x.split("_").lastOption))
      .flatMap({ x =>
        val (start, end) = x
        for {
          s <- start
          e <- end
        } yield Shard(UUID.fromString(s), UUID.fromString(e))
      })
  }

  private def computeShard(i: Int, total: Int): Shard = {
    val half = total / 2
    if(i < half) {
      Shard(
        new UUID(Long.MaxValue / half * i + i, 0),
        new UUID(Long.MaxValue / half * (i + 1) + i, -1)
      )
    } else {
      Shard(
        new UUID(Long.MinValue  - (Long.MinValue / half * (i - half)), 0),
        new UUID(Long.MinValue  - (Long.MinValue / half * (i + 1 - half) + 1), -1),
      )
    }
  }

  def isPowerOfTwo(x: Int) = x > 0 && (x & (x - 1)) == 0

  def generateShards(number: Int): List[Shard] = {
    if(isPowerOfTwo(number)) {
      (0 to (number - 1)).toList.map(i => computeShard(i, number))
    } else {
      throw new Exception("Number must be a power of 2.")
    }
  }
}