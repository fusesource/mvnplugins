package org.scala_tools.maven.plexus

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.factory.AbstractComponentFactory;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * This class is used to instantiate new components in plexus
 */
class ScalaComponentFactory extends AbstractComponentFactory {
  
  //Override "newInstance" method.  Note: AnyRef = java.lang.Object
  override def newInstance(componentDescriptor : ComponentDescriptor, 
                           classRealm : ClassRealm, 
                           container : PlexusContainer ) : AnyRef = {
    //Helper method to load  a scala class from our "realm"
    def loadScalaClass(className : String) = classRealm.loadClass(className)
    //Helper method to create a new scala object using the discovered constructor
    def createScalaObject(constructor : java.lang.reflect.Constructor[_]) = constructor.newInstance().asInstanceOf[AnyRef]
    def createScalaObjectWithArgs(constructor : java.lang.reflect.Constructor[_], args : Array[Object]) = {
      constructor.newInstance(args : _*).asInstanceOf[AnyRef]
    }
    /** this will load the desired argument of the desired type for a constructor*/
    def loadArgumentValue(config : PlexusConfiguration, desiredType : Class[_]) : Object = {
      //TODO - Implement this method...
      
      null
    }
    
    try {
      componentDescriptor.setComponentConfigurator( "scala" );
      
      val constructorPrefix = "$$constructor$"
      //TODO - See if we need to inject into the constructor somehow
      var clazz = loadScalaClass(componentDescriptor.getImplementation)
      
      //TODO - Ignore this constructor krap until we fix the mojo desc extractor
      val constructorArgs : Array[PlexusConfiguration] = if(componentDescriptor.getConfiguration != null) {
	       (for {        
	        child <- componentDescriptor.getConfiguration.getChildren
	        if child.getName.startsWith(constructorPrefix)
	        order = child.getName.substring(constructorPrefix.length)
	      } yield (child, order)).toList.sortWith(_._2 < _._2).map(_._1).toArray
      } else {
        Array()
      }
      //THis will bomb if we don't have an appropriate constructor
      val constructor = clazz.getConstructors.filter( _.getParameterTypes.length == constructorArgs.size).head      
      val constructorArgVals = for {
        (argConfig, argType) <- constructorArgs.zip(constructor.getParameterTypes)        
      } yield {
        loadArgumentValue(argConfig, argType)
      }
      
      if(constructorArgVals.length > 0) {
        createScalaObjectWithArgs(constructor, constructorArgVals)
      } else {
        createScalaObject(constructor)
      }
    } catch {
      case t : Throwable =>
        throw new ComponentConfigurationException("Problem creating new scala component", t);
    }
  }
  
  
}
