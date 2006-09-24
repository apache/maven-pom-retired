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

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class WriteIncludeFileExtension extends AbstractContinuumAgentAction implements BuildResultsExtension, Startable {

    /**
     * @plexus.configuration
     */
    private String includePattern;

    /**
     * @plexus.configuration
     */
    private String fileNameTemplate;

    private StringTemplate template;

    /**
     * @plexus.configuration
     */
    private String resultsDirectory;

    /**
     * @plexus.configuration
     */
    private String dateFormat;


    private File directory;
    private SimpleDateFormat dateFormatter;

    /**
     * required for plexus
     */
    public WriteIncludeFileExtension() {
    }

    public WriteIncludeFileExtension(String includePattern, String fileNameTemplate, String resultsDirectory, String dateFormat) {
        this.includePattern = includePattern;
        this.fileNameTemplate = fileNameTemplate;
        this.resultsDirectory = resultsDirectory;
        this.dateFormat = dateFormat;
    }

    public void start() throws StartingException {
        template = new StringTemplate(fileNameTemplate);
        directory = new File(resultsDirectory);
        directory.mkdirs();

        if (!directory.exists()) {
            throw new IllegalStateException("File specified does not exist. " + directory.getAbsolutePath());
        }

        if (!directory.isDirectory()) {
            throw new IllegalStateException("File specified is not a directory. " + directory.getAbsolutePath());
        }

        if (!directory.canWrite()) {
            throw new IllegalStateException("Directory specified is not writable. " + directory.getAbsolutePath());
        }

        getLogger().info("Include files will be written to " + directory.getAbsolutePath());
        dateFormatter = new SimpleDateFormat(dateFormat);
    }

    public void stop() throws StoppingException {
    }

    public void execute(Map context) throws Exception {
        getLogger().debug("Pattern " + includePattern);
        Map map = new HashMap();
        map.putAll(context);
        map.put("date", dateFormatter.format(new Date()));

        for (Iterator iterator = context.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();

            if (key.matches(includePattern)) {
                try {
                    getLogger().debug("Found include pattern " + key);
                    String fileName = template.apply(map);
                    File file = new File(directory, fileName);
                    File parent = file.getParentFile();
                    parent.mkdirs();
                    write(file, (String) value);
                } catch (Exception e) {
                    getLogger().warn("Abnormal failure on header " + key, e);
                }
            }
        }

    }

    private void write(File file, String content) {
        try {
            getLogger().info("Writing " + content.length() + " characters to " + file.getAbsolutePath());
            FileOutputStream out = new FileOutputStream(file);
            out.write(content.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            getLogger().error("Could not write to file " + file.getAbsolutePath(), e);
        }
    }
}
