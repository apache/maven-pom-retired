package org.apache.maven.continuum.wagon;

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

import java.io.File;
import java.util.Map;

import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.ConfigurationStoringException;

/**
 * Mock class for testing WagonContinuumNotifier's call to ConfigurationService.getBuildOutputFile()
 * 
 * @author <a href="mailto:nramirez@exist">Napoleon Esmundo C. Ramirez</a>
 *
 */
public class MockConfigurationService implements ConfigurationService
{
    private String basedir;
    
    public MockConfigurationService()
    {
        basedir = System.getProperty( "basedir" );
    }
    
    public File getBuildOutputDirectory()
    {
        return new File( basedir, "src/test/resources" + "/" + "build-output-directory" );
    }
    
    public File getBuildOutputFile( int buildId, int projectId )
        throws ConfigurationException
    {
        File dir = new File( getBuildOutputDirectory(), Integer.toString( projectId ) );
        
        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new ConfigurationException( "Could not make the build output directory: " + "'" + dir.getAbsolutePath() + "'." );
        }
        
        return new File( dir, buildId + ".log.txt" );
    }
    
    public File getWorkingDirectory()
    {
        return new File( basedir, "src/test/resources" + "/" + "working-directory" );
    }

    public File getApplicationHome()
    {
        return null;
    }

    public boolean isInitialized()
    {
        return false;
    }

    public void setInitialized( boolean initialized )
    {
    }

    public String getUrl()
    {
        return null;
    }

    public void setUrl( String url )
    {
    }

    public void setBuildOutputDirectory( File buildOutputDirectory )
    {
    }

    public void setWorkingDirectory( File workingDirectory )
    {
    }

    public File getDeploymentRepositoryDirectory()
    {
        return null;
    }

    public void setDeploymentRepositoryDirectory( File deploymentRepositoryDirectory )
    {
    }

    public void setJdks( Map jdks )
    {
    }

    public String getCompanyLogo()
    {
        return null;
    }

    public void setCompanyLogo( String companyLogoUrl )
    {
    }

    public String getCompanyName()
    {
        return null;
    }

    public void setCompanyName( String companyName )
    {
    }

    public String getCompanyUrl()
    {
        return null;
    }

    public void setCompanyUrl( String companyUrl )
    {
    }

    public boolean isGuestAccountEnabled()
    {
        return false;
    }

    public void setGuestAccountEnabled( boolean enabled )
    {
    }

    public String getBuildOutput( int buildId, int projectId )
        throws ConfigurationException
    {
        return null;
    }

    public File getFile( String filename )
    {
        return null;
    }

    public boolean isLoaded()
    {
        return false;
    }

    public void load()
        throws ConfigurationLoadingException
    {
    }

    public void store()
        throws ConfigurationStoringException
    {
    }
}