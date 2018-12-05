package utils

import anorm.{Column, MetaDataItem, ToStatement, TypeDoesNotMatch}
import org.postgresql.util.PGobject
import play.api.libs.json.{JsValue, Json}

object AnormTypesInstances {
  implicit val jsonToStatement = new ToStatement[JsValue] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: JsValue): Unit = {
      val pgo = new PGobject()
      pgo.setType("json")
      pgo.setValue(aValue.toString)

      s.setObject(index, pgo)
    }
  }

  implicit def rowToJsValue: Column[JsValue] = Column.nonNull1 { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case o: PGobject if o.getType() == "json" => Right(Json.parse(o.getValue()))
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to json for column " + qualified))
    }
  }
}
