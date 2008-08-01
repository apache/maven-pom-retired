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

package org.apache.maven.mercury.transport;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.mercury.transport.api.Verifier;

/**
 * SHA1Verifier
 *
 *
 */
public class SHA1Verifier implements Verifier
{
    public static final String digestAlgorithm = "SHA-1";
    private MessageDigest digest;
    private byte[] digestBytes;
    private boolean isLenient;
    private boolean isSufficient;
    
    
    public SHA1Verifier (boolean isLenient, boolean isSufficient)
    {
        this.isLenient = isLenient;
        this.isSufficient = isSufficient;
        try
        {
            digest = MessageDigest.getInstance( digestAlgorithm );
        }
        catch (NoSuchAlgorithmException e)
        {
            //TODO
        }
    }

    public String getExtension()
    {
        return ".sha1";
    }

    public byte[] getSignatureBytes ()
    {
        if (digestBytes == null)
            digestBytes = digest.digest();
        return digestBytes;
    }
    
    public String getSignature()
    {
        return ChecksumCalculator.encodeToAsciiHex( getSignatureBytes() );
    }
    
    public void setLenient (boolean lenient)
    {
        isLenient = lenient;
    }

    public boolean isLenient()
    {
        return isLenient;
    }

    public void setSufficient (boolean sufficient)
    {
        isSufficient = sufficient;
    }
    public boolean isSufficient()
    {
        return isSufficient;
    }

    public boolean verifySignature(String sig)
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

}
