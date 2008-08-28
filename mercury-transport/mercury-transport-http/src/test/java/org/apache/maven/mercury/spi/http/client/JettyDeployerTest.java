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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.spi.http.client.deploy.DefaultDeployer;
import org.apache.maven.mercury.spi.http.client.deploy.DeployRequest;
import org.apache.maven.mercury.spi.http.client.deploy.DeployResponse;
import org.apache.maven.mercury.spi.http.server.SimplePutServer;
import org.apache.maven.mercury.spi.http.validate.Validator;
import org.apache.maven.mercury.transport.api.Binding;
import org.mortbay.util.IO;

public class JettyDeployerTest extends TestCase
{
    public String _HOST_FRAGMENT = "http://localhost:";
    public static final String __PATH_FRAGMENT = "/maven2/repo/";

    private static final String keyId   = "0EDB5D91141BC4F2";

    private static final String secretKeyFile = "/pgp/secring.gpg";
    private static final String secretKeyPass = "testKey82";
    
    protected DefaultDeployer _deployer;
    protected SimplePutServer _putServer;
    protected String _port;
    File _baseDir;
    File _file0;
    File _file1;
    File _file2;
    File _file3;
    File _file4;
    File _file5;
    File _file6;

    org.apache.maven.mercury.transport.api.Server remoteServerType;
    HashSet<StreamVerifierFactory> factories;
    
    protected class DeployRequestImpl implements DeployRequest
    {
        private Set<Binding> _bindings = new HashSet<Binding>();
        private boolean _failFast;
        
        public Set<Validator> getValidators()
        {
            return null;
        }
        public void setBindings(Set<Binding> bindings)
        {
            _bindings=bindings;
        }
        public Set<Binding> getBindings()
        {
            return _bindings;
        }

        public boolean isFailFast()
        {
            return _failFast;
        }
        
        public void setFailFast (boolean f)
        {
            _failFast=f;
        }
    };
    
    public JettyDeployerTest () throws Exception
    {
       
    }
    
    public void setUpFiles () throws Exception
    {
        //copy the test files from the classpath to disk
        _baseDir = File.createTempFile("deployerTestFiles",null);
        _baseDir.delete();
        _baseDir.mkdir();
        _baseDir.deleteOnExit();       
        URL list = JettyDeployerTest.class.getResource("/testRepo/");
        LineNumberReader in = new LineNumberReader(new InputStreamReader(list.openStream()));
        String file=null;
        while ((file=in.readLine())!=null)
        {
            if (!file.startsWith("file"))
                continue;
            OutputStream out=new FileOutputStream(new File(_baseDir,file));
            IO.copy(JettyDeployerTest.class.getResource("/testRepo/"+file).openStream(),out);
            out.close();
        }
    }

