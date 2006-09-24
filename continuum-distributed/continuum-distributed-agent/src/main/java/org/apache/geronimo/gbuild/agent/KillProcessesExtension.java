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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
public class KillProcessesExtension extends AbstractLogEnabled implements BuildAgentExtension {

    /**
     * @plexus.requirement
     */
    private BuildActivityNotifier notifier;

    private String regex;

    public void preProcess(Map build) {
    }

    public void postProcess(Map build, Map results) {
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");

        if (isWindows){
            return;
        }
        
        String[] ids = null;

        List pids = null;
        try {
            pids = findProcessIds(regex);
        } catch (Exception e) {
            getLogger().warn("Failed to get a list of running processes. ", e);
            return;
        }

        String processes = "";
        for (int i = 0; i < pids.size(); i++) {
            String pid = (String) pids.get(i);
            processes += pid +", ";
        }

        processes = processes.replaceAll(", $","");

        if (pids.size() > 0){

            String message = "Found " + pids.size()+ " processes matching \""+regex+"\" (" + processes + ").";

            results.put("header.hung-processes", processes);
            getLogger().info(message);
            notifier.sendNotification(build, message);

            try {
                killProcesses(pids);
            } catch (Exception e) {
                getLogger().error("Unable to kill "+pids.size()+" processes matching \""+regex+"\" (" + processes + ").", e);
            }

        } else {
            getLogger().debug("No processes found matching \""+regex+"\".");
        }
    }

    public static void killProcesses(List pids) throws Exception {
        ExecResult res = exec("kill", (String[]) pids.toArray(new String[]{}));
        if (res.exitCode != 0){
            throw new IllegalStateException("Command returned error exit code " + res.exitCode + ".");
        }
    }

    public static List findProcessIds(String regex) throws Exception {
        ExecResult res = exec("sh", new String[]{"-c","\"ps ax\""});
        if (res.exitCode != 0){
            throw new IllegalStateException("Command returned error exit code " + res.exitCode + ".");
        }
        String stdout = res.getStdout();
        String[] lines = stdout.split("[\n\r]");
        List list = new ArrayList();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.matches(regex)){
                line = line.replaceFirst(" *","");
                String pid = line.split(" +")[0];
                list.add(pid);
            }
        }
        return list;
    }

    public static class ExecResult {
        private final int exitCode;
        private final String stdout;
        private final String stderr;

        public ExecResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }
    }

    public static ExecResult exec(String cmd, String[] arguments) throws CommandLineException {
        Commandline commandline = new Commandline();
        commandline.setExecutable(cmd);
        commandline.setWorkingDirectory(new File(".").getAbsolutePath());

        for (int i = 0; i < arguments.length; i++) {
            commandline.createArgument().setLine(arguments[i]);
        }

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        int exitCode = CommandLineUtils.executeCommandLine(commandline, stdout, stderr);
        return new ExecResult(exitCode, stdout.getOutput(), stderr.getOutput());
    }

}
