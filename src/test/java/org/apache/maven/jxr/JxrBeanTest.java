package org.apache.maven.jxr;

import junit.framework.TestCase;

public class JxrBeanTest
    extends TestCase
{

    private JxrBean jxrBean;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        jxrBean = new JxrBean();
        jxrBean.setSourceDir( System.getProperty( "basedir" ) + "/src/test" );
        jxrBean.setDestDir( System.getProperty( "basedir" ) + "/target" );
        jxrBean.setInputEncoding( "ISO-8859-1" );
        jxrBean.setOutputEncoding( "ISO-8859-1" );
        jxrBean.setTemplateDir( System.getProperty( "basedir" ) + "/src/plugin-resources/templates" );
        jxrBean.setJavadocDir( "" );
        jxrBean.setWindowTitle( "title" );
        jxrBean.setDocTitle( "title" );
        jxrBean.setBottom( "copyright" );
    }

    public void testXref()
        throws Exception
    {
        jxrBean.xref();
    }

}