    protected void setUp() throws Exception
    {        
        setUpFiles();
        _deployer = new DefaultDeployer();
        _putServer = new SimplePutServer();
        _putServer.start();
        _port = String.valueOf(_putServer.getPort());
        setUpServerType();
        super.setUp();
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
    
    protected void setUpServerType () throws Exception
    {
        HashSet<org.apache.maven.mercury.transport.api.Server> remoteServerTypes = new HashSet<org.apache.maven.mercury.transport.api.Server>();
        remoteServerType = new org.apache.maven.mercury.transport.api.Server( "test", new URL(_HOST_FRAGMENT+_port));
        factories = new HashSet<StreamVerifierFactory>();       
        remoteServerTypes.add(remoteServerType);
        _deployer.setServers(remoteServerTypes);
    }

    protected void tearDown() throws Exception
    {
        _putServer.stop();
        _putServer.destroy();        
        destroy(_baseDir);
        System.err.println("Destroyed "+_baseDir.getAbsolutePath());
        super.tearDown();
    }
    
    public void testUploadOKWithChecksums () throws Exception
    {
        HashSet<Binding> bindings = new HashSet<Binding>();
        DeployRequestImpl request = new DeployRequestImpl();
        factories.add( new SHA1VerifierFactory(false, true) ); //!lenient, sufficient
        factories.add( 
            new PgpStreamVerifierFactory(
                    new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                    , getClass().getResourceAsStream( secretKeyFile )
                    , keyId, secretKeyPass
                                        )
                      );
        remoteServerType.setWriterStreamVerifierFactories(factories);
        
        System.err.println("Basedir = "+_baseDir.getAbsolutePath());
        
        _file0 = new File(_baseDir, "file0.txt");
        _file1 = new File(_baseDir, "file1.txt");
        _file2 = new File(_baseDir, "file2.txt");
        _file3 = new File(_baseDir, "file3.jar");
        _file4 = new File(_baseDir, "file4.so");
        _file5 = new File(_baseDir, "file5.jpg");
        _file6 = new File(_baseDir, "file6.gif");
        Binding binding0 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"), _file0);
        Binding binding3 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar"), _file3);
        Binding binding4 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so"), _file4);
        Binding binding5 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"), _file5);      
        Binding binding6 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file6.gif"), _file6);      

        bindings.add(binding0);
        bindings.add(binding3);
        bindings.add(binding4);
        bindings.add(binding5);
        bindings.add(binding6);
          
        request.setBindings(bindings);
        
        DeployResponse response = _deployer.deploy(request);

        for (HttpClientException t:response.getExceptions())
            t.printStackTrace();
        
        assertEquals(0, response.getExceptions().size());
        File f0 = new File(_putServer.getPutDir(), "file0.txt");
        File f0cs = new File (_putServer.getPutDir(), "file0.txt.sha1");
        assertTrue (f0.exists());
        assertTrue (f0cs.exists());
      
        File f3 = new File(_putServer.getPutDir(), "file3.jar");
        File f3cs = new File (_putServer.getPutDir(), "file3.jar.sha1");
        assertTrue(f3.exists());
        assertTrue(f3cs.exists());
        
        File f4 = new File(_putServer.getPutDir(), "file4.so");
        File f4cs = new File (_putServer.getPutDir(), "file4.so.sha1");
        assertTrue (f4.exists());
        assertTrue (f4cs.exists());
        
        File f5 = new File(_putServer.getPutDir(), "file5.jpg");
        File f5cs = new File (_putServer.getPutDir(), "file5.jpg.sha1");
        assertTrue (f5.exists());
        assertTrue (f5cs.exists());
        
        File f6 = new File(_putServer.getPutDir(), "file6.gif");
        File f6cs = new File (_putServer.getPutDir(), "file6.gif.asc");
        assertTrue (f6.exists());
        assertTrue (f6cs.exists());
        
    }
    /* This test duplicates the one above unless we allow for checksum files to
     * be pre-existing
     
    public void testUploadOKMissingChecksum () throws Exception
    {
        HashSet<Binding> bindings = new HashSet<Binding>();
        DeployRequestImpl request = new DeployRequestImpl();
        
        _file0 = new File(_baseDir, "file0.txt");
        _file1 = new File(_baseDir, "file1.txt");
        _file2 = new File(_baseDir, "file2.txt");
        _file3 = new File(_baseDir, "file3.jar");
        _file4 = new File(_baseDir, "file4.so");
        _file5 = new File(_baseDir, "file5.jpg");
        
        _binding0.setRemoteResource(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
        _binding0.setLocalFile(_file0);
        bindings.add(_binding0);
        
        //TODO Test Lenient
        _binding1.setRemoteResource(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"); //has no sha file
        _binding1.setLocalFile(_file1);
        //_binding1.setLenientChecksum(true);
        bindings.add(_binding1);
      
        _binding3.setRemoteResource(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
        _binding3.setLocalFile(_file3);
        bindings.add(_binding3);
        
        _binding4.setRemoteResource(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
        _binding4.setLocalFile(_file4);
        bindings.add(_binding4);
       
        _binding5.setRemoteResource(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
        _binding5.setLocalFile(_file5);
        bindings.add(_binding5);
          
        request.setBindings(bindings);
        
        DeployResponse response = _deployer.deploy(request);

        //for (MercuryException t:response.getExceptions())
        //    t.printStackTrace();
        
        assertEquals(10, _putServer.getPutDir().list().length);
        assertEquals(0, response.getExceptions().size());
        File f0 = new File(_putServer.getPutDir(), "file0.txt");
        File f0cs = new File (_putServer.getPutDir(), "file0.txt.sha1");
        assertTrue (f0.exists());
        assertTrue (f0cs.exists());
        
        File f1 = new File(_putServer.getPutDir(), "file1.txt");
        File f1cs = new File(_putServer.getPutDir(), "file1.txt.sha1");
        assertTrue(f1.exists());
        assertTrue(f1cs.exists());
        
        File f3 = new File(_putServer.getPutDir(), "file3.jar");
        File f3cs = new File (_putServer.getPutDir(), "file3.jar.sha1");
        assertTrue(f3.exists());
        assertTrue(f3cs.exists());
        
        File f4 = new File(_putServer.getPutDir(), "file4.so");
        File f4cs = new File (_putServer.getPutDir(), "file4.so.sha1");
        assertTrue (f4.exists());
        assertTrue (f4cs.exists());
        
        File f5 = new File(_putServer.getPutDir(), "file5.jpg");
        File f5cs = new File (_putServer.getPutDir(), "file5.jpg.sha1");
        assertTrue (f5.exists());
        assertTrue (f5cs.exists());
    }
    */
    
   
    public void testUploadFail () throws Exception 
    {        
        factories.add(new SHA1VerifierFactory(false, true)); //!lenient, sufficient
        remoteServerType.setWriterStreamVerifierFactories(factories);
        HashSet<Binding> bindings = new HashSet<Binding>();
        DeployRequestImpl request = new DeployRequestImpl();

        _file0 = new File(_baseDir, "file0.txt");
        _file1 = new File(_baseDir, "file1.txt");
        _file2 = new File(_baseDir, "file2.txt");
        _file3 = new File(_baseDir, "file3.jar");
        _file4 = new File(_baseDir, "file4.so");
        _file5 = new File(_baseDir, "file5.jpg");
        File file6 = new File(_baseDir, "file6.txt");//doesn't exist
        File file7 = new File(_baseDir, "file7.txt");//doesn't exist
        
        Binding binding0 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"), _file0);
        bindings.add(binding0);

        Binding binding3 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar"), _file3);
        bindings.add(binding3);

        Binding binding4 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so"), _file4);
        bindings.add(binding4);

        Binding binding5 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"), _file5);
        bindings.add(binding5);

        Binding binding6 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file6.txt"), file6);
        bindings.add(binding6);
        
        Binding binding7 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file7.txt"), file7);
        bindings.add(binding7);
        
        request.setBindings(bindings);            
        DeployResponse response = _deployer.deploy(request);

//        for (HttpClientException t:response.getExceptions())
//            t.printStackTrace();

        //as the serverside is not running the mercury enhancements to the put filter, then
        //all the files except for the 2 which don't exists should have been uploaded
        assertEquals(2, response.getExceptions().size());
        File f0 = new File(_putServer.getPutDir(), "file0.txt");
        File f0cs = new File (_putServer.getPutDir(), "file0.txt.sha1");
        assertTrue (f0.exists());
        assertTrue (f0cs.exists());

        File f3 = new File(_putServer.getPutDir(), "file3.jar");
        File f3cs = new File (_putServer.getPutDir(), "file3.jar.sha1");
        assertTrue(f3.exists());
        assertTrue(f3cs.exists());

        File f4 = new File(_putServer.getPutDir(), "file4.so");
        File f4cs = new File (_putServer.getPutDir(), "file4.so.sha1");
        assertTrue (f4.exists());
        assertTrue (f4cs.exists());

        File f5 = new File(_putServer.getPutDir(), "file5.jpg");
        File f5cs = new File (_putServer.getPutDir(), "file5.jpg.sha1");
        assertTrue (f5.exists());
        assertTrue (f5cs.exists());

        File f6 = new File(_putServer.getPutDir(), "file6.txt");
        File f6cs = new File (_putServer.getPutDir(), "file6.txt.sha1");
        assertFalse (f6.exists());
        assertFalse (f6cs.exists());        
        
        File f7 = new File(_putServer.getPutDir(), "file7.txt");
        File f7cs = new File (_putServer.getPutDir(), "file7.txt.sha1");
        assertFalse (f7.exists());
        assertFalse (f7cs.exists());
    }
  
    public void testUploadFailFast () throws Exception 
    {
        factories.add(new SHA1VerifierFactory(false, true)); //!lenient, sufficient
        remoteServerType.setWriterStreamVerifierFactories(factories);
        HashSet<Binding> bindings = new HashSet<Binding>();
        DeployRequestImpl request = new DeployRequestImpl();

        _file0 = new File(_baseDir, "file0.txt");
        _file1 = new File(_baseDir, "file1.txt");
        _file2 = new File(_baseDir, "file2.txt");
        _file3 = new File(_baseDir, "file3.jar");
        _file4 = new File(_baseDir, "file4.so");
        _file5 = new File(_baseDir, "file5.jpg");
        File file6 = new File(_baseDir, "file6.txt");//doesn't exist
        File file7 = new File(_baseDir, "file7.txt");//doesn't exist

        Binding binding0 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"), _file0);
        bindings.add(binding0);

        Binding binding3 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar"), _file3);
        bindings.add(binding3);

        Binding binding4 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so"), _file4);
        bindings.add(binding4);

        Binding binding5 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"), _file5);
        bindings.add(binding5);

        Binding binding6 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file6.txt"), file6);
        bindings.add(binding6);
        
        Binding binding7 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file7.txt"), file7);
        bindings.add(binding7);

        request.setBindings(bindings);     
        request.setFailFast(true);
        DeployResponse response = _deployer.deploy(request);

//        for (HttpClientException t:response.getExceptions())
//            t.printStackTrace();
        
        //with failfast==true and the server side not running the mercury enhancements, we have no way to know
        //how many files actually did get uploaded, but the first exception should cause it to stop
        assertEquals(1, response.getExceptions().size());

        File f6 = new File(_putServer.getPutDir(), "file6.txt");
        File f6cs = new File (_putServer.getPutDir(), "file6.txt.sha1");
        assertFalse (f6.exists());
        assertFalse (f6cs.exists());        
        File f7 = new File(_putServer.getPutDir(), "file7.txt");
        File f7cs = new File (_putServer.getPutDir(), "file7.txt.sha1");
        assertFalse (f7.exists());
        assertFalse (f7cs.exists());
        
        
        Thread.sleep(500);
    }
    
    public void testMemoryDeployment () throws Exception
    {
        factories.add(new SHA1VerifierFactory(false, true)); //!lenient, sufficient
        remoteServerType.setWriterStreamVerifierFactories(factories);
        HashSet<Binding> bindings = new HashSet<Binding>();
        DeployRequestImpl request = new DeployRequestImpl();

        String s0 = "memory contents0";
        InputStream is0 = new ByteArrayInputStream(s0.getBytes());
        Binding binding0 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt"), is0);
        bindings.add(binding0);

        String s5 = "memory contents5";
        InputStream is5 = new ByteArrayInputStream(s5.getBytes());
        Binding binding5 = new Binding(new URL(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg"), is5);
        bindings.add(binding5);

        request.setBindings(bindings);     
        request.setFailFast(true);
        DeployResponse response = _deployer.deploy(request);

//        for (HttpClientException t:response.getExceptions())
//            t.printStackTrace();
        
  
        assertEquals(0, response.getExceptions().size());

        
        File f0 = new File(_putServer.getPutDir(), "file0.txt");
        File f0cs = new File (_putServer.getPutDir(), "file0.txt.sha1");
        assertTrue (f0.exists());
        BufferedReader reader = new BufferedReader(new FileReader(f0));
        String s = reader.readLine();
        reader.close();
        assertEquals(s0, s.trim());
        assertTrue (f0cs.exists()); 
        
        File f5 = new File(_putServer.getPutDir(), "file5.jpg");
        File f5cs = new File (_putServer.getPutDir(), "file5.jpg.sha1");
        assertTrue (f5.exists());
        reader = new BufferedReader(new FileReader(f5));
        s = reader.readLine();
        reader.close();
        assertEquals(s5, s.trim());
        assertTrue (f5cs.exists());  
    }
}
