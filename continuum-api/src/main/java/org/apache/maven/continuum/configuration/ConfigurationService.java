package org.apache.maven.continuum.configuration;

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

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public interface ConfigurationService
{
    String ROLE = ConfigurationService.class.getName();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    String CONFIGURATION = "configuration";

    String CONFIGURATION_INITIALIZED = "initialized";

    String CONFIGURATION_URL = "url";

    String CONFIGURATION_BUILD_OUTPUT_DIRECTORY = "build-output-directory";

    String CONFIGURATION_WORKING_DIRECTORY = "working-directory";

    String CONFIGURATION_COMPANY_LOGO = "company-logo";

    String CONFIGURATION_COMPANY_NAME = "company-name";

    String CONFIGURATION_COMPANY_URL = "company-url";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    void setInitialized( boolean initialized );

    boolean isInitialized();

    String getUrl();

    void setUrl( String url );

    File getBuildOutputDirectory();

    void setBuildOutputDirectory( File buildOutputDirectory );

    File getWorkingDirectory();

    void setWorkingDirectory( File workingDirectory );

    void setJdks( Map jdks );

    void setInMemoryMode( boolean inMemoryMode );

    boolean inMemoryMode();

    String getCompanyLogo();

    void setCompanyLogo( String companyLogoUrl );

    String getCompanyName();

    void setCompanyName( String companyName );

    String getCompanyUrl();

    void setCompanyUrl( String companyUrl );

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    void load()
        throws ConfigurationLoadingException;

    void store()
        throws ConfigurationStoringException;
}
