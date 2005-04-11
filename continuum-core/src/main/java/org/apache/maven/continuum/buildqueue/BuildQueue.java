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

/**
 * A queue of build job ids.
 *
 * <it>A <code>BuildQueue</code> implementation MUST be thread safe.</it>
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: BuildQueue.java,v 1.1.1.1 2005/03/29 20:42:01 trygvis Exp $
 */
public interface BuildQueue
{
    String ROLE = BuildQueue.class.getName();

    /**
     * Returns a bulid id from the queue.
     * <p/>
     * Returns <code>null</code> if the queue is empty.
     *
     * @return Returns a build id from the queue or <code>null</code> if the queue is empty.
     */
    String dequeue()
        throws BuildQueueException;

    /**
     * @param projectId The id of the build to enqueue.
     * @param buildId
     */
    void enqueue( String projectId, String buildId )
        throws BuildQueueException;
}
