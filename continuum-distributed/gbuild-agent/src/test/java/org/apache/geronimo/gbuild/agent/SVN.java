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

import java.io.File;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class SVN extends SystemExecutable {
    private final String cmd = "svn";
    private final String svnUrl;

    public SVN(String svnUrl) {
        this.svnUrl = svnUrl;
    }

    public void _import(File dir) throws CommandLineException {
//        svn import /tmp/myproject file:///path/to/repos/myproject -m "initial import"
        system(dir.getParentFile(), "svn", "import " + dir.getAbsolutePath() + " " + svnUrl +" -m yo_yo");
    }

    public String getSvnUrl() {
        return svnUrl;
    }

//    public static SVN checkout(URL url, File dir) throws CommandLineException {
//        system(dir.getParentFile(), "svn", "co " + url.toExternalForm() + " " + dir.getName());
//        return new SVN(dir);
//    }
//
    public static void create(File dir) throws CommandLineException {
        //svnadmin create /home/projects/FOO/scm
        system(dir.getParentFile(), "svnadmin", "create " + dir.getAbsolutePath());
    }
//
//    public void add(String artifactId) throws CommandLineException {
//        system(work, cmd, "add " + artifactId);
//    }
//
//    public void commit(String artifactId) throws CommandLineException {
//        system(work, cmd, "ci -m yo_yo " + artifactId);
//    }
}
