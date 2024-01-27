import utest.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*

@js.native
@JSImport("lodash", JSImport.Default)
object Lodash extends js.Object:
	def merge(obj1: js.Object, obj2: js.Object): js.Object = js.native

object TrivialTest extends TestSuite:
	val tests = Tests:
		val emptyMergedObj = Lodash.merge(js.Object(), js.Object())
		println(emptyMergedObj)
		assert(true)