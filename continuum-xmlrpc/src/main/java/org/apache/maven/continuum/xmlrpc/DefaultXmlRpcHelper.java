package org.apache.maven.continuum.xmlrpc;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

/**
 * @plexus.component
 *   role="org.apache.maven.continuum.xmlrpc.XmlRpcHelper"
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultXmlRpcHelper
    extends AbstractLogEnabled
    implements XmlRpcHelper
{
    private final Set alwaysExcludedProperties = new HashSet( Arrays.asList( new String[] {
        "class",
    } ) );

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[ 0 ];

    // ----------------------------------------------------------------------
    // XmlRpcHelper Implementation
    // ----------------------------------------------------------------------

    public Hashtable objectToHashtable( Object object )
        throws IllegalAccessException, InvocationTargetException
    {
        return objectToHashtable( object, Collections.EMPTY_SET );
    }

    public Hashtable objectToHashtable( Object object, Set excludedProperties )
        throws IllegalAccessException, InvocationTargetException
    {
        return objectToHashtable( object, excludedProperties, new HashSet() );
    }

    public Vector collectionToVector( Collection value, boolean convertElements )
        throws IllegalAccessException, InvocationTargetException
    {
        return collectionToVector( value, convertElements, Collections.EMPTY_SET );
    }

    public Vector collectionToVector( Collection value, boolean convertElements, Set excludedProperties )
        throws IllegalAccessException, InvocationTargetException
    {
        return collectionToVector( value, convertElements, excludedProperties, new HashSet() );
    }

    public void hashtableToObject( Hashtable hashtable, Object target )
        throws IntrospectionException, IllegalAccessException, InvocationTargetException
    {
        for ( Iterator it = hashtable.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            String key = (String) entry.getKey();

            Object value = entry.getValue();

            if ( key == null || value == null )
            {
                continue;
            }

            // ----------------------------------------------------------------------
            // Convert the key to a setter
            // ----------------------------------------------------------------------

            String setterName = "set" +
                                Character.toUpperCase( key.charAt( 0 ) ) +
                                key.substring( 1 );

            Class clazz = target.getClass();

            // ----------------------------------------------------------------------
            //
            // ----------------------------------------------------------------------

            Method setter = getSetter( clazz, setterName, key );

            if ( setter == null )
            {
                continue;
            }

            // TODO: Implement to give better feedback
//            Class parameter = setter.getParameterTypes()[ 0 ];
//
//            if ( value.getClass().isAssignableFrom( parameter ) )
//            {
//            }

            setter.invoke( target, new Object[]{value} );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Hashtable objectToHashtable( Object object,
                                         Set excludedProperties,
                                         Set visitedObjects )
        throws IllegalAccessException, InvocationTargetException
    {
        if ( !visitedObjects.add( object ) )
        {
            return null;
        }

        Hashtable hashtable = new Hashtable();

        if ( object == null )
        {
            return hashtable;
        }

        Method[] methods = object.getClass().getMethods();

        for ( int i = 0; i < methods.length; i++ )
        {
            Method method = methods[ i ];

            String name = method.getName();

            // Only call getters
            if ( (!name.startsWith( "get" ) || name.length() <= 3 ) &&
                 (!name.startsWith( "is" ) || name.length() <= 2 ) )
            {
                continue;
            }

            // Only call methods without arguments
            if ( method.getParameterTypes().length != 0 )
            {
                continue;
            }

            // No use in calling methods that doesn't return anything
            if ( method.getReturnType().equals( Void.TYPE ) )
            {
                continue;
            }

            // ----------------------------------------------------------------------
            // Rewrite the name from the form 'getFoo' to 'foo'.
            // ----------------------------------------------------------------------

            String propertyName;

            if ( name.startsWith( "get" ) )
            {
                propertyName = name.substring( 3 );
            }
            else
            {
                propertyName = name.substring( 2 );
            }

            propertyName = StringUtils.uncapitalise( propertyName );

            if ( excludedProperties.contains( propertyName ) ||
                 alwaysExcludedProperties.contains( propertyName ) )
            {
                continue;
            }

            // ----------------------------------------------------------------------
            // Get the value
            // ----------------------------------------------------------------------

            Object value = method.invoke( object, EMPTY_OBJECT_ARRAY );

            // ----------------------------------------------------------------------
            // Convert the value to a String
            // ----------------------------------------------------------------------

            if ( value == null )
            {
                continue;
            }
            else if ( value instanceof String )
            {
                // nothing to do after all, the object is already a string!
            }
            else if ( value instanceof Number )
            {
                value = value.toString();
            }
            else if ( value instanceof Boolean )
            {
                value = value.toString();
            }
            else if ( value instanceof Collection )
            {
                value = collectionToVector( (Collection) value,
                                            true,
                                            excludedProperties,
                                            visitedObjects );
            }
            else
            {
                value = objectToHashtable( value,
                                           excludedProperties,
                                           visitedObjects );
            }

            // ----------------------------------------------------------------------
            //
            // ----------------------------------------------------------------------

            if ( value != null )
            {
                hashtable.put( propertyName, value );
            }
        }

        return hashtable;
    }

    private Vector collectionToVector( Collection value,
                                      boolean convertElements,
                                      Set excludedProperties,
                                      Set visitedObjects )
        throws IllegalAccessException, InvocationTargetException
    {
        if ( value instanceof Vector )
        {
            return (Vector) value;
        }

        Vector vector = new Vector( value.size() );

        for ( Iterator it = value.iterator(); it.hasNext(); )
        {
            Object object = it.next();

            if ( convertElements )
            {
                object = objectToHashtable( object, excludedProperties, visitedObjects );
            }

            if ( object != null )
            {
                vector.add( object );
            }
        }

        return vector;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Method getSetter( Class clazz, String setterName, String key )
    {
        Map setterMap = getSetterMap( clazz );

        Method setter = (Method) setterMap.get( setterName );

        if ( setter == null )
        {
            getLogger().warn( "No setter for field '" + key + "' on the class '" + clazz.getName() + "'." );

            return null;
        }

        if ( setter.getParameterTypes().length != 1 )
        {
            getLogger().warn( "No setter for field '" + key + "' on the class '" + clazz.getName() + "'. " +
                              "The class has multiple setters for the field." );
        }

        return setter;
    }

    private Map getSetterMap( Class clazz )
    {
        // TODO: Cache the generated maps

        Method[] methods = clazz.getMethods();

        Map map = new HashMap();

        for ( int i = 0; i < methods.length; i++ )
        {
            Method method = methods[ i ];

            String name = method.getName();

            if ( name.length() <= 3 )
            {
                continue;
            }

            if ( !name.startsWith( "set" ) )
            {
                continue;
            }

            map.put( name, method );
        }

        return map;
    }
}
