package org.apache.maven.continuum.execution;

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
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumBuildExecutionResult
{
    private boolean success;

    private String standardOutput;

    private String standardError;

    private int exitCode;

    public ContinuumBuildExecutionResult( boolean success, String standardOutput, String standardError, int exitCode )
    {
        this.success = success;

        this.standardOutput = standardOutput;

        this.standardError = standardError;

        this.exitCode = exitCode;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getStandardOutput()
    {
        return standardOutput;
    }

    public String getStandardError()
    {
        return standardError;
    }

    public int getExitCode()
    {
        return exitCode;
    }
}
