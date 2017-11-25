package models

import anorm._
import org.postgresql.util.PGobject

import play.api.libs.json._

import play.api.data._
import play.api.data.format.Formatter

object enums {

  trait EnumAdt[A] {
    def values: List[A]
    def valueAsString(x: A): String
    def parseValue(x: String): Option[A] = {
      values.find(valueAsString(_) == x)
    }
  }

  def values[A](implicit ev: EnumAdt[A]): List[A] = ev.values

  def jsonWrites[A](implicit ev: EnumAdt[A]): Writes[A] = new Writes[A] {
    def writes(v: A) = {
      JsString(ev.valueAsString(v))
    }
  }

  def jsonReads[A](implicit ev: EnumAdt[A]): Reads[A] = new Reads[A] {
    def reads(js: JsValue) = js match {
      case JsString(s) => ev.parseValue(s).map(JsSuccess(_)).getOrElse(JsError("no parse"))
      case _ => JsError("no parse")
    }
  }

  def toStatement[A](implicit ev: EnumAdt[A]): ToStatement[A] = new ToStatement[A] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: A): Unit = s.setObject(index, ev.valueAsString(aValue))
  }

  def fromColumn[A](implicit ev: EnumAdt[A]): Column[A] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    val error = TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " for column " + qualified)
    value match {
      case s: PGobject =>
        ev.parseValue(s.getValue).toRight(error)
      case s: String => 
        ev.parseValue(s).toRight(error)
      case _ => Left(error)
    }
  }

  def enumFormat[A](implicit ev: EnumAdt[A]): Formatter[A] = new Formatter[A] {
    def bind(key: String, data: Map[String, String]) =
      data
        .get(key)
        .flatMap(x => ev.parseValue(x))
        .toRight(Seq(FormError(key, "error.invalid", Nil)))
    def unbind(key: String, value: A) = Map(key -> ev.valueAsString(value))
  }
}
