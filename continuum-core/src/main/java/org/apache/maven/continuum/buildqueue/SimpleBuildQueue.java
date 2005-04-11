package org.apache.maven.continuum.buildqueue;

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

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: SimpleBuildQueue.java,v 1.1.1.1 2005/03/29 20:42:01 trygvis Exp $
 */
public class SimpleBuildQueue
    extends AbstractBuildQueue
    implements BuildQueue
{
    /**
     * The queue of elements.
     */
    private List queue = new LinkedList();

    public String dequeue()
    {
        synchronized ( queue )
        {
            if ( queue.size() == 0 )
                return null;

            return (String) queue.remove( 0 );
        }
    }

    public void enqueue( String projectId, String buildId )
    {
        synchronized ( queue )
        {
            queue.add( projectId );
        }
    }
}
