package org.apache.maven.mercury.metadata;

import junit.framework.TestCase;

public abstract class AbstractSimpleTreeTest
extends TestCase
{
  //       b:b:1
  //      / 
  // a:a:1       b:b:2
  //    \     /
  //     c:c:1
  //          \ b:b:1
  
  ArtifactMetadata mdaa1 = new ArtifactMetadata("a:a:1");
  ArtifactMetadata mdbb1 = new ArtifactMetadata("b:b:1");
  ArtifactMetadata mdbb2 = new ArtifactMetadata("b:b:2");
  ArtifactMetadata mdcc1 = new ArtifactMetadata("c:c:1");
  
  MetadataTreeNode aa1;
  MetadataTreeNode bb1;
  MetadataTreeNode cc1;
  MetadataTreeNode cc1bb1;
  MetadataTreeNode cc1bb2;
  
  @Override
  protected void setUp() throws Exception
  {
    aa1 = new MetadataTreeNode( mdaa1, null, mdaa1 );
    bb1 = new MetadataTreeNode( mdbb1, aa1, mdbb1 );
    aa1.addChild(bb1);
    cc1 = new MetadataTreeNode( mdcc1, aa1, mdcc1 );
    aa1.addChild(cc1);
    cc1bb1 = new MetadataTreeNode( mdbb1, cc1, mdbb1 );
    cc1.addChild( cc1bb1 );
    cc1bb2 = new MetadataTreeNode( mdbb2, cc1, mdbb2 );
    cc1.addChild( cc1bb2 );
  }

}
