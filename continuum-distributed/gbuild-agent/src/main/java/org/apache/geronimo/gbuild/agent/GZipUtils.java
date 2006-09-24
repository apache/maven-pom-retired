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

import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * @version $Rev$ $Date$
 */
public class GZipUtils {

    /**
     * Reads data from a file in to a gzip byte array.
     *
     * @param file  The name of the file to read.
     * @return The GZipped content of the file.
     */
    public static byte[] fileRead(File file) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GZIPOutputStream out = new GZIPOutputStream(baos);

        FileInputStream in = null;

        try {
            in = new FileInputStream(file);
            int count;
            byte[] b = new byte[512];
            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
        }
        finally {
            IOUtil.close(in);
            IOUtil.close(out);
        }

        return baos.toByteArray();
    }

    /**
     * Writes data to a file. The file will be created if it does not exist.
     *
     * @param file  The name of the file to write.
     * @param bytes The GZipped content to write to the file.
     */
    public static void fileWrite(File file, byte[] bytes) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        GZIPInputStream in = new GZIPInputStream(bais);

        FileOutputStream out = null;

        try {
            out = new FileOutputStream(file);
            int count;
            byte[] b = new byte[512];
            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
        }
        finally {
            IOUtil.close(out);
            IOUtil.close(in);
        }
    }

    /**
     * Writes data to a file. The file will be created if it does not exist.
     *
     * @param file  The name of the file to write.
     * @param bytes The GZipped content to write to the file.
     */
    public static void fileAppend(File file, byte[] bytes) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        GZIPInputStream in = new GZIPInputStream(bais);

        FileOutputStream out = null;

        try {
            out = new FileOutputStream(file, true);
            int count;
            byte[] b = new byte[512];
            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
        }
        finally {
            IOUtil.close(out);
            IOUtil.close(in);
        }
    }
}
