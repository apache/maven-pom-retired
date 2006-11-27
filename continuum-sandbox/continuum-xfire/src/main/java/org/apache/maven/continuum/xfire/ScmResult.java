package org.apache.maven.continuum.xfire;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.List;

public class ScmResult
{
    private List files;
    private boolean success;
    private String providerMessage;
    private String commandOutput;
    
    public List getFiles()
    {
        return files;
    }
    public void setFiles(List files)
    {
        this.files = files;
    }
    public String getCommandOutput()
    {
        return commandOutput;
    }
    public void setCommandOutput(String commandOutput)
    {
        this.commandOutput = commandOutput;
    }
    public String getProviderMessage()
    {
        return providerMessage;
    }
    public void setProviderMessage(String providerMessage)
    {
        this.providerMessage = providerMessage;
    }
    public boolean isSuccess()
    {
        return success;
    }
    public void setSuccess(boolean success)
    {
        this.success = success;
    }
}
