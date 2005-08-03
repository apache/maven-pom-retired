package org.apache.maven.continuum.web.it;

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

import org.codehaus.plexus.context.Context;

import com.meterware.httpunit.WebTable;
import com.meterware.httpunit.TableCell;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import net.sourceforge.jwebunit.WebTestCase;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractIntegrationWebTest
    extends AbstractPlexusWebTest
{
    private static final DateFormat progressDateFormat = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss" );

    private Date startTime;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void setUp()
        throws Exception
    {
        startTime = new Date();

        super.setUp();
    }

    public void tearDown()
        throws Exception
    {
        Date endTime = new Date();

        super.tearDown();

        long diff = endTime.getTime() - startTime.getTime();

        progress( "Used " + diff + "ms" );
    }

    public String getBaseUrl()
    {
        return "http://localhost:8080/continuum/servlet/continuum";
    }

    // ----------------------------------------------------------------------
    // Logging
    // ----------------------------------------------------------------------

    public static void progress( String message )
    {
        System.out.println( "[" + progressDateFormat.format( new Date() ) + "] " + message );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void addAntProject( String targets, String executable, String projectName, String scmUrl, String version )
    {
        beginAt( "/" );
        clickLinkWithText( "Add Ant Project" );
        assertTitleEquals( "Add Project" );
        assertTextPresent( "Targets" );
        setFormElement( "project.targets", targets );
        setFormElement( "project.executable", executable );
        setFormElement( "project.name", projectName );
        setFormElement( "projectScmUrl", scmUrl );
        setFormElement( "project.version", version );
        submit();
    }

    public void addMavenOneProject( String pomUrl )
    {
        beginAt( "/" );
        clickLinkWithText( "Add M1 Project" );
        assertTitleEquals( "Add Project" );
        assertTextPresent( "M1 POM Url" );
        setFormElement( "m1PomUrl", pomUrl );
        submit();
    }

    public void addMavenTwoProject( String pomUrl )
    {
        beginAt( "/" );
        clickLinkWithText( "Add M2 Project" );
        assertTitleEquals( "Add Project" );
        assertTextPresent( "M2 POM Url" );
        setFormElement( "m2PomUrl", pomUrl );
        submit();
    }

    public void addShellProject( String executable, String projectName, String scmUrl, String version )
    {
        beginAt( "/" );
        clickLinkWithText( "Add Shell Project" );
        assertTitleEquals( "Add Project" );
        assertTextPresent( "Executable" );
        assertTextNotPresent( "Targets" );
        setFormElement( "project.executable", executable );
        setFormElement( "project.name", projectName );
        setFormElement( "projectScmUrl", scmUrl );
        setFormElement( "project.version", version );
        submit();
    }

    public void uploadMavenOneProject( String pomUrl )
    {
        beginAt( "/" );
        clickLinkWithText( "Upload M1 Project" );
        assertTitleEquals( "Add Project" );
        assertTextPresent( "M1 POM Url" );
        setFormElement( "m1PomUrl", pomUrl );
        submit();
    }

    public void uploadMavenTwoProject( String pomUrl )
    {
        beginAt( "/" );
        clickLinkWithText( "Upload M2 Project" );
        assertTitleEquals( "Add Project" );
        assertTextPresent( "M2 POM Url" );
        setFormElement( "m2PomUrl", pomUrl );
        submit();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

	protected String getNodeHtml(Node node) {
		String nodeHtml = "";
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				nodeHtml += "<" + child.getNodeName() + ">";
			}
			if (child.hasChildNodes()) {
				nodeHtml += getNodeHtml(child);
			} else {
				nodeHtml += child.getNodeValue();
			}
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				nodeHtml += "</" + child.getNodeName() + ">";
			}
		}
		return getTestContext().toEncodedString(nodeHtml);
	}

	/**
	 * Return the text (without any markup) of the tree rooted at node.
	 */
	protected String getNodeText(Node node) {
		String nodeText = "";
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.hasChildNodes()) {
				nodeText += getNodeText(child);
			} else if (child.getNodeType() == Node.TEXT_NODE) {
				nodeText += ((Text) child).getData();
			}
		}
		return nodeText;
	}

    public void assertTextInTable( String tableSummaryOrId, String text, int row, int column )
    {
		WebTable table = getDialog().getWebTableBySummaryOrId( tableSummaryOrId );
		assertNotNull( "table [" + tableSummaryOrId + "] doesn't exist", table );
		TableCell cell = table.getTableCell( row, column );
		assertNotNull( "cell [" + row + "," + column + "] in table [" + tableSummaryOrId + "] doesn't exist", table );
		if ( cell != null )
		{
			String cellHtml = getNodeHtml( cell.getDOM() );
			assertEquals( "Invalid text in cell [" + row + "," + column + "] in table " + tableSummaryOrId, text, cellHtml );
		}
    }
}
