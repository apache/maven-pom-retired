package org.apache.maven.mercury.repository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.maven.mercury.ArtifactBasicMetadata;
import org.apache.maven.mercury.ArtifactMetadata;
import org.xml.sax.SAXException;

/**
 * 
 * a temporary thing to be replaced with real projectBuilder implementation
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MetadataProcessorMock
implements MetadataProcessor
{

  public List<ArtifactBasicMetadata> getDependencies(
                                      ArtifactBasicMetadata bmd
                                      , MetadataReader mdReader
                                                )
  throws MetadataProcessingException
  {
    List<ArtifactBasicMetadata> deps = null;
    
    try
    {
      byte [] pomBytes = mdReader.readMetadata( bmd );
      deps = getDeps(  pomBytes );
      
      return deps;
    }
    catch( Exception e )
    {
      throw new MetadataProcessingException( e );
    }
  }
  
  private static final List<ArtifactBasicMetadata> getDeps( byte [] pom )
  throws IOException, SAXException
  {
    DependencyCreator dc = new DependencyCreator();
    Digester digester = new Digester();
    digester.push( dc );
    
    digester.addCallMethod("project/dependencies/dependency", "addMD", 6 );
    digester.addCallParam("project/dependencies/dependency/groupId",0);
    digester.addCallParam("project/dependencies/dependency/artifactId",1);
    digester.addCallParam("project/dependencies/dependency/version",2);
    digester.addCallParam("project/dependencies/dependency/type",3);
    digester.addCallParam("project/dependencies/dependency/scope",4);
    digester.addCallParam("project/dependencies/dependency/optional",5);
    
    digester.parse( new ByteArrayInputStream(pom) );
    
    return dc.mds;
  }

}
//==============================================================================================
class DependencyCreator
{
  List<ArtifactBasicMetadata> mds = new ArrayList<ArtifactBasicMetadata>(8);
  
  public void addMD( String g, String a, String v, String t, String s, String o)
  {
    ArtifactBasicMetadata md = new ArtifactBasicMetadata();
    md.setGroupId(g);
    md.setArtifactId(a);
    md.setVersion(v);
    md.setType(t);
    md.setScope(s);
    md.setOptional(o);

    mds.add(md);
  }
}
//==============================================================================================
