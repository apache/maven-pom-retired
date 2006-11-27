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
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

/**
 * @version $Rev$ $Date$
 */
public class CVS extends SystemExecutable {
    private final String cmd = "cvs";
    private final File cvsroot;

    public CVS(String cvsroot) {
        this(new File(cvsroot));
    }

    public CVS(File cvsroot) {
        this.cvsroot = cvsroot.getAbsoluteFile();
    }

    public File getCvsroot() {
        return cvsroot;
    }

    public void _import(File root, String artifactId) throws CommandLineException {
        system(root, cmd, "-d " + cvsroot.getAbsolutePath() + " import -m yo_yo " + artifactId + " continuum_test start");
    }

    public void init() throws IOException, CommandLineException {
        if (cvsroot.isDirectory()) {
            FileUtils.deleteDirectory(cvsroot);
        }

        Assert.assertTrue("Could not make directory " + cvsroot, cvsroot.mkdirs());

        system(cvsroot, cmd, " -d " + cvsroot.getAbsolutePath() + " init");
    }
}
