package simple

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@js.native
trait ImportedType extends js.Object:
  def hello: String = js.native

@js.native
@JSImport("/module/dependency", "someValue")
object ImportObject extends ImportedType

object Main {
  def main(args: Array[String]): Unit = {
    println(s"Hello: ${ImportObject.hello}")
  }
}
