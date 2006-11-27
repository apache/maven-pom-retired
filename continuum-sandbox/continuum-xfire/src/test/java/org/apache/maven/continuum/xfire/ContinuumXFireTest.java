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

import java.util.Collection;

import org.apache.maven.continuum.project.ContinuumProject;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.plexus.PlexusXFireTest;
import org.codehaus.xfire.plexus.config.ConfigurationService;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.ServiceRegistry;
import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.transport.Channel;
import org.codehaus.xfire.transport.local.LocalTransport;
import org.codehaus.xfire.wsdl.WSDLWriter;
import org.codehaus.yom.Document;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumXFireTest
    extends PlexusXFireTest
{
    public void testService() throws Exception
    {
        lookup(ConfigurationService.ROLE);
        
        Service clientService = getXFire().getServiceRegistry().getService("Continuum");
        
        ServiceRegistry reg = getXFire().getServiceRegistry();
        LocalTransport transport = new LocalTransport();
        Service service = reg.getService("Continuum");
        Channel channel = transport.createChannel(service);
        
        XFireProxyFactory proxy = new XFireProxyFactory();
        ContinuumWebService ws = (ContinuumWebService) proxy.create(transport, clientService, channel.getUri());

        Project project = new Project();
        project.setScmUrl("scm:local:file://../continuum-test-projects/shell");
        project.setExecutable("script.sh");
        project.setType("shell");
        
        String id = ws.addProject(project);
        
        Project p2 = ws.getProject(id);
        assertNotNull(p2.getId());
        assertEquals(project.getType(), p2.getType());
        assertEquals(project.getUrl(), p2.getUrl());
        assertEquals(project.getScmUrl(), p2.getScmUrl());
        assertEquals(project.getExecutable(), p2.getExecutable());
        
        ScmResult result = ws.getScmResult(id);

        Collection builds = ws.getBuilds(id);
        assertEquals(0, builds.size());
        
        Build build = ws.getLatestBuild(id);

        ws.removeProject(id);
        
        try
        {
            ws.getProject(id);
            fail("xfire fault should have been thrown.");
        }
        catch (XFireFault fault)
        {
        }
        
        String url = getTestFile( "../continuum-test-projects/m2/pom.xml" ).toURL().toExternalForm();
        Collection projects = ws.addMavenTwoProject(url);
        assertEquals(1, projects.size());
        Project m2 = (Project) projects.iterator().next();
        assertNotNull(m2.getId());
    }
    
    public void testWSDL()
        throws Exception
    {
        lookup(ConfigurationService.ROLE);

        ContinuumProject project = null;

        Document doc = getWSDLDocument("Continuum");
        
        // printNode(doc);
        
        addNamespace("wsdl", WSDLWriter.WSDL11_NS);
        addNamespace("wsdlsoap", WSDLWriter.WSDL11_SOAP_NS);
        addNamespace("xsd", SoapConstants.XSD);
        
        // rudimentary check for operations
        assertValid("//wsdl:operation[@name='addProject']", doc);
        assertValid("//wsdl:operation[@name='updateProject']", doc);
        assertValid("//wsdl:operation[@name='removeProject']", doc);
    }
}
