package org.apache.maven.mercury.artifact.api;

/**
 * provides a way to configure an object instance, if that object supports the idea 
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface Configurable
{

  /**
   * configure this instance
   * 
   * @param name of the configurable property
   * @param val configuration value
   */
  public void setOption( String name, String val );
}
