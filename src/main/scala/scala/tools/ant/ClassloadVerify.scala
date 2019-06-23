/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.tools.ant

import org.apache.tools.ant.Project
import org.apache.tools.ant.types.{Path, Reference}
import scala.jdk.CollectionConverters._
import scala.tools.util.VerifyClass

class ClassloadVerify extends ScalaMatchingTask {

  /** The class path to use for this compilation. */
  protected var classpath: Option[Path] = None

  /** Sets the `classpath` attribute. Used by [[http://ant.apache.org Ant]].
   *  @param input The value of `classpath`. */
  def setClasspath(input: Path): Unit = {
    classpath = Some(input)
  }

  def setClasspathref(input: Reference): Unit = {
    val p = new Path(getProject)
    p.setRefid(input)
    classpath = Some(p)
  }

  private def getClasspath: Array[String] = classpath match {
    case None     => buildError("Member 'classpath' is empty.")
    case Some(x)  => x.list
  }

  override def execute(): Unit = {
    val results = VerifyClass.run(getClasspath).asScala
    results foreach (r => log("Checking: " + r, Project.MSG_DEBUG))
    val errors = for((name, error) <- results; if error != null) yield (name,error)
    if(errors.isEmpty) {
      // TODO - Log success
      log("Classload verification succeeded with " + results.size + " classes.", Project.MSG_INFO)
    } else {
      for((name, error) <- errors) {
         log(s"$name failed verification with: $error", Project.MSG_ERR)
      }
      buildError(s"${errors.size} classload verification errors on ${results.size} classes.")
    }
  }

}
