package org.apache.maven.continuum.management;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.store.ContinuumStoreException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

/**
 * Continuum database management tool API.
 */
public interface DataManagementTool
{
    /** Plexus Role. */
    String ROLE = DataManagementTool.class.getName();

    /** The filename to use. */
    String BUILDS_XML = "builds.xml";

    /**
     * Backup the build database.
     * @param backupDirectory the directory to backup to
     * @throws java.io.IOException if there is a problem writing to the backup file
     * @throws ContinuumStoreException if there is a problem reading from the database
     */
    void backupBuildDatabase( File backupDirectory )
        throws IOException, ContinuumStoreException;

    /**
     * Restore the build database.
     * @param backupDirectory the directory where the backup to restore from resides
     * @throws java.io.IOException if there is a problem reading the backup file
     * @throws javax.xml.stream.XMLStreamException if there is a problem parsing the backup file
     */
    void restoreBuildDatabase( File backupDirectory )
        throws IOException, XMLStreamException;

    /**
     * Smoke the build database.
     */
    void eraseBuildDatabase();
}
