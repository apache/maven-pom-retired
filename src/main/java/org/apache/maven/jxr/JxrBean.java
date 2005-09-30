package org.apache.maven.jxr;

/* ====================================================================
 *   Copyright 2001-2004 The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * ====================================================================
 */

import org.apache.maven.jxr.JXR;
import org.apache.maven.jxr.DirectoryIndexer;
import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.apache.maven.jxr.pacman.PackageType;
import org.apache.maven.jxr.pacman.ClassType;
import java.util.Enumeration;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates an html-based, cross referenced  version of Java source code
 * for a project.
 *
 * @author <a href="mailto:lucas@collab.net">Josh Lucas</a>
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:brian@brainslug.com">Brian Leonard</a>
 * @version $Id$
 */
public class JxrBean
{
    /** Log. */
    private static final Log log = LogFactory.getLog(JxrBean.class);

    /*
     * See the doc comments for the corresponding getter/setter methods
     */
    private List sourceDirs;
    private String destDir;
    private String lang;
    private String inputEncoding;
    private String outputEncoding;
    private String javadocDir;
    private String windowTitle;
    private String docTitle;
    private String bottom;
    private String templateDir;

    /**
     * Default constructor
     */
    public JxrBean()
    {
        sourceDirs = new LinkedList();
    }

    /**
     * Starts the cross-referencing and indexing.
     *
     * @throws IOException when any error occurs
     */
    public void xref()
        throws Exception
    {
        // get a relative link to the javadocs
        String javadocLinkDir = null;
        if (javadocDir != null) {
            javadocLinkDir = getRelativeLink(destDir, javadocDir);
        }
        
        // first collect package and class info
        PackageManager pkgmgr = new PackageManager();

        FileManager.getInstance().setEncoding(inputEncoding);
        
        // go through each source directory and xref the java files
        for (Iterator i = sourceDirs.iterator(); i.hasNext();)
        {
            String path = (String) i.next();
            path = new File(path).getCanonicalPath();
            
            pkgmgr.process(path);
            
            new JXR(pkgmgr, path, destDir, lang, inputEncoding, outputEncoding, javadocLinkDir, "HEAD");
        }
        
        // once we have all the source files xref'd, create the index pages
        DirectoryIndexer indexer = new DirectoryIndexer(pkgmgr, destDir);
        indexer.setOutputEncoding(outputEncoding);
        indexer.setTemplateDir(getTemplateDir());
        indexer.setWindowTitle(getWindowTitle());
        indexer.setDocTitle(getDocTitle());
        indexer.setBottom(getBottom());
        indexer.process();
    }
    
    /**
     * Creates a relative link from one directory to another.
     *
     * Example:
     *   given /foo/bar/baz/oink
     *     and /foo/bar/schmoo
     *
     * this method will return a string of "../../schmoo/"
     *
     * @param fromDir The directory from which the link is relative.
     * @param toDir   The directory into which the link points.
     * @throws IOException
     *    If a problem is encountered while navigating through the directories.
     * @return a string of format "../../schmoo/"
     */
    private String getRelativeLink(String fromDir, String toDir)
        throws IOException
    {
        StringBuffer toLink = new StringBuffer();   // up from fromDir
        StringBuffer fromLink = new StringBuffer(); // down into toDir

        // create a List of toDir's parent directories
        LinkedList parents = new LinkedList();
        File f = new File(toDir);
        f = f.getCanonicalFile();
        while (f != null)
        {
            parents.add(f);
            f = f.getParentFile();
        }
            
        // walk up fromDir to find the common parent
        f = new File(fromDir);
        f = f.getCanonicalFile();
        f = f.getParentFile();
        boolean found = false;
        while (f != null && !found)
        {
            for (int i = 0; i < parents.size(); ++i)
            {
                File parent = (File) parents.get(i);
                if (f.equals(parent))
                {
                    // when we find the common parent, add the subdirectories 
                    // down to toDir itself
                    for (int j = 0; j < i; ++j)
                    {
                        File p = (File) parents.get(j);
                        toLink.insert(0, p.getName() + "/");
                    }
                    found = true;
                    break;
                }
            }
            f = f.getParentFile();
            fromLink.append("../");
        }

        if (!found)
        {
            throw new FileNotFoundException(fromDir + " and " + toDir +
                                            " have no common parent.");
        }

        return fromLink.append(toLink.toString()).toString();
    }

