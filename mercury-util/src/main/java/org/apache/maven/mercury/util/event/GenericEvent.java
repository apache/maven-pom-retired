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

  public GenericEvent( String type )
  {
    super(type);
  }

  public GenericEvent( String type, String tag )
  {
    super( type, tag );
  }

  public void setTag( String tag )
  {
    this.tag = tag;
  }

}
