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
  public static final boolean isEmpty( Collection o )
   {
     return o == null || o.isEmpty();
   }
   
   public static final boolean isEmpty( String o )
   {
     return o == null || o.length() < 1;
   }

   public static final boolean isEmpty( Object [] o )
   {
     return o == null || o.length < 1;
   }

   public static final boolean isEmpty( Object o )
   {
     return o == null;
   }

   public static final String nvlS( String s, String dflt )
   {
     return isEmpty(s) ? dflt : s;
   }
}
