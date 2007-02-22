package org.apache.maven.continuum.web.action;

/*
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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Edwin Punzalan
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="surefireReport"
 * @todo too many inner classes, maybe a continuum-reports project group ?
 */
public class SurefireReportAction
    extends ContinuumActionSupport
{
    private int buildId;

    private int projectId;

    private List testSuites;

    private List testSummaryList;

    private List testPackageList;

    private String projectName;

    private Project project;

    public String execute()
        throws ContinuumException
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        project = getProjectById( projectId );

        //@todo maven-surefire-report reportsDirectory should be detected ?
        File reportsDirectory = new File( project.getWorkingDirectory() + "/target/surefire-reports" );

        parseReports( reportsDirectory );

        getSummary( testSuites );

        getDetails( testSuites );

        return SUCCESS;
    }

    private void parseReports( File reportsDirectory )
        throws ContinuumException
    {
        String[] xmlReportFiles = getIncludedFiles( reportsDirectory, "*.xml", "*.txt" );

        testSuites = new ArrayList();

        for ( int index = 0; index < xmlReportFiles.length; index++ )
        {
            ReportTestSuite testSuite = new ReportTestSuite();

            String currentReport = xmlReportFiles[index];

            try
            {
                testSuite.parse( reportsDirectory + "/" + currentReport );
            }
            catch ( ParserConfigurationException e )
            {
                throw new ContinuumException( "Error setting up parser for Surefire XML report", e );
            }
            catch ( SAXException e )
            {
                throw new ContinuumException( "Error parsing Surefire XML report " + currentReport, e );
            }
            catch ( IOException e )
            {
                throw new ContinuumException( "Error reading Surefire XML report " + currentReport, e );
            }

            testSuites.add( testSuite );
        }
    }

    private void getSummary( List suiteList )
    {
        int totalTests = 0;

        int totalErrors = 0;

        int totalFailures = 0;

        float totalTime = 0.0f;

        for ( Iterator suites = suiteList.iterator(); suites.hasNext(); )
        {
            ReportTestSuite suite = (ReportTestSuite) suites.next();

            totalTests += suite.getNumberOfTests();

            totalErrors += suite.getNumberOfErrors();

            totalFailures += suite.getNumberOfFailures();

            totalTime += suite.getTimeElapsed();
        }

        ReportTest report = new ReportTest();
        report.setTests( totalTests );
        report.setErrors( totalErrors );
        report.setFailures( totalFailures );
        report.setElapsedTime( totalTime );

        testSummaryList = Collections.singletonList( report );
    }

    private void getDetails( List suiteList )
    {
        Map testsByPackage = new LinkedHashMap();

        for ( Iterator suites = suiteList.iterator(); suites.hasNext(); )
        {
            ReportTestSuite suite = (ReportTestSuite) suites.next();

            ReportTest report = (ReportTest) testsByPackage.get( suite.getPackageName() );

            if ( report == null )
            {
                report = new ReportTest();

                report.setId( suite.getPackageName() );

                report.setName( suite.getPackageName() );
            }

            report.setTests( report.getTests() + suite.getNumberOfTests() );
            report.setErrors( report.getErrors() + suite.getNumberOfErrors() );
            report.setFailures( report.getFailures() + suite.getNumberOfFailures() );
            report.setElapsedTime( report.getElapsedTime() + suite.getTimeElapsed() );

            ReportTest reportTest = new ReportTest();
            reportTest.setId( suite.getPackageName() + "." + suite.getName() );
            reportTest.setName( suite.getName() );
            reportTest.setTests( suite.getNumberOfTests() );
            reportTest.setErrors( suite.getNumberOfErrors() );
            reportTest.setFailures( suite.getNumberOfFailures() );
            reportTest.setElapsedTime( suite.getTimeElapsed() );
            reportTest.setChildren( suite.getTestCases() );

            report.getChildren().add( reportTest );

            testsByPackage.put( suite.getPackageName(), report );
        }

        testPackageList = new ArrayList( testsByPackage.values() );
    }

    public int getBuildId()
    {
        return buildId;
    }

    public void setBuildId( int buildId )
    {
        this.buildId = buildId;
    }

    public Project getProject()
    {
        return project;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    private String[] getIncludedFiles( File directory, String includes, String excludes )
    {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir( directory );

        scanner.setIncludes( StringUtils.split( includes, "," ) );

        scanner.setExcludes( StringUtils.split( excludes, "," ) );

        scanner.scan();

        return scanner.getIncludedFiles();
    }

    public List getTestSuites()
    {
        return testSuites;
    }

    public void setTestSuites( List testSuites )
    {
        this.testSuites = testSuites;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public List getTestSummaryList()
    {
        return testSummaryList;
    }

    public void setTestSummaryList( List testSummaryList )
    {
        this.testSummaryList = testSummaryList;
    }

    public List getTestPackageList()
    {
        return testPackageList;
    }

    public void setTestPackageList( List testPackageList )
    {
        this.testPackageList = testPackageList;
    }

    public class ReportTest
    {
        private String id;

        private String name;

        private int tests;

        private int errors;

        private int failures;

        private float elapsedTime;

        private List children;

        public String getName()
        {
            return name;
        }

        public void setName( String name )
        {
            this.name = name;
        }

        public int getTests()
        {
            return tests;
        }

        public void setTests( int tests )
        {
            this.tests = tests;
        }

        public int getErrors()
        {
            return errors;
        }

        public void setErrors( int errors )
        {
            this.errors = errors;
        }

        public int getFailures()
        {
            return failures;
        }

        public void setFailures( int failures )
        {
            this.failures = failures;
        }

        public float getSuccessRate()
        {
            float percentage;
            if ( tests == 0 )
            {
                percentage = 0;
            }
            else
            {
                percentage = ( (float) ( tests - errors - failures ) / (float) tests ) * 100;
            }

            return percentage;
        }

        public float getElapsedTime()
        {
            return elapsedTime;
        }

        public void setElapsedTime( float elapsedTime )
        {
            this.elapsedTime = elapsedTime;
        }

        public List getChildren()
        {
            if ( children == null )
            {
                children = new ArrayList();
            }

            return children;
        }

        public void setChildren( List children )
        {
            this.children = children;
        }

        public String getId()
        {
            return id;
        }

        public void setId( String id )
        {
            this.id = id;
        }
    }

    /**
     * Taken from maven-surefire-report-plugin
     */
    private class ReportTestSuite
        extends DefaultHandler
    {
        private List testCases;

        private int numberOfErrors;

        private int numberOfFailures;

        private int numberOfTests;

        private String name;

        private String fullClassName;

        private String packageName;

        private float timeElapsed;

        private NumberFormat numberFormat = NumberFormat.getInstance();

        /**
         * @noinspection StringBufferField
         */
        private StringBuffer currentElement;

        private ReportTestCase testCase;

        public void parse( String xmlPath )
            throws ParserConfigurationException, SAXException, IOException
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();

            SAXParser saxParser = factory.newSAXParser();

            saxParser.parse( new File( xmlPath ), this );
        }

        public void startElement( String uri, String localName, String qName, Attributes attributes )
            throws SAXException
        {
            try
            {
                if ( "testsuite".equals( qName ) )
                {
                    numberOfErrors = Integer.parseInt( attributes.getValue( "errors" ) );

                    numberOfFailures = Integer.parseInt( attributes.getValue( "failures" ) );

                    numberOfTests = Integer.parseInt( attributes.getValue( "tests" ) );

                    Number time = numberFormat.parse( attributes.getValue( "time" ) );

                    timeElapsed = time.floatValue();

                    //check if group attribute is existing
                    if ( attributes.getValue( "group" ) != null && !"".equals( attributes.getValue( "group" ) ) )
                    {
                        packageName = attributes.getValue( "group" );

                        name = attributes.getValue( "name" );

                        fullClassName = packageName + "." + name;
                    }
                    else
                    {
                        fullClassName = attributes.getValue( "name" );

                        name = fullClassName.substring( fullClassName.lastIndexOf( "." ) + 1, fullClassName.length() );

                        int lastDotPosition = fullClassName.lastIndexOf( "." );
                        if ( lastDotPosition < 0 )
                        {
                            /* no package name */
                            packageName = "";
                        }
                        else
                        {
                            packageName = fullClassName.substring( 0, lastDotPosition );
                        }
                    }

                    testCases = new ArrayList();
                }
                else if ( "testcase".equals( qName ) )
                {
                    currentElement = new StringBuffer();

                    testCase = new ReportTestCase();

                    testCase.setFullClassName( fullClassName );

                    testCase.setName( attributes.getValue( "name" ) );

                    testCase.setClassName( name );

                    String timeAsString = attributes.getValue( "time" );

                    Number time = new Integer( 0 );

                    if ( timeAsString != null )
                    {
                        time = numberFormat.parse( timeAsString );
                    }

                    testCase.setTime( time.floatValue() );

                    testCase.setFullName( packageName + "." + name + "." + testCase.getName() );
                }
                else if ( "failure".equals( qName ) )
                {
                    testCase.setFailureType( attributes.getValue( "type" ) );
                    testCase.setFailureMessage( attributes.getValue( "message" ) );
                }
                else if ( "error".equals( qName ) )
                {
                    testCase.setFailureType( attributes.getValue( "type" ) );
                    testCase.setFailureMessage( attributes.getValue( "message" ) );
                }
            }
            catch ( ParseException e )
            {
                throw new SAXException( e.getMessage(), e );
            }
        }

        public void endElement( String uri, String localName, String qName )
            throws SAXException
        {
            if ( "testcase".equals( qName ) )
            {
                testCases.add( testCase );
            }
            else if ( "failure".equals( qName ) )
            {
                testCase.setFailureDetails( currentElement.toString() );
            }
            else if ( "error".equals( qName ) )
            {
                testCase.setFailureDetails( currentElement.toString() );
            }
        }

        public void characters( char[] ch, int start, int length )
            throws SAXException
        {
            String s = new String( ch, start, length );

            if ( !"".equals( s.trim() ) )
            {
                currentElement.append( s );
            }
        }

        public List getTestCases()
        {
            return this.testCases;
        }

        public int getNumberOfErrors()
        {
            return numberOfErrors;
        }

        public void setNumberOfErrors( int numberOfErrors )
        {
            this.numberOfErrors = numberOfErrors;
        }

        public int getNumberOfFailures()
        {
            return numberOfFailures;
        }

        public void setNumberOfFailures( int numberOfFailures )
        {
            this.numberOfFailures = numberOfFailures;
        }

        public int getNumberOfTests()
        {
            return numberOfTests;
        }

        public void setNumberOfTests( int numberOfTests )
        {
            this.numberOfTests = numberOfTests;
        }

        public String getName()
        {
            return name;
        }

        public void setName( String name )
        {
            this.name = name;
        }

        public String getFName()
        {
            return name;
        }

        public void setFName( String name )
        {
            this.name = name;
        }

        public String getPackageName()
        {
            return packageName;
        }

        public void setPackageName( String packageName )
        {
            this.packageName = packageName;
        }

        public float getTimeElapsed()
        {
            return this.timeElapsed;
        }

        public void setTimeElapsed( float timeElapsed )
        {
            this.timeElapsed = timeElapsed;
        }

        private List parseCause( String detail )
        {
            String fullName = testCase.getFullName();
            String name = fullName.substring( fullName.lastIndexOf( "." ) + 1 );
            return parseCause( detail, name );
        }

        private List parseCause( String detail, String compareTo )
        {
            StringTokenizer stringTokenizer = new StringTokenizer( detail, "\n" );
            List parsedDetail = new ArrayList( stringTokenizer.countTokens() );

            while ( stringTokenizer.hasMoreTokens() )
            {
                String lineString = stringTokenizer.nextToken().trim();
                parsedDetail.add( lineString );
                if ( lineString.indexOf( compareTo ) >= 0 )
                {
                    break;
                }
            }

            return parsedDetail;
        }

        public void setTestCases( List testCases )
        {
            this.testCases = Collections.unmodifiableList( testCases );
        }
    }

    /**
     * Taken from maven-surefire-report-plugin
     */
    public class ReportTestCase
    {
        private String fullClassName;

        private String className;

        private String fullName;

        private String name;

        private float time;

        private String failureType;

        private String failureMessage;

        private String failureDetails;

        public String getName()
        {
            return name;
        }

        public void setName( String name )
        {
            this.name = name;
        }

        public String getFullClassName()
        {
            return fullClassName;
        }

        public void setFullClassName( String name )
        {
            this.fullClassName = name;
        }

        public String getClassName()
        {
            return className;
        }

        public void setClassName( String name )
        {
            this.className = name;
        }

        public float getTime()
        {
            return time;
        }

        public void setTime( float time )
        {
            this.time = time;
        }

        public String getFullName()
        {
            return fullName;
        }

        public void setFullName( String fullName )
        {
            this.fullName = fullName;
        }

        public String getFailureType()
        {
            return failureType;
        }

        public void setFailureType( String failureType )
        {
            this.failureType = failureType;
        }

        public String getFailureMessage()
        {
            return failureMessage;
        }

        public void setFailureMessage( String failureMessage )
        {
            this.failureMessage = failureMessage;
        }

        public String getFailureDetails()
        {
            return failureDetails;
        }

        public void setFailureDetails( String failureDetails )
        {
            this.failureDetails = failureDetails;
        }
    }

    public Project getProjectById( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProject( projectId );
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        return getProjectById( projectId ).getProjectGroup().getName();
    }
}
