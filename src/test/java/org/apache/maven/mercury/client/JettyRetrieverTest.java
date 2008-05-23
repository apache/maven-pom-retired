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

package org.apache.maven.mercury.client;

import junit.framework.TestCase;

import org.apache.maven.mercury.client.BatchException;
import org.apache.maven.mercury.client.Binding;
import org.apache.maven.mercury.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.server.SimpleTestServer;
import org.apache.maven.mercury.validate.Validator;

import java.io.File;
import java.util.HashSet;
import java.util.List;

public class JettyRetrieverTest extends TestCase
{
    public static final String __HOST_FRAGMENT = "http://localhost:";
    public static final String __PATH_FRAGMENT = "/maven2/repo/";
    public String _port;
    File file0;
    File file1;
    File file2;
    File file3;
    File file4;
    File file5;
    Binding binding0 = new Binding();
    Binding binding1 = new Binding();
    Binding binding2 = new Binding();
    Binding binding3 = new Binding();
    Binding binding4 = new Binding();
    Binding binding5 = new Binding();
    DefaultRetriever retriever;
    SimpleTestServer server;
    
    
    public class TxtValidator implements Validator 
    {

        public String getFileExtension()
        {
            return "txt";
        }

        public boolean validate(String stagedFile, List<String> errors)
        {
            if (stagedFile==null)
                return true;
            int i = stagedFile.lastIndexOf(".");
            String ext = (i>=0?stagedFile.substring(i+1):"");
            if ("txt".equalsIgnoreCase(ext))
            {
                //just accept any file contents
                File f = new File(stagedFile);
                return f.isFile();
            }
            
            return false;
        }
        
    }
    
    public class AlwaysFalseTxtValidator extends TxtValidator
    {
        public String getFileExtension()
        {
            System.err.println("Returning extension for AlwaysFalseTxtValidator");
            return "txt";
        }

        public boolean validate(String stagedFile, List<String> errors)
        {
            System.err.println("Evaluating "+stagedFile);
            errors.add("Always false");
            return false;
        }
    }
    
