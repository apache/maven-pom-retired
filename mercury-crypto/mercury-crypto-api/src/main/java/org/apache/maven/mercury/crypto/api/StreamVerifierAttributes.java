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
package org.apache.maven.mercury.crypto.api;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class StreamVerifierAttributes
{
  protected boolean isLenient = true;
  protected boolean isSufficient = false;
  protected String  extension = "none";
  protected String  digestAlgorithm = "SHA-1";
  
  /**
   * 
   */
  public StreamVerifierAttributes( String extension, boolean isLenient, boolean isSufficient)
  {
    this.extension = extension;
    this.isLenient = isLenient;
    this.isSufficient = isSufficient;
  }
  
  /**
   * 
   */
  public StreamVerifierAttributes()
  {
  }

  public boolean isLenient()
  {
      return isLenient;
  }

  public boolean isSufficient()
  {
      return isSufficient;
  }

  public String getExtension()
  {
    return extension == null
             ? extension
             : extension.startsWith( "." )
                         ? extension 
                         : "."+extension
           ;
  }
  
  
}
