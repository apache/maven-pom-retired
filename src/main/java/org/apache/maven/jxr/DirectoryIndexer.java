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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.XMLOutput;
import org.apache.maven.jxr.pacman.ClassType;
import org.apache.maven.jxr.pacman.PackageManager;
import org.apache.maven.jxr.pacman.PackageType;
import org.apache.oro.text.perl.Perl5Util;

/**
 * This class creates the navigational pages for jxr's cross-referenced source
 * files.  The navigation is inspired by javadoc, so it should have a familiar feel.
 *
 * Creates the following files:
 * <ul>
 *   <li>index.html            main index containing the frameset</li>
 *   <li>overview-frame.html   list of the project's packages              (top left)</li>
 *   <li>allclasses-frame.html list of all classes in the project          (bottom left)</li>
 *   <li>overview-summary.html top-level listing of the project's packages (main frame)</li>
 *
 *   <ul>
 *   Package specific:
 *     <li>package-summary.html listing of all classes in this package    (main frame)</li>
 *     <li>package-frame.html   listing of all classes in this package    (bottom left)</li>
 *   </ul>
 * </ul>
 *
 *
 * @author <a href="mailto:brian@brainslug.org">Brian Leonard</a>
 * @version $Id$
 */
public class DirectoryIndexer
{
    /*
     * CodeTransform uses this to cross-reference package references
     * with that package's main summary page.
     */
    final static String INDEX = "package-summary.html";

    /*
     * Path to the root output directory.
     */
    private String root;

    /*
     * Package Manager for this project.
     */
    private PackageManager packageManager;
    
    /*
     * see the getter/setter docs for these properties
     */
    private String outputEncoding;
    private String templateDir;
    private String windowTitle;
    private String docTitle;
    private String bottom;
    

    /**
     * Constructor for the DirectoryIndexer object
     *
     * @param packageManager  PackageManager for this project
     * @param root            Path of the root output directory
     */
    public DirectoryIndexer(PackageManager packageManager, String root)
    {
        this.packageManager = packageManager;
        this.root = root;
    }

    /**
     * OutputEncoding is the encoding of output files.
     *
     * @param outputEncoding output Encoding
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
     * TemplateDir is the location of the jelly template files used
     * to generate the navigation pages.
     *
     * @param templateDir location of the template directory
     */
    public void setTemplateDir(String templateDir)
    {
        this.templateDir = templateDir;
    }

    /**
     * see setTemplateDir(String)
     *
     * @see setTemplateDir(String)
     */
    public String getTemplateDir()
    {
        return templateDir;
    }
    
    /**
     * WindowTitle is used in the output's &lt;title&gt; tags
     * see the javadoc documentation for the property of the same name
     *
     * @param windowTitle the &lt;title&gt; attribute
     */
    public void setWindowTitle(String windowTitle)
    {
        this.windowTitle = windowTitle;
    }

    /**
     * see setWindowTitle(String)
     *
     * @see #setWindowTitle(String) setWindowTitle
     */
    public String getWindowTitle()
    {
        return windowTitle;
    }

    /**
     * DocTitle is used as a page heading for the summary files
     * see the javadoc documentation for the property of the same name
     *
     * @param docTitle major page heading
     */
    public void setDocTitle(String docTitle)
    {
        this.docTitle = docTitle;
    }

    /**
     * see setDocTitle(String)
     *
     * @see #setDocTitle(String) setDocTitle
     */
    public String getDocTitle()
    {
        return docTitle;
    }

    /**
     * Bottom is a footer for the navigation pages, usually a copyright
     * see the javadoc documentation for the property of the same name
     *
     * @param bottom page footer
     */
    public void setBottom(String bottom)
    {
        this.bottom = bottom;
    }

    /**
     * see setBottom(String)
     *
     * @see #setBottom(String) setBottom
     */
    public String getBottom()
    {
        return bottom;
    }
    
