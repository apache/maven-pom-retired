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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.mercury.crypto.api.AbstractStreamVerifier;
import org.apache.maven.mercury.crypto.api.StreamVerifier;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.basic.ChecksumCalculator;


/**
 * SHA1Verifier
 *
 *
 */
public class SHA1Verifier
extends AbstractStreamVerifier
implements StreamVerifier
{
    public static final String digestAlgorithm = "SHA-1";

    private MessageDigest digest;
    private byte[] digestBytes;
    private long length  = -1;
    private String lastModified;
    
    private String sig;
    
    public SHA1Verifier( StreamVerifierAttributes attributes )
    {
      super( attributes );

      try
      {
          digest = MessageDigest.getInstance( digestAlgorithm );
      }
      catch (NoSuchAlgorithmException e)
      {
          //TODO
      }
    }

    private byte[] getSignatureBytes ()
    {
        if (digestBytes == null)
            digestBytes = digest.digest();
        return digestBytes;
    }
    
    public String getSignature()
    {
        return ChecksumCalculator.encodeToAsciiHex( getSignatureBytes() );
    }
    
    public void initSignature( String signatureString )
    throws StreamVerifierException
    {
      if( signatureString == null || signatureString.length() < 1 )
        throw new IllegalArgumentException("null signature stream");
      
      sig =  signatureString;
    
    }

    public boolean verifySignature()
    {
        String calculatedSignature = getSignature();

        if (calculatedSignature == null && sig == null)
            return true;

        if ((calculatedSignature != null) && calculatedSignature.equals(sig))
            return true;
        
        return false;
    }

    public void byteReady(int b)
    {
        if (digest != null)
            digest.update((byte)b);
    }

    public void bytesReady(byte[] b, int off, int len)
    {
        if (digest != null)
            digest.update(b, off, len);
    }
    //-----------------------------------------------------------------------------------
    public long getLength()
    {
        return length;
    }
    //-----------------------------------------------------------------------------------
    public void setLength(long length)
    {
        this.length = length;
    }

    public String getLastModified()
    {
        return lastModified;
    }

    public void setLastModified(String time)
    {
        lastModified = time;
    }
}
