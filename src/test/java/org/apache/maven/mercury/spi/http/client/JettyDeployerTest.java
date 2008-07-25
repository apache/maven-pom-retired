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

import junit.framework.TestCase;

import org.apache.maven.mercury.spi.http.client.Binding;
import org.apache.maven.mercury.spi.http.client.MercuryException;
import org.apache.maven.mercury.spi.http.client.deploy.DefaultDeployer;
import org.apache.maven.mercury.spi.http.client.deploy.DeployRequest;
import org.apache.maven.mercury.spi.http.client.deploy.DeployResponse;
import org.apache.maven.mercury.spi.http.server.SecurePutServer;
import org.apache.maven.mercury.spi.http.server.SimplePutServer;
import org.apache.maven.mercury.spi.http.validate.Validator;
import org.mortbay.jetty.Server;
import org.mortbay.util.IO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class JettyDeployerTest extends TestCase
{
    public String _HOST_FRAGMENT = "http://localhost:";
    public static final String __PATH_FRAGMENT = "/maven2/repo/";
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
    Binding _binding0 = new Binding();
    Binding _binding1 = new Binding();
    Binding _binding2 = new Binding();
    Binding _binding3 = new Binding();
    Binding _binding4 = new Binding();
    Binding _binding5 = new Binding();
    
    private class DeployRequestImpl implements DeployRequest
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
        _deployer = new DefaultDeployer();
        _putServer = new SimplePutServer();
        _putServer.start();
        _port = String.valueOf(_putServer.getPort());
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        _putServer.stop();
        _putServer.destroy();
        super.tearDown();
    }
    
    public void testUploadOKWithChecksums () throws Exception
    {
        HashSet<Binding> bindings = new HashSet<Binding>();
        DeployRequestImpl request = new DeployRequestImpl();
        
        _file0 = new File(_baseDir, "file0.txt");
        _file1 = new File(_baseDir, "file1.txt");
        _file2 = new File(_baseDir, "file2.txt");
        _file3 = new File(_baseDir, "file3.jar");
        _file4 = new File(_baseDir, "file4.so");
        _file5 = new File(_baseDir, "file5.jpg");
        
        _binding0.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
        _binding0.setLocalFile(_file0);
        bindings.add(_binding0);
      
        _binding3.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
        _binding3.setLocalFile(_file3);
        bindings.add(_binding3);
        
        _binding4.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
        _binding4.setLocalFile(_file4);
        bindings.add(_binding4);
       
        _binding5.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
        _binding5.setLocalFile(_file5);
        bindings.add(_binding5);
          
        request.setBindings(bindings);
        
        DeployResponse response = _deployer.deploy(request);

        for (MercuryException t:response.getExceptions())
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
    }
    
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
        
        _binding0.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
        _binding0.setLocalFile(_file0);
        bindings.add(_binding0);
        
        //TODO Test Lenient
        _binding1.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file1.txt"); //has no sha file
        _binding1.setLocalFile(_file1);
        //_binding1.setLenientChecksum(true);
        bindings.add(_binding1);
      
        _binding3.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
        _binding3.setLocalFile(_file3);
        bindings.add(_binding3);
        
        _binding4.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
        _binding4.setLocalFile(_file4);
        bindings.add(_binding4);
       
        _binding5.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
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
    
    public void testUploadFail () throws Exception 
    {
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
        
        _binding0.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
        _binding0.setLocalFile(_file0);
        bindings.add(_binding0);

        _binding3.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
        _binding3.setLocalFile(_file3);
        bindings.add(_binding3);

        _binding4.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
        _binding4.setLocalFile(_file4);
        bindings.add(_binding4);

        _binding5.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
        _binding5.setLocalFile(_file5);
        bindings.add(_binding5);

        Binding binding6 = new Binding();
        binding6.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file6.txt");
        binding6.setLocalFile(file6);
        bindings.add(binding6);
        
        Binding binding7 = new Binding();
        binding6.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file7.txt");
        binding6.setLocalFile(file7);
        bindings.add(binding7);
        
        request.setBindings(bindings);            
        DeployResponse response = _deployer.deploy(request);

        //for (BatchException t:response.getExceptions())
        //    t.printStackTrace();

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

        _binding0.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file0.txt");
        _binding0.setLocalFile(_file0);
        bindings.add(_binding0);

        _binding3.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file3.jar");
        _binding3.setLocalFile(_file3);
        bindings.add(_binding3);

        _binding4.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file4.so");
        _binding4.setLocalFile(_file4);
        bindings.add(_binding4);

        _binding5.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file5.jpg");
        _binding5.setLocalFile(_file5);
        bindings.add(_binding5);

        Binding binding6 = new Binding();
        binding6.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file6.txt");
        binding6.setLocalFile(file6);
        bindings.add(binding6);
        
        Binding binding7 = new Binding();
        binding6.setRemoteUrl(_HOST_FRAGMENT+_port+__PATH_FRAGMENT+"file7.txt");
        binding6.setLocalFile(file7);
        bindings.add(binding7);

        request.setBindings(bindings);     
        request.setFailFast(true);
        DeployResponse response = _deployer.deploy(request);

        //for (MercuryException t:response.getExceptions())
        //    t.printStackTrace();
        
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
}
