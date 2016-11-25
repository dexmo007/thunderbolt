package controllers

import java.util.ServiceLoader

import de.hd.jcg.generators.Generator

import scala.collection._
import scala.collection.JavaConverters._
import scala.collection.breakOut

/**
  * Created by henri on 11/20/2016.
  */
object JcgUtil {
  val generators: Map[String, Generator] = ServiceLoader.load(classOf[Generator]).asScala.map(gen => (gen.highlightClass, gen))(breakOut)
}
