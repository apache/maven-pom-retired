/**
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

package org.apache.maven.mercury.http.server;

import junit.framework.TestCase;

import org.apache.maven.mercury.http.server.StagingBatchFilter;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;
import org.mortbay.util.IO;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;

public class BatchFilterTest extends TestCase
{
    File _baseDir;
    File _stagingDir;
    
    ServletTester tester;
    

    public void setUp () throws Exception
    {
        _baseDir = File.createTempFile("testBatchFilter",null);
        _baseDir.delete();
        _baseDir.mkdir();
        _baseDir.deleteOnExit();
        assertTrue(_baseDir.isDirectory());
        
        _stagingDir = new File(System.getProperty("java.io.tmpdir"));

        super.setUp();
        tester=new ServletTester();
        tester.setContextPath("/context");
        tester.setResourceBase(_baseDir.getCanonicalPath());
        tester.addServlet( DefaultServlet.class, "/");
        FilterHolder holder = tester.addFilter( StagingBatchFilter.class,"/*",0);
        holder.setInitParameter("stagingDirURI", _stagingDir.toURI().toString());
        tester.start();
        System.err.println("Set up tester, basedir="+tester.getResourceBase()+" exists?"+_baseDir.exists());
        System.err.println("Set up tester, stagingdir="+_stagingDir.toURI().toString());
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    

    public void testHandlePutWithoutBatching() throws Exception
    {
        // generated and parsed test
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
        
        request.setMethod("PUT");
        request.setVersion("HTTP/1.1");
        request.setHeader("Host","tester");
        request.setURI("/context/file.txt");
        request.setHeader("Content-Type","text/plain");
        String data0="Now is the time for all good men to come to the aid of the party";
        request.setContent(data0);
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_CREATED,response.getStatus());
        
        File file=new File(_baseDir,"file.txt");
        assertTrue(file.exists());
        assertEquals(data0,IO.toString(new FileInputStream(file)));
    }
    
    public void testBatchingCommit() throws Exception
    {
        // generated and parsed test
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
        
        request.setMethod("PUT");
        request.setVersion("HTTP/1.1");
        request.setHeader("Host","tester");
        request.setURI("/context/file1.txt");
        request.setHeader("Content-Type","text/plain");
        request.setHeader("Jetty-Batch-Id", "999999");
        String data1="How Now BROWN COW!!!!";
        request.setContent(data1);
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_CREATED,response.getStatus());
        
        File batchDir = new File (_stagingDir, "999999");
        File stagedFile = new File (batchDir, "file1.txt");
        System.err.println("Checking existence of "+stagedFile.getCanonicalPath());
        assertTrue(stagedFile.exists());
        assertEquals(data1,IO.toString(new FileInputStream(stagedFile)));

        request.setMethod("PUT");
        request.setURI("/context/file2.txt");
        request.setHeader("Content-Type","text/plain");
        String data2="Blah blah blah Blah blah";
        request.setHeader("Jetty-Batch-Id", "999999");
        request.setContent(data2);
        response.parse(tester.getResponses(request.generate()));
        assertEquals(HttpServletResponse.SC_CREATED,response.getStatus());
        
        batchDir = new File (_stagingDir, "999999");
        stagedFile = new File (batchDir, "file2.txt");
        assertTrue(stagedFile.exists());
        assertEquals(data2,IO.toString(new FileInputStream(stagedFile)));

        // test POST commit
        request = new HttpTester();
        request.setMethod("POST");
        request.setVersion("HTTP/1.1");
        request.setHeader("Content-Type","text/plain");
        request.setHeader("Host","tester");
        request.setHeader("Jetty-Batch-Commit", "999999");
        request.setURI("/context/");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_OK,response.getStatus());

        File finalFile1 = new File(_baseDir, "file1.txt");
        assertTrue(finalFile1.exists());
        File finalFile2 = new File(_baseDir, "file2.txt");
        assertTrue(finalFile2.exists());
    }
    
    
    public void testBatchingDiscard () 
    throws Exception 
    {
        // generated and parsed test
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
   
        request.setMethod("PUT");
        request.setVersion("HTTP/1.1");
        request.setHeader("Host","tester");
        request.setURI("/context/org/foo/file1.txt");
        request.setHeader("Content-Type","text/plain");
        request.setHeader("Jetty-Batch-Id", "999999");
        String data1="How Now BROWN COW!!!!";
        request.setContent(data1);
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_CREATED,response.getStatus());
        
        File batchDir = new File (_stagingDir, "999999");
        File stagedFile = new File(batchDir, "org");
        stagedFile = new File(stagedFile, "foo");
        stagedFile = new File (stagedFile, "file1.txt");
        System.err.println("Checking existence of "+stagedFile.getCanonicalPath());
        assertTrue(stagedFile.exists());
        assertEquals(data1,IO.toString(new FileInputStream(stagedFile)));

        request.setMethod("PUT");
        request.setVersion("HTTP/1.1");
        request.setHeader("Host","tester");
        request.setURI("/context/file2.txt");
        request.setHeader("Content-Type","text/plain");
        String data2="Blah blah blah Blah blah";
        request.setHeader("Jetty-Batch-Id", "999999");
        request.setContent(data2);
        response.parse(tester.getResponses(request.generate()));
        assertEquals(HttpServletResponse.SC_CREATED,response.getStatus());
        
        batchDir = new File (_stagingDir, "999999");
        stagedFile = new File (batchDir, "file2.txt");
        assertTrue(stagedFile.exists());
        assertEquals(data2,IO.toString(new FileInputStream(stagedFile)));

        // test POST discard
        request = new HttpTester();
        request.setMethod("POST");
        request.setVersion("HTTP/1.1");
        request.setHeader("Content-Type","text/plain");
        request.setHeader("Host","tester");
        request.setHeader("Jetty-Batch-Discard", "999999");
        request.setURI("/context/");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_OK,response.getStatus());

        File finalFile1 = new File(_baseDir, "org");
        finalFile1 = new File(finalFile1, "foo");
        finalFile1 = new File(finalFile1, "file1.txt");
        assertFalse(finalFile1.exists());
        File finalFile2 = new File(_baseDir, "file2.txt");
        assertFalse(finalFile2.exists());
    }
    
}