    /**
     * Does the actual indexing.
     *
     * @throws Exception If something went wrong with jelly's processing.
     */
    public void process()
        throws Exception
    {
        Map info = getPackageInfo();

        JellyContext mainContext = new JellyContext();
        mainContext.setVariable("outputEncoding", getOutputEncoding());
        mainContext.setVariable("windowTitle", getWindowTitle());
        mainContext.setVariable("docTitle", getDocTitle());
        mainContext.setVariable("bottom", getBottom());
        mainContext.setVariable("info", info);
        
        doJellyFile("index",            root, mainContext);
        doJellyFile("overview-frame",   root, mainContext);
        doJellyFile("allclasses-frame", root, mainContext);
        doJellyFile("overview-summary", root, mainContext);

        Iterator iter = ((Map)info.get("allPackages")).values().iterator();
        while (iter.hasNext())
        {
            Map pkgInfo = (Map)iter.next();

            JellyContext subContext = mainContext.newJellyContext();
            subContext.setVariable("pkgInfo", pkgInfo);

            String outDir = root + "/" + (String)pkgInfo.get("dir");
            doJellyFile("package-summary", outDir, subContext);
            doJellyFile("package-frame",   outDir, subContext);
        }
    }

    /*
     * executes a given jelly file with the given context and places the
     * generated file in outDir.  File names are assumed to be
     * {templateName}.jelly for input and {templateName}.html for output
     *
     */
    private void doJellyFile(String templateName, String outDir, JellyContext context)
        throws Exception
    {
        String outFile = outDir + "/" + templateName + ".html";
        OutputStream out = null;
        try
        {
            // Throws FileNotFoundException
            out = new FileOutputStream(outFile);

            String templateFileName = getTemplateDir() + "/" + templateName + ".jelly";
            File templateFile = new File(templateFileName);
        
            File theFile = new File(outFile);
            File dir = theFile.getParentFile();
            if (dir != null)
            {
                dir.mkdirs();
            }

            XMLOutput xmlOutput = XMLOutput.createXMLOutput(out, false);
            context.runScript(templateFile, xmlOutput);
            xmlOutput.flush();
        }
        catch (Throwable e)
        {
            System.out.println("IGNORING: Failed to process file [" + outFile + "]. Closing streams and moving on. Exception: " + e);
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }        
            }
            catch (IOException e)
            {
                System.out.println("Failed to close outputstream for file [" + outFile + "], which is a bad thing!");
                throw e;
            }
        }
    }
        

    /*
     * Creates a Map of other Maps containing information about
     * this project's packages and classes, obtained from the PackageManager.
     *
     * allPackages collection of Maps with package info, with the following format
     *   {name}    package name (e.g., "org.apache.maven.jxr")
     *   {dir}     package dir relative to the root output dir (e.g., "org/apache/maven/jxr")
     *   {rootRef} relative link to root output dir (e.g., "../../../../") note trailing slash
     *   {classes} collection of Maps with class info
     *      {name}  class name (e.g., "DirectoryIndexer")
     *      {dir}   duplicate of package {dir}
     *
     * allClasses collection of Maps with class info, format as above
     *
     */
    private Map getPackageInfo()
    {
        TreeMap allPackages = new TreeMap();
        TreeMap allClasses  = new TreeMap();
        Perl5Util perl = new Perl5Util();

        Enumeration packages = packageManager.getPackageTypes();
        while (packages.hasMoreElements())
        {
            PackageType pkg = (PackageType)packages.nextElement();
            String pkgName = pkg.getName();
            String pkgDir  = perl.substitute("s/\\./\\//g", pkgName);
            String rootRef = perl.substitute("s/[^\\.]*(\\.|$)/\\.\\.\\//g", pkgName);
            
            // special case for the default package
            // javadoc doesn't deal with it, but it's easy for us
            if (pkgName.length() == 0)
            {
                pkgName = "(default package)";
                pkgDir = ".";
                rootRef = "./";
            }

            TreeMap pkgClasses = new TreeMap();
            Enumeration classes = pkg.getClassTypes();
            while (classes.hasMoreElements())
            {
                ClassType clazz = (ClassType)classes.nextElement();
                
                String className = clazz.getName();
                Map classInfo = new HashMap();
                classInfo.put("name", className);
                classInfo.put("dir", pkgDir);

                pkgClasses.put(className, classInfo);
                allClasses.put(className, classInfo);
            }

            Map pkgInfo = new HashMap();
            pkgInfo.put("name", pkgName);
            pkgInfo.put("dir", pkgDir);
            pkgInfo.put("classes", pkgClasses);
            pkgInfo.put("rootRef", rootRef);
            allPackages.put(pkgName, pkgInfo);
        }

        Map info = new HashMap();
        info.put("allPackages", allPackages);
        info.put("allClasses",  allClasses);

        return info;
    }
}
