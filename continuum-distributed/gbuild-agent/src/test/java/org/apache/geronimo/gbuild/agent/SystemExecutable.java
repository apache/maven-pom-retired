/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.gbuild.agent;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils;

import java.io.File;

import junit.framework.Assert;

/**
 * @version $Rev$ $Date$
 */
public class SystemExecutable {

    public static void system(File workingDirectory, String cmd, String arguments)
            throws CommandLineException {
        system(workingDirectory, cmd, new String[]{arguments});
    }

    public static void system(File workingDirectory, String cmd, String[] arguments)
            throws CommandLineException {
        Commandline commandline = new Commandline();

        commandline.setExecutable(cmd);

        commandline.setWorkingDirectory(workingDirectory.getAbsolutePath());

        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];

            commandline.createArgument().setLine(argument);
        }

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        int exitCode = CommandLineUtils.executeCommandLine(commandline, stdout, stderr);

        if (exitCode != 0) {
            System.err.println("Error while executing command: " + commandline.toString());
            System.err.println("workingDirectory: " + workingDirectory.getAbsolutePath());
            System.err.println("Exit code: " + exitCode);

            System.err.println("Standard output:");
            line();
            System.err.println(stdout.getOutput());
            line();
            System.err.println("Standard Error:");
            line();
            System.err.println(stderr.getOutput());
            line();

            Assert.fail("The command failed.");
        }
    }

    private static void line() {
        System.err.println("-------------------------------------------------------------------------------");
    }
}