    public void setUp ()
    throws Exception
    {
        retriever = new DefaultRetriever();
        
        server = new SimpleTestServer();
        server.start();
        _port=String.valueOf(server.getPort()); 
    }
    
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        server.stop();
        server.destroy();
    }



    public File mkTempDir()
    throws Exception
    {
        File dir = File.createTempFile("mercury", "tmp");
        dir.delete();
        dir.mkdir();
        dir.deleteOnExit();
        assert dir.exists();
        assert dir.isDirectory();
        assert dir.canWrite();
        return dir;
    }
    
    public void testSyncRetrievalAllGood()
    throws Exception
    {
        //make local dir to put stuff in
        final File dir = mkTempDir();
        RetrievalRequestImpl request = new RetrievalRequestImpl();
        HashSet<Binding> bindings = new HashSet<Binding>();

        file0 = new File(dir, "file0.txt");
        file1 = new File(dir, "file1.txt");
        file2 = new File(dir, "file2.txt");
        file3 = new File(dir, "file3.jar");
        file4 = new File(dir, "file4.so");
        file5 = new File(dir, "file5.jpg");
        binding0.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
        binding0.setLocalFile(file0);
        bindings.add(binding0);
        
        binding1.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"); //has no sha file
        binding1.setLocalFile(file1);
        bindings.add(binding1);
       
        binding2.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file2.txt"); //has wrong sha file
        binding2.setLocalFile(file2);
        bindings.add(binding2);
      
        binding3.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
        binding3.setLocalFile(file3);
        bindings.add(binding3);
        
        binding4.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
        binding4.setLocalFile(file4);
        bindings.add(binding4);
       
        binding5.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
        binding5.setLocalFile(file5);
        bindings.add(binding5);
          
        request.setBindings(bindings);
        
        RetrievalResponse response = retriever.retrieve(request);
        
//        for (BatchException t:response.getExceptions())
//            t.printStackTrace();
        
        assertEquals(2,response.getExceptions().size());
        assertTrue(!file0.exists());
        assertTrue(!file1.exists());
        assertTrue(!file2.exists());
        assertTrue(!file3.exists());
        assertTrue(!file4.exists());
        assertTrue(!file5.exists());

    }


    public void testSyncRetrievalFailFast()
        throws Exception
    {
        //make local dir to put stuff in
        final File dir = mkTempDir();
        RetrievalRequestImpl request = new RetrievalRequestImpl();
        HashSet<Binding> bindings = new HashSet<Binding>();

        file0 = new File(dir, "file0.txt");
        file1 = new File(dir, "file1.txt");
        file2 = new File(dir, "file2.txt");
        file3 = new File(dir, "file3.jar");
        file4 = new File(dir, "file4.so");
        file5 = new File(dir, "file5.jpg");
        binding0.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
        binding0.setLocalFile(file0);
        bindings.add(binding0);

        binding1.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"); //has no sha file
        binding1.setLocalFile(file1);
        binding1.setLenientChecksum(false);
        bindings.add(binding1);

        binding2.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file2.txt"); //has wrong sha file
        binding2.setLocalFile(file2);
        binding2.setLenientChecksum(true);
        bindings.add(binding2);

        binding3.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
        binding3.setLocalFile(file3);
        bindings.add(binding3);

        binding4.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
        binding4.setLocalFile(file4);
        bindings.add(binding4);

        binding5.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
        binding5.setLocalFile(file5);
        bindings.add(binding5);


        request = new RetrievalRequestImpl();
        request.setBindings(bindings);
        request.setFailFast(true);

        request.setBindings(bindings);
        
        RetrievalResponse response = retriever.retrieve(request);

        //for (BatchException t:response.getExceptions())
        //   t.printStackTrace();

        assertTrue(!file0.exists());
        assertTrue(!file1.exists());
        assertTrue(!file2.exists());
        assertTrue(!file3.exists());
        assertTrue(!file4.exists());
        assertTrue(!file5.exists());
        
        Thread.sleep(100);
    }

    public void testSyncRetrievalLenient0()
        throws Exception
    {
        //make local dir to put stuff in
        final File dir = mkTempDir();
        RetrievalRequestImpl request = new RetrievalRequestImpl();
        HashSet<Binding> bindings = new HashSet<Binding>();

        file0 = new File(dir, "file0.txt");
        file1 = new File(dir, "file1.txt");
        file2 = new File(dir, "file2.txt");
        file3 = new File(dir, "file3.jar");
        file4 = new File(dir, "file4.so");
        file5 = new File(dir, "file5.jpg");
        binding0.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
        binding0.setLocalFile(file0);
        bindings.add(binding0);

        binding1.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"); //has no sha file
        binding1.setLocalFile(file1);
        binding1.setLenientChecksum(true);
        bindings.add(binding1);

        binding2.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file2.txt"); //has wrong sha file
        binding2.setLocalFile(file2);
        binding2.setLenientChecksum(true);
        bindings.add(binding2);

        binding3.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
        binding3.setLocalFile(file3);
        bindings.add(binding3);

        binding4.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
        binding4.setLocalFile(file4);
        bindings.add(binding4);

        binding5.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
        binding5.setLocalFile(file5);
        bindings.add(binding5);

        request.setBindings(bindings);
        request.setFailFast(false);
        RetrievalResponse response = retriever.retrieve(request);

        assertEquals(1,response.getExceptions().size());
        assertEquals(binding2,response.getExceptions().iterator().next().getBinding());
        assertTrue(!file0.exists());
        assertTrue(!file1.exists());
        assertTrue(!file2.exists());
        assertTrue(!file3.exists());
        assertTrue(!file4.exists());
        assertTrue(!file5.exists());
    }

    public void testSyncRetrievalLenient1()
    throws Exception
    {
        //make local dir to put stuff in
        final File dir = mkTempDir();
        RetrievalRequestImpl request = new RetrievalRequestImpl();
        HashSet<Binding> bindings = new HashSet<Binding>();

        file0 = new File(dir, "file0.txt");
        file1 = new File(dir, "file1.txt");
        file2 = new File(dir, "file2.txt");
        file3 = new File(dir, "file3.jar");
        file4 = new File(dir, "file4.so");
        file5 = new File(dir, "file5.jpg");
        binding0.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
        binding0.setLocalFile(file0);
        bindings.add(binding0);

        binding1.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"); //has no sha file
        binding1.setLocalFile(file1);
        binding1.setLenientChecksum(true);
        bindings.add(binding1);

        binding3.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
        binding3.setLocalFile(file3);
        bindings.add(binding3);

        binding4.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
        binding4.setLocalFile(file4);
        bindings.add(binding4);

        binding5.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
        binding5.setLocalFile(file5);
        bindings.add(binding5);

        request.setBindings(bindings);
        request.setFailFast(false);
        RetrievalResponse response = retriever.retrieve(request);

        for (BatchException t:response.getExceptions())
            t.printStackTrace();

        assertEquals(0,response.getExceptions().size());
        assertTrue(file0.exists());
        assertTrue(file1.exists());
        assertTrue(!file2.exists());
        assertTrue(file3.exists());
        assertTrue(file4.exists());
        assertTrue(file5.exists());

    }
    
    public void testValidatorSuccess() throws Exception
    {
            //make local dir to put stuff in
            final File dir = mkTempDir();
            RetrievalRequestImpl request = new RetrievalRequestImpl();
            HashSet<Binding> bindings = new HashSet<Binding>();
            HashSet<Validator> validators = new HashSet<Validator>();
            validators.add(new TxtValidator());
            request.setValidators(validators);

            file0 = new File(dir, "file0.txt");
            file1 = new File(dir, "file1.txt");
            file2 = new File(dir, "file2.txt");
            file3 = new File(dir, "file3.jar");
            file4 = new File(dir, "file4.so");
            file5 = new File(dir, "file5.jpg");
            binding0.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
            binding0.setLocalFile(file0);
            bindings.add(binding0);

            binding1.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"); //has no sha file
            binding1.setLocalFile(file1);
            binding1.setLenientChecksum(true);
            bindings.add(binding1);

            binding3.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
            binding3.setLocalFile(file3);
            bindings.add(binding3);

            binding4.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
            binding4.setLocalFile(file4);
            bindings.add(binding4);

            binding5.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
            binding5.setLocalFile(file5);
            bindings.add(binding5);

            request.setFailFast(false);

            request.setBindings(bindings);
            RetrievalResponse response = retriever.retrieve(request);

            //for (BatchException t:response.getExceptions())
            //    t.printStackTrace();

            assertEquals(0,response.getExceptions().size());
            assertTrue(file0.exists());
            assertTrue(file1.exists());
            assertTrue(!file2.exists());
            assertTrue(file3.exists());
            assertTrue(file4.exists());
            assertTrue(file5.exists());
    }
    
    public void testValidatorFailure () throws Exception
    {
        //make local dir to put stuff in
        final File dir = mkTempDir();
        RetrievalRequestImpl request = new RetrievalRequestImpl();
        HashSet<Binding> bindings = new HashSet<Binding>();
        HashSet<Validator> validators = new HashSet<Validator>();
        validators.add(new AlwaysFalseTxtValidator());
        request.setValidators(validators);

        file0 = new File(dir, "file0.txt");
        file1 = new File(dir, "file1.txt");
        file2 = new File(dir, "file2.txt");
        file3 = new File(dir, "file3.jar");
        file4 = new File(dir, "file4.so");
        file5 = new File(dir, "file5.jpg");
        binding0.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
        binding0.setLocalFile(file0);
        bindings.add(binding0);

        binding1.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"); //has no sha file
        binding1.setLocalFile(file1);
        binding1.setLenientChecksum(true);
        bindings.add(binding1);

        binding3.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
        binding3.setLocalFile(file3);
        bindings.add(binding3);

        binding4.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
        binding4.setLocalFile(file4);
        bindings.add(binding4);

        binding5.setRemoteUrl(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
        binding5.setLocalFile(file5);
        bindings.add(binding5);

        request.setFailFast(false);

        request.setBindings(bindings);
        RetrievalResponse response = retriever.retrieve(request);

        //for (BatchException t:response.getExceptions())
        //    t.printStackTrace();

        assertEquals(2,response.getExceptions().size());
        assertTrue(!file0.exists());
        assertTrue(!file1.exists());
        assertTrue(!file2.exists());
        assertTrue(!file3.exists());
        assertTrue(!file4.exists());
        assertTrue(!file5.exists());
    }
}
