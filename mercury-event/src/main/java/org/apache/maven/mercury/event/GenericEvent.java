package org.apache.maven.mercury.event;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class GenericEvent
extends AbstractMercuryEvent
{

  public GenericEvent( EventTypeEnum type, String name )
  {
    super(type, name);
  }

  public GenericEvent( EventTypeEnum type, String name, String tag )
  {
    super( type, name, tag );
  }

  public void setTag( String tag )
  {
    this.tag = tag;
  }

}
