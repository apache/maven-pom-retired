/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.maven.mercury.repository.cache.fs;

import java.io.File;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.maven.mercury.repository.api.RepositoryGAMetadata;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.util.FileUtil;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class CachedGATest
    extends TestCase
{
  
  byte [] mdBytes;
  
  CachedGAMetadata gam;
  
  Metadata omd;

  @Override
  protected void setUp()
  throws Exception
  {
    InputStream is = CachedGATest.class.getResourceAsStream( "/ga-metadata.xml" );
    
    mdBytes = FileUtil.readRawData( is );
    
    omd = MetadataBuilder.getMetadata( mdBytes );
    
    gam = new CachedGAMetadata( new RepositoryGAMetadata(omd) );
  }
  
  public void testData()
  throws Exception
  {
    assertEquals( omd.getGroupId(), gam.getGA().getGroupId() );
    assertEquals( omd.getArtifactId(), gam.getGA().getArtifactId() );
    
    assertEquals( omd.getVersioning().getVersions().size(), gam.getVersions().size() );
  }
  
  public void testRead()
  throws Exception
  {
    File mf = File.createTempFile( "test-ga-", ".xml", new File("./target") );
    gam.cm.save( mf );
    
    CachedGAMetadata gam2 = new CachedGAMetadata( mf );

    assertEquals( omd.getGroupId(), gam2.getGA().getGroupId() );
    assertEquals( omd.getArtifactId(), gam2.getGA().getArtifactId() );
    
    assertEquals( omd.getVersioning().getVersions().size(), gam2.getVersions().size() );
  }
  
  
}
