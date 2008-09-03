package org.apache.maven.mercury.artifact.version;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class AttributeQuery
{
  public static final char EXRP_START = '{';
  public static final char EXRP_STOP  = '}';
  
  private String expr;
  
  public AttributeQuery( String query )
  {
    if( query == null || query.indexOf( EXRP_START ) == -1 )
      return;
    int from = query.indexOf( EXRP_START );
    int to   = query.indexOf( EXRP_STOP );
  }
  
  public static String stripExpression( String query )
  {
    if( query == null || query.indexOf( EXRP_START ) == -1 )
      return query;
    
    int from = query.indexOf( EXRP_START );
    if( from == 0 )
      return null;

    return query.substring( 0, from );
  }
}
