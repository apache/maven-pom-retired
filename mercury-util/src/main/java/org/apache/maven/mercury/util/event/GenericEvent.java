package org.apache.maven.mercury.util.event;

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

  public GenericEvent( MercuryEvent.EventTypeEnum type, String name )
  {
    super(type, name);
  }

  public GenericEvent( MercuryEvent.EventTypeEnum type, String name, String tag )
  {
    super( type, name, tag );
  }

  public void setTag( String tag )
  {
    this.tag = tag;
  }

}
