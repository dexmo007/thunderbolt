import de.hd.jcg.generators.JavaGenerator
import play.api.libs.json.Json

/**
  * Created by henri on 11/21/2016.
  */
object Test {

  val complex =
    """{
      |"name":"dexter",
      |"friends":["Harry","Floid"],
      |"age":23,
      |"single":true,
      |"car":{"brand":"vw","year":2014}
      |}""".stripMargin

  private def extractName(clazz: String): String = {
    val start = clazz.indexOf("public class") + 12
    val end = clazz.indexOf("{", start)
    clazz.substring(start, end).trim
  }

  def main(args: Array[String]): Unit = {
    for (clazz <- new JavaGenerator().generateClasses(complex)) {
      println(extractName(clazz))
    }
  }

}
