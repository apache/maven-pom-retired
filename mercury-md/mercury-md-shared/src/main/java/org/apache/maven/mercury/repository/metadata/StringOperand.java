package org.apache.maven.mercury.repository.metadata;

import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

/**
 * String storage
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class StringOperand
    extends AbstractOperand
{
  private static final Language lang = new DefaultLanguage( StringOperand.class );
  String str;
  
  public StringOperand( String data )
  {
    if( data == null || data.length() < 1 )
      throw new IllegalArgumentException( lang.getMessage( "bad.string.data", data ) );
    this.str = data;
  }
  
  public String getOperand()
  {
    return str;
  }
}
