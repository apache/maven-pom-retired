package org.apache.maven.mercury.metadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.maven.mercury.metadata.version.VersionException;
import org.apache.maven.mercury.metadata.version.VersionRange;
import org.apache.maven.mercury.repository.DefaultLocalRepository;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.layout.RepositoryLayout;
import org.mortbay.log.Log;
import org.xml.sax.SAXException;


/**
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class MockMetadataSource
implements MetadataSource
{

  public Collection<ArtifactMetadata> expand(
                    ArtifactMetadata mdq
                  , LocalRepository localRepository
                  , Set<RemoteRepository> remoteRepositories
                                        )
      throws MetadataRetrievalException
  {
    if( localRepository == null || mdq == null )
      return null;
    
    File queryDir = new File( localRepository.getDirectory(), mdq.groupId.replace('.', File.separatorChar)+File.separatorChar+mdq.artifactId );
    if( !queryDir.exists() || !queryDir.isDirectory() )
      return null;
    
    File [] files = queryDir.listFiles();
    
    if( files == null || files.length < 1 )
      return null;
    
    ArrayList<ArtifactMetadata> res = new ArrayList<ArtifactMetadata>( files.length );
    
    VersionRange range;
    try
    {
      range = new VersionRange( mdq.getVersion() );
    }
    catch (VersionException e)
    {
      throw new MetadataRetrievalException(e);
    }
    
    for( File f : files )
    {
      if( !f.isDirectory() )
        continue;
      
      File pom = new File( f, mdq.artifactId+"-"+f.getName()+".pom" );
      if( pom.exists() )
      {
        ArtifactMetadata md = getMD(pom);
        
        // TODO abstract into a range matcher
//        if( md.sameGAV(mdq) )
        if( range.includes( md.getVersion() ) )
          res.add(md);
      }
    }

Log.info("Expanded "+mdq+" into:\n"+res );
    return res;
  }

  public MetadataResolution retrieve( 
                      ArtifactMetadata metadata
                    , LocalRepository localRepository
                    , Set<RemoteRepository> remoteRepositories
                                    )
      throws MetadataRetrievalException
  {
    if( localRepository == null || metadata == null )
      return null;

    try
    {
      metadata.setDependencies( getDeps( localRepository.getDirectory(), metadata) );
      MetadataResolution mr = new MetadataResolution(metadata);
      
      return mr;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new MetadataRetrievalException(e);
    }
  }
  
  private static final List<ArtifactMetadata> getDeps( File repo, ArtifactMetadata md )
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
  
  private static final ArtifactMetadata getMD( File pom )
  throws MetadataRetrievalException
  {
    Digester digester = new Digester();
    digester.setValidating(false);
    digester.addObjectCreate( "project", ArtifactMetadata.class );
    digester.addCallMethod("project/groupId", "setGroupId", 1 );
    digester.addCallParam( "project/groupId", 0 );
    digester.addCallMethod("project/artifactId", "setArtifactId", 1 );
    digester.addCallParam( "project/artifactId", 0 );
    digester.addCallMethod("project/version", "setVersion", 1 );
    digester.addCallParam( "project/version", 0 );
    digester.addCallMethod("project/packaging", "setType", 1 );
    digester.addCallParam( "project/packaging", 0 );
    
    ArtifactMetadata md;
    try
    {
      md = (ArtifactMetadata) digester.parse(pom);
    }
    catch (Exception e)
    {
      throw new MetadataRetrievalException(e);
    }
    
    if( md.getType() == null )
      md.setType("jar");
    
    return md;
  }
  
  public static void main( String[] args )
  throws MetadataRetrievalException
  {
    MockMetadataSource mms = new MockMetadataSource();
    ArtifactMetadata md = new ArtifactMetadata( "pmd:pmd:3.9" );
    System.out.println("  Got deps as " + mms.retrieve(md, new DefaultLocalRepository("local", null, new File("/app/maven.repo")), null) );
  }

}
//==============================================================================================
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
//==============================================================================================
