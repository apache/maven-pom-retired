package org.apache.maven.mercury.metadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.maven.mercury.repository.DefaultLocalRepository;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.layout.RepositoryLayout;
import org.xml.sax.SAXException;

public class MockMetadataSource
implements MetadataSource
{

  public Collection<ArtifactMetadata> expand(ArtifactMetadata metadataQuery,
      LocalRepository localRepository, Set<RemoteRepository> remoteRepositories)
      throws MetadataRetrievalException
  {
    // TODO Auto-generated method stub
    return null;
  }
  

  public MetadataResolution retrieve(ArtifactMetadata metadata,
      LocalRepository localRepository, Set<RemoteRepository> remoteRepositories)
      throws MetadataRetrievalException
  {
    if( localRepository == null || metadata == null )
      return null;

    try
    {
      metadata.setDependencies( getMD( localRepository.getDirectory(), metadata) );
      MetadataResolution mr = new MetadataResolution(metadata);
      
      return mr;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new MetadataRetrievalException(e);
    }
  }
  
  private static final List<ArtifactMetadata> getMD( File repo, ArtifactMetadata md )
  throws IOException, SAXException
  {
    File pom = new File( repo, md.groupId.replace('.', '/')
        +'/'+md.artifactId+'/'+md.version
        +'/'+md.artifactId+'-'+md.version+".pom"
                      );
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
    
    digester.parse(pom);
    
    return dc.mds;
  }
  
  public static void main( String[] args )
  throws MetadataRetrievalException
  {
    MockMetadataSource mms = new MockMetadataSource();
    ArtifactMetadata md = new ArtifactMetadata( "pmd:pmd:3.9" );
    System.out.println("  Got deps as " + mms.retrieve(md, new DefaultLocalRepository("local", null, new File("/app/maven.repo")), null) );
  }

}

class DependencyCreator
{
  List<ArtifactMetadata> mds = new ArrayList<ArtifactMetadata>(8);
  
  public void addMD( String g, String a, String v, String t, String s, String o)
  {
    ArtifactMetadata md = new ArtifactMetadata();
    md.setGroupId(g);
    md.setArtifactId(a);
    md.setVersion(v);
    md.setType(t);
    md.setScope(s);
    md.setOptional(o);

    mds.add(md);
  }
}
