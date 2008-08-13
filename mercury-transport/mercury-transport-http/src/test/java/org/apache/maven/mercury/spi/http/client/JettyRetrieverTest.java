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

package org.apache.maven.mercury.spi.http.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.spi.http.server.SimpleTestServer;
import org.apache.maven.mercury.spi.http.validate.Validator;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Server;
import org.mortbay.util.IO;

public class JettyRetrieverTest extends TestCase
{
    public static final String __HOST_FRAGMENT = "http://localhost:";
    public static final String __PATH_FRAGMENT = "/maven2/repo/";
    
    private static final String publicKeyFile = "/pgp/pubring.gpg";
    
    public String _port;
    File file0;
    File file1;
    File file2;
    File file3;
    File file4;
    File file5;
    File file6;
    
    DefaultRetriever retriever;
    SimpleTestServer server;
    Server remoteServerType;
    HashSet<StreamVerifierFactory> factories;
    File dir;
    

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
            return "txt";
        }

        public boolean validate(String stagedFile, List<String> errors)
        {
            errors.add("Always false");
            return false;
        }
    }
    
    public void setUp ()
    throws Exception
    {
        server = new SimpleTestServer();
        server.start();
        _port=String.valueOf(server.getPort()); 
        
        HashSet<Server> remoteServerTypes = new HashSet<Server>();
        remoteServerType = new Server( "test", new URL(__HOST_FRAGMENT+_port));
        factories = new HashSet<StreamVerifierFactory>();
            
        remoteServerTypes.add(remoteServerType);
        
        retriever = new DefaultRetriever();
        retriever.setServers(remoteServerTypes);
    }
    
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        server.stop();
        server.destroy();
        destroy(dir);
    }
    
    
    public void destroy (File f)
    {
        if (f == null)
            return;
        if (f.isDirectory())
        {
            File[] files = f.listFiles();
            for (int i=0;files!=null && i<files.length; i++)
            {
                destroy (files[i]);
            }  
        }
        f.delete(); 
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
        factories.add( new SHA1VerifierFactory(false, true) ); //!lenient, sufficient
        remoteServerType.setStreamObserverFactories(factories);
        
        //make local dir to put stuff in
        dir = mkTempDir();
        DefaultRetrievalRequest request = new DefaultRetrievalRequest();
        HashSet<Binding> bindings = new HashSet<Binding>();

        file0 = new File(dir, "file0.txt");
        file1 = new File(dir, "file1.txt");
        file2 = new File(dir, "file2.txt");
        file3 = new File(dir, "file3.jar");
        file4 = new File(dir, "file4.so");
        file5 = new File(dir, "file5.jpg");
        Binding binding0 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"), file0);
        bindings.add(binding0);
        
        Binding binding1 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"),file1); //has no sha file
        bindings.add(binding1);
       
        Binding binding2 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file2.txt"), file2); //has wrong sha file
        bindings.add(binding2);
      
        Binding binding3 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar"), file3);
        bindings.add(binding3);
        
        Binding binding4 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so"), file4);
        bindings.add(binding4);
       
        Binding binding5 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"), file5);
        bindings.add(binding5);
          
        request.setBindings(bindings);
        
        RetrievalResponse response = retriever.retrieve(request);
        
        System.err.println("--------- testSyncRetrievalAllGood --------------");
        for (HttpClientException t:response.getExceptions())
            t.printStackTrace();
        System.err.println("-------------------------------------------------");
        assertEquals(2,response.getExceptions().size());
        assertTrue(!file0.exists());
        assertTrue(!file1.exists());
        assertTrue(!file2.exists());
        assertTrue(!file3.exists());
        assertTrue(!file4.exists());
        assertTrue(!file5.exists());

    }

    
    public void testSyncRetrievalPgpGood()
    throws Exception
    {
      factories.add( 
          new PgpStreamVerifierFactory(
                  new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                  , getClass().getResourceAsStream( publicKeyFile )
                                      )
                    );
        remoteServerType.setStreamObserverFactories(factories);
        
        //make local dir to put stuff in
        dir = mkTempDir();
        DefaultRetrievalRequest request = new DefaultRetrievalRequest();
        HashSet<Binding> bindings = new HashSet<Binding>();

        file6 = new File(dir, "file6.gif");
        Binding binding0 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file6.gif"), file6);
        bindings.add(binding0);
          
        request.setBindings(bindings);
        
        RetrievalResponse response = retriever.retrieve(request);
        
        System.err.println("--------- testSyncRetrievalPgpGood --------------");
        for (HttpClientException t:response.getExceptions())
            t.printStackTrace();
        System.err.println("-------------------------------------------------");
        assertEquals( 0, response.getExceptions().size() );
        assertTrue( file6.exists() );
       
    }


    public void testSyncRetrievalFailFast()
        throws Exception
    {
        factories.add(new SHA1VerifierFactory(false, true)); //!lenient, sufficient
        remoteServerType.setStreamObserverFactories(factories);
        
        //make local dir to put stuff in
        dir = mkTempDir();
        DefaultRetrievalRequest request = new DefaultRetrievalRequest();
        HashSet<Binding> bindings = new HashSet<Binding>();

        file0 = new File(dir, "file0.txt");
        file1 = new File(dir, "file1.txt");
        file2 = new File(dir, "file2.txt");
        file3 = new File(dir, "file3.jar");
        file4 = new File(dir, "file4.so");
        file5 = new File(dir, "file5.jpg");
        
        Binding binding0 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"), file0);
        bindings.add(binding0);

        Binding binding1 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"), file1); //has no sha file
        bindings.add(binding1);

        Binding binding2 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file2.txt"), file2); //has wrong sha file
        bindings.add(binding2);

        Binding binding3 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar"), file3);
        bindings.add(binding3);

        Binding binding4 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so"), file4);
        bindings.add(binding4);

        Binding binding5 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"), file5);
        bindings.add(binding5);


        request = new DefaultRetrievalRequest();
        request.setBindings(bindings);
        request.setFailFast(true);

        request.setBindings(bindings);
        
        RetrievalResponse response = retriever.retrieve(request);

        System.err.println("--------- testSyncRetrievalFailFast -------------");
        for (HttpClientException t:response.getExceptions())
           t.printStackTrace();
        System.err.println("-------------------------------------------------");

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
        factories.add(new SHA1VerifierFactory(true, true)); //lenient, sufficient
        remoteServerType.setStreamObserverFactories(factories);
        
        //make local dir to put stuff in
        dir = mkTempDir();
        DefaultRetrievalRequest request = new DefaultRetrievalRequest();
        HashSet<Binding> bindings = new HashSet<Binding>();

        file0 = new File(dir, "file0.txt");
        file1 = new File(dir, "file1.txt");
        file2 = new File(dir, "file2.txt");
        file3 = new File(dir, "file3.jar");
        file4 = new File(dir, "file4.so");
        file5 = new File(dir, "file5.jpg");
        Binding binding0 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"), file0);
        bindings.add(binding0);

        Binding binding1 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"), file1); //has no sha file
        bindings.add(binding1);

        Binding binding2 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file2.txt"), file2); //has wrong sha file
        bindings.add(binding2);

        Binding binding3 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar"),file3 );
        bindings.add(binding3);

        Binding binding4 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so"), file4);
        bindings.add(binding4);

        Binding binding5 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"), file5);
        bindings.add(binding5);

        request.setBindings(bindings);
        request.setFailFast(false);
        RetrievalResponse response = retriever.retrieve(request);

        System.err.println("--------- testSyncRetrievalLenient0 -------------");
        for (HttpClientException t:response.getExceptions())
           t.printStackTrace();
        System.err.println("-------------------------------------------------");

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
        factories.add(new SHA1VerifierFactory(true, true)); //lenient, sufficient
        remoteServerType.setStreamObserverFactories(factories);
        //make local dir to put stuff in
        dir = mkTempDir();
        DefaultRetrievalRequest request = new DefaultRetrievalRequest();
        HashSet<Binding> bindings = new HashSet<Binding>();

        file0 = new File(dir, "file0.txt");
        file1 = new File(dir, "file1.txt");
        file2 = new File(dir, "file2.txt");
        file3 = new File(dir, "file3.jar");
        file4 = new File(dir, "file4.so");
        file5 = new File(dir, "file5.jpg");
        Binding binding0 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"), file0);
        bindings.add(binding0);

        Binding binding1 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"), file1); //has no sha file
        bindings.add(binding1);

        Binding binding3 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar"), file3);
        bindings.add(binding3);

        Binding binding4 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so"), file4);
        bindings.add(binding4);

        Binding binding5 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"), file5);
        bindings.add(binding5);

        request.setBindings(bindings);
        request.setFailFast(false);
        RetrievalResponse response = retriever.retrieve(request);

        System.err.println("------------- testSyncRetrievalLenient1 ---------");
        for (HttpClientException t:response.getExceptions())
            t.printStackTrace();
        System.err.println("-------------------------------------------------");

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
        factories.add(new SHA1VerifierFactory(true, true)); //lenient, sufficient
        remoteServerType.setStreamObserverFactories(factories);

        //make local dir to put stuff in
        dir = mkTempDir();
        DefaultRetrievalRequest request = new DefaultRetrievalRequest();
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
        
        Binding binding0 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"), file0);
        bindings.add(binding0);

        Binding binding1 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"), file1); //has no sha file
        bindings.add(binding1);

        Binding binding3 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar"), file3);
        bindings.add(binding3);

        Binding binding4 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so"), file4);
        bindings.add(binding4);

        Binding binding5 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"), file5);
        bindings.add(binding5);

        request.setFailFast(false);

        request.setBindings(bindings);
        RetrievalResponse response = retriever.retrieve(request);

        System.err.println("------------ testValidatorSuccess ---------------");
        for (HttpClientException t:response.getExceptions())
            t.printStackTrace();
        System.err.println("-------------------------------------------------");

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
        factories.add(new SHA1VerifierFactory(true, true)); //lenient, sufficient
        remoteServerType.setStreamObserverFactories(factories);
        
        //make local dir to put stuff in
        dir = mkTempDir();
        DefaultRetrievalRequest request = new DefaultRetrievalRequest();
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
        
        Binding binding0 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"), file0);
        bindings.add(binding0);

        Binding binding1 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"), file1); //has no sha file
        bindings.add(binding1);

        Binding binding3 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar"), file3);
        bindings.add(binding3);

        Binding binding4 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so"), file4);
        bindings.add(binding4);

        Binding binding5 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"), file5);
        bindings.add(binding5);

        request.setFailFast(false);

        request.setBindings(bindings);
        RetrievalResponse response = retriever.retrieve(request);

        System.err.println("---------- testValidatorFail --------------------");
        for (HttpClientException t:response.getExceptions())
            t.printStackTrace();
        System.err.println("-------------------------------------------------");

        assertEquals(2,response.getExceptions().size());
        assertTrue(!file0.exists());
        assertTrue(!file1.exists());
        assertTrue(!file2.exists());
        assertTrue(!file3.exists());
        assertTrue(!file4.exists());
        assertTrue(!file5.exists());
    }
   
    public void testMemoryRetrieval () throws Exception
    {
        factories.add(new SHA1VerifierFactory(true, true)); //lenient, sufficient
        remoteServerType.setStreamObserverFactories(factories);

        //make local dir to put stuff in
        dir = mkTempDir();
        DefaultRetrievalRequest request = new DefaultRetrievalRequest();
        HashSet<Binding> bindings = new HashSet<Binding>();
        HashSet<Validator> validators = new HashSet<Validator>();
        validators.add(new TxtValidator());
        request.setValidators(validators);
        
        Binding binding0 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"));
        bindings.add(binding0);

        Binding binding5 = new Binding(new URL(__HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"));
        bindings.add(binding5);

        request.setFailFast(false);

        request.setBindings(bindings);
        RetrievalResponse response = retriever.retrieve(request);

        System.err.println("--------- testMemoryRetrieval -------------------");
        for (HttpClientException t:response.getExceptions())
            t.printStackTrace();
        System.err.println("-------------------------------------------------");

        assertEquals(0,response.getExceptions().size());
        
        
        InputStream is = this.getClass().getResourceAsStream("/testRepo/file0.txt");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IO.copy(is, os);
        assertEquals(os.toByteArray().length, binding0.getInboundContent().length);

        is = this.getClass().getResourceAsStream("/testRepo/file5.jpg");
        os.reset();
        IO.copy(is,os);
        assertEquals(os.toByteArray().length, binding5.getInboundContent().length);
    }
   
}
