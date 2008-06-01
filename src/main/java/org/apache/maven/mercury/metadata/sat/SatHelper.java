package org.apache.maven.mercury.metadata.sat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.metadata.ArtifactMetadata;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class SatHelper
{
  //-----------------------------------------------------------------------
  public static final List<ArtifactMetadata> createList( String... uris )
  {
    List<ArtifactMetadata> aml = new ArrayList<ArtifactMetadata>( uris.length );
    for( String uri : uris )
    {
      aml.add( new ArtifactMetadata(uri) );
    }
    return aml;
  }
  //-----------------------------------------------------------------------
  public static final IVecInt getSmallOnes( int... ones )
  {
    VecInt res = new VecInt( ones.length );
    
    for( int i=0; i<ones.length; i++ )
      res.push( ones[i] );
    
    return res;
  }
  //-----------------------------------------------------------------------
  public static final IVec<BigInteger> getBigOnes( int... ones )
  {
    IVec<BigInteger> res = new Vec<BigInteger>( ones.length );
    
    for( int i=0; i<ones.length; i++ )
      res.push( new BigInteger(""+ones[i]) );
    
    return res;
  }
  //-----------------------------------------------------------------------
  public static final IVec<BigInteger> getBigOnes( int nOnes )
  {
    return getBigOnes(nOnes, false);
  }
  //-----------------------------------------------------------------------
  public static final IVec<BigInteger> getBigOnes( int nOnes, boolean negate )
  {
    Vec<BigInteger> res = new Vec<BigInteger>( nOnes );
    BigInteger bi = negate ? BigInteger.ONE.negate() : BigInteger.ONE;
    
    for( int i=0; i<nOnes; i++ )
      res.push( bi );
    
    return res;
  }
  //-----------------------------------------------------------------------
  public static final void showContext( int... ones )
  {
    VecInt res = new VecInt( ones.length );
    
    for( int i=0; i<ones.length; i++ )
      System.out.print( " x"+ones[i] );
  }
  //-----------------------------------------------------------------------
  public static final void show( int... ones )
  {
    VecInt res = new VecInt( ones.length );
    
    for( int i=0; i<ones.length; i++ )
      System.out.print( " x"+ones[i] );
  }
  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
}