    /**
     * Sets a single source directory to be cross-referenced.
     *
     * @param sourceDir The source directory to be cross-referenced.
     */
    public void setSourceDir(String sourceDir)
    {
        if (!sourceDirs.isEmpty())
        {
            sourceDirs.clear();
        }
        addSourceDir(sourceDir);
    }

    /**
     * Adds a directory to the list of those to be cross-referenced.
     *
     * @param sourceDir The source directory to be cross-referenced.
     */
    public void addSourceDir(String sourceDir)
    {
        sourceDirs.add(sourceDir);
    }


    /**
     * DestDir is the directory in which jxr will write its output
     *
     * @param destDir the destination directory for jxr output
     */
    public void setDestDir(String destDir)
    {
        this.destDir = destDir;
    }

    /**
     * see setDestDir(String)
     *
     * @see setDestDir(String)
     */
    public String getDestDir()
    {
        return destDir;
    }


    /**
     * Lang attribute of output files.
     *
     * @param lang lang attribute of output files.
     */
    public void setLang(String lang)
    {
        this.lang = lang;
    }

    /**
     * see setLang(String)
     *
     * @see setLang(String)
     */
    public String getLang()
    {
        return lang;
    }


    /**
     * InputEncoding is the encoding of source files.
     *
     * @param inputEncoding encoding of source files
     */
    public void setInputEncoding(String inputEncoding)
    {
        this.inputEncoding = inputEncoding;
    }

    /**
     * see setInputEncoding(String)
     *
     * @see setInputEncoding(String)
     */
    public String getInputEncoding()
    {
        return inputEncoding;
    }


    /**
     * OutputEncoding is the encoding of output files.
     *
     * @param outputEncoding encoding of output files
     */
    public void setOutputEncoding(String outputEncoding)
    {
        this.outputEncoding = outputEncoding;
    }

    /**
     * see setOutputEncoding(String)
     *
     * @see setOutputEncoding(String)
     */
    public String getOutputEncoding()
    {
        return outputEncoding;
    }


    /**
     * JavadocDir is used to cross-reference the source code with
     * the appropriate javadoc pages.
     * 
     * If <code>null</code>, no javadoc link will be added.
     *
     * @param javadocDir The root directory containing javadocs
     */
    public void setJavadocDir(String javadocDir)
    {
        this.javadocDir = javadocDir;
    }

    /**
     * see setJavadocDir(String)
     *
     * @see setJavadocDir(String)
     */
    public String getJavadocDir()
    {
        return javadocDir;
    }

    /**
     * see DirectoryIndexer
     *
     * @param windowTitle used by DirectoryIndexer
     * @see DirectoryIndexer#setWindowTitle(String) setWindowTitle(String)
     */
    public void setWindowTitle(String windowTitle)
    {
        this.windowTitle = windowTitle;
    }

    /**
     * see DirectoryIndexer
     *
     * @see DirectoryIndexer#setWindowTitle(String) setWindowTitle(String)
     */
    public String getWindowTitle()
    {
        return windowTitle;
    }

    /**
     * see DirectoryIndexer
     *
     * @param docTitle used by DirectoryIndexer
     * @see DirectoryIndexer#setDocTitle(String) setDocTitle(String)
     */
    public void setDocTitle(String docTitle)
    {
        this.docTitle = docTitle;
    }

    /**
     * see DirectoryIndexer
     *
     * @see DirectoryIndexer#setDocTitle(String) setDocTitle(String)
     */
    public String getDocTitle()
    {
        return docTitle;
    }

    /**
     * see DirectoryIndexer
     *
     * @param bottom used by DirectoryIndexer
     * @see DirectoryIndexer#setBottom(String) setBottom(String)
     */
    public void setBottom(String bottom)
    {
        this.bottom = bottom;
    }

    /**
     * see DirectoryIndexer
     *
     * @see DirectoryIndexer#setBottom(String) setBottom(String)
     */
    public String getBottom()
    {
        return bottom;
    }

    /**
     * TemplateDir is where the navigation templates are located
     *
     * @param templateDir the template directory
     */
    public void setTemplateDir(String templateDir)
    {
        this.templateDir = templateDir;
    }

    /**
     * see setTemplateDir(String)
     *
     * @see setTemplateDir(String) setTemplateDir(String)
     */
    public String getTemplateDir()
    {
        return templateDir;
    }

}

