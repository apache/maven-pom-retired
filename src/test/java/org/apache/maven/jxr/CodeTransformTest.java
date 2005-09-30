package org.apache.maven.jxr;

import junit.framework.TestCase;
import org.apache.maven.jxr.pacman.PackageManager;

import java.io.File;

public class CodeTransformTest
    extends TestCase
{

    private CodeTransform codeTransform;

    private PackageManager packageManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        packageManager = new PackageManager();
        codeTransform = new CodeTransform( packageManager );
    }

    public void testTransform()
        throws Exception
    {
        File sourceFile = new File(
            System.getProperty( "basedir" ) + "/src/test/org/apache/maven/jxr/CodeTransformTest.java" );
        assertTrue( sourceFile.exists() );
        codeTransform.transform( sourceFile.getAbsolutePath(),
                                 System.getProperty( "basedir" ) + "/target/CodeTransformTest.html", "en", "ISO-8859-1",
                                 "ISO-8859-1", "", "" );
        //        sourceFile = new File("src/test/org/apache/maven/jxr/package-info.java");
        //        assertTrue(sourceFile.exists());
        //        codeTransform.transform(sourceFile.getAbsolutePath(),
        //                "target/pakage-info.html", "en", "ISO-8859-1", "ISO-8859-1",
        //                "", "");
    }

}
