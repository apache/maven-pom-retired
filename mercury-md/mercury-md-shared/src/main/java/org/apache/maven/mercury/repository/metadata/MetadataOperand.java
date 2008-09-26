package org.apache.maven.mercury.repository.metadata;

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MetadataOperand
    extends AbstractOperand
{
  private static final Language lang = new DefaultLanguage( MetadataOperand.class );
  Metadata metadata;
  
  public MetadataOperand( Metadata data )
  {
    if( data == null  )
      this.metadata = new Metadata();
    else
      this.metadata = data;
  }
  
  public Metadata getOperand()
  {
    return metadata;
  }
}
