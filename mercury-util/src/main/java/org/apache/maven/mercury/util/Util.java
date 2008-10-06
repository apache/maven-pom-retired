package org.apache.maven.mercury.util;

import java.util.Collection;

/**
 * general utility helpers
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class Util
{
   @SuppressWarnings("unchecked")
  public boolean isEmpty( Collection o )
   {
     return o == null || o.isEmpty();
   }
   
   public boolean isEmpty( String o )
   {
     return o == null || o.length() < 1;
   }

   public boolean isEmpty( Object [] o )
   {
     return o == null || o.length < 1;
   }

   public boolean isEmpty( Object o )
   {
     return o == null;
   }
}
