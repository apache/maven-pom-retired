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

package org.apache.maven.mercury.crypto.sha;

import org.apache.maven.mercury.crypto.api.AbstractStreamVerifierFactory;
import org.apache.maven.mercury.crypto.api.StreamVerifier;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;

public class SHA1VerifierFactory
extends AbstractStreamVerifierFactory
implements StreamVerifierFactory
{
  public static final String DEFAULT_EXTENSION = "sha1";
  
  public SHA1VerifierFactory( StreamVerifierAttributes attrs )
  {
    super( attrs );
  }
  
  public SHA1VerifierFactory( boolean lenient, boolean satisfactory )
  {
    super( new StreamVerifierAttributes( DEFAULT_EXTENSION, lenient, satisfactory ) );
  }
  
  public StreamVerifier newInstance()
  {
     return new SHA1Verifier( attributes );
  }

  public String getDefaultExtension()
  {
    return DEFAULT_EXTENSION;
  }

}
