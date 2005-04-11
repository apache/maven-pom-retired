package org.apache.maven.continuum.builder.shell;

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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ShellCommandHelper.java,v 1.1.1.1 2005/03/29 20:42:00 trygvis Exp $
 */
public interface ShellCommandHelper
{
    String ROLE = ShellCommandHelper.class.getName();

    ExecutionResult executeShellCommand( File workingDirectory, String shellCommand, String[] arguments )
        throws Exception;
}
