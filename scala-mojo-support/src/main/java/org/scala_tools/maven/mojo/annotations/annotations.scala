package org.scala_tools.maven.mojo.annotations

import scala.annotation.StaticAnnotation

sealed abstract class MavenAnnotation extends StaticAnnotation

object MavenAnnotation {

  object Companion {
    def unapply(ann: String) = try {
      Some(Class.forName(ann + "$").getField("MODULE$").get(null))
    } catch {
      case _ ⇒ None
    }
  }

  def apply(annotation: String): MavenAnnotation = {
    annotation match {
      case Companion(`parameter`) => parameter()
      case Companion(`required`) => required()
      case Companion(`readOnly`) => readOnly()
      case Companion(`executionStrategy`) => new executionStrategy()
      case Companion(`inheritByDefault`) => inheritByDefault(true)
      case Companion(`requiresOnline`) => requiresOnline(true)
      case Companion(`requiresProject`) => requiresProject(true)
      case Companion(`requiresReports`) => requiresReports(true)
      case Companion(`requiresDirectInvocation`) => requiresDirectInvocation(true)

      case _ => throw new UnsupportedOperationException("Annotation (%s) is not supported".format(annotation))
    }
  }

  def apply(annotation: String, value: String): MavenAnnotation = {
    annotation match {
      case Companion(`expression`) => expression(value)
      case Companion(`alias`) => alias(value)
      case Companion(`configurator`) => configurator(value)
      case Companion(`phase`) => phase(value)
      case Companion(`goal`) => goal(value)
      case Companion(`executeGoal`) => executeGoal(value)
      case Companion(`executePhase`) => executePhase(value)
      case Companion(`instantiationStrategy`) => instantiationStrategy(value)
      case Companion(`requiresDependencyResolution`) => requiresDependencyResolution(value)
      case Companion(`description`) => description(value)
      case Companion(`since`) => since(value)
      case Companion(`component`) => component(value, None)

      case _ => throw new UnsupportedOperationException("Annotation (%s) is not supported".format(annotation))
    }
  }

  def apply(annotation: String, value: Boolean): MavenAnnotation = {
    annotation match {
      case Companion(`inheritByDefault`) => inheritByDefault(value)
      case Companion(`requiresOnline`) => requiresOnline(value)
      case Companion(`requiresProject`) => requiresProject(value)
      case Companion(`requiresReports`) => requiresReports(value)
      case Companion(`requiresDirectInvocation`) => requiresDirectInvocation(value)

      case _ => throw new UnsupportedOperationException("Annotation (%s) is not supported".format(annotation))
    }
  }

  def apply(annotation: String, value1: String, value2: String): MavenAnnotation = {
    annotation match {
      case Companion(`executePhaseInLifecycle`) => executePhaseInLifecycle(value1, value2)

      case _ => throw new UnsupportedOperationException("Annotation (%s) is not supported".format(annotation))
    }
  }

  def apply(annotation: String, value1: String, value2: Option[String]): MavenAnnotation = {
    annotation match {
      case Companion(`component`) => component(value1, value2)

      case _ => throw new UnsupportedOperationException("Annotation (%s) is not supported".format(annotation))
    }
  }
}

/**
 * Annotations for variable and/or constructor arguments
 **/

/** Used to define a parameter from a var field */
case class parameter() extends MavenAnnotation

/** Used to mark a parameter as required to execute*/
case class required() extends MavenAnnotation

/** Used to define a parameter as readonly - Cannot be replaced by user in POM */
case class readOnly() extends MavenAnnotation

/** Expression to use when setting a parameter */
case class expression(value : String) extends MavenAnnotation

/** Alias to use for a parameter in the pom */
case class alias(value : String) extends MavenAnnotation

/** Injects a plexus component */
case class component(role : String, roleHint : Option[String]) extends MavenAnnotation

//TODO - Support default values!

/**
 * Annotations for Mojos
 **/
/**
 * (at)configurator <roleHint> The configurator type to use when injecting parameter values into this Mojo.
 *                          The value is normally deduced from the Mojo's implementation language,
 *                          but can be specified to allow a custom ComponentConfigurator implementation to be used.
 *
 * NOTE: This will only be used in very special cases, using a highly controlled vocabulary of possible values.
 * (Elements like this are why it's a good idea to use the descriptor tools.)
 **/
case class configurator(roleHint : String) extends MavenAnnotation

/** Binds this Mojo to a particular phase of the standard build lifecycle, if specified. NOTE: This is only required if this Mojo is to participate in the standard build process. */
case class phase(name : String) extends MavenAnnotation

/** Gives a name to the goal represented by the annotated Mojo */
case class goal(name : String) extends MavenAnnotation

// I split the executes into separate annotations for easier use in code
case class executionStrategy() extends MavenAnnotation
case class executeGoal(value : String) extends MavenAnnotation
case class executePhase(value : String) extends MavenAnnotation
case class executePhaseInLifecycle(phase : String, lifecycle : String) extends MavenAnnotation

case class instantiationStrategy(value : String) extends MavenAnnotation
case class requiresDependencyResolution(scope : String) extends MavenAnnotation
case class description(value : String) extends MavenAnnotation
case class since(value : String) extends MavenAnnotation

case class inheritByDefault(value : Boolean)	extends MavenAnnotation
case class requiresOnline(value : Boolean) extends MavenAnnotation
case class requiresProject(value : Boolean = true) extends MavenAnnotation
case class requiresReports(value : Boolean) extends MavenAnnotation
case class requiresDirectInvocation(value : Boolean) extends MavenAnnotation


