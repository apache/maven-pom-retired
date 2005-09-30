package org.apache.maven.jxr.ant;

import java.io.File;

/**
 * Workaround to ignore package-info.java files.
 *
 * @author Carlos Sanchez
 */
public class DirectoryScanner
    extends org.apache.tools.ant.DirectoryScanner
{

    public void addDefaultExcludes()
    {
        super.addDefaultExcludes();
        int excludesLength = excludes == null ? 0 : excludes.length;
        String[] newExcludes;
        newExcludes = new String[excludesLength + 1];
        if ( excludesLength > 0 )
        {
            System.arraycopy( excludes, 0, newExcludes, 0, excludesLength );
        }
        newExcludes[excludesLength] = "**" + File.separatorChar + "package-info.java";
        excludes = newExcludes;
    }

}
