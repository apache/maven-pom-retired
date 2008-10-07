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
  public static boolean isEmpty( Collection o )
   {
     return o == null || o.isEmpty();
   }
   
   public static boolean isEmpty( String o )
   {
     return o == null || o.length() < 1;
   }

   public static boolean isEmpty( Object [] o )
   {
     return o == null || o.length < 1;
   }

   public static boolean isEmpty( Object o )
   {
     return o == null;
   }
}
