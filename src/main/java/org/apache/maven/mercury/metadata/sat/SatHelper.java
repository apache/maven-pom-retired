package org.apache.maven.mercury.metadata.sat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.ArtifactMetadata;
import org.sat4j.core.ReadOnlyVec;
import org.sat4j.core.ReadOnlyVecInt;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
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
  public static final IVecInt getSmallOnes( int... ints )
  {
    VecInt res = new VecInt( ints );
    return new ReadOnlyVecInt(res);
  }
  //-----------------------------------------------------------------------
  public static final IVecInt getSmallOnes( int first, int second )
  {
    return getSmallOnes( new int [] {first, second} );
  }
  //-----------------------------------------------------------------------
  public static int [] toIntArray( int first, int... ints )
  {
    int [] lits = new int[ ints.length+1 ];
    lits[0] = first;
    int ptr = 1;

    for( int i : ints )
      lits[ptr++] = i;
    
    return lits;
  }
  //-----------------------------------------------------------------------
  private static final IVec<BigInteger> toVec( BigInteger... bis  )
  {
    return new ReadOnlyVec<BigInteger>( new Vec<BigInteger>( bis ) );
  }
  //-----------------------------------------------------------------------
  public static final IVec<BigInteger> getBigOnes( int... ones )
  {
    BigInteger [] res = new BigInteger[ ones.length ];
    
    for( int i=0; i<ones.length; i++ )
      res[ i ] = BigInteger.valueOf( ones[i] );
    
    return toVec( res );
  }
  //-----------------------------------------------------------------------
  public static final IVec<BigInteger> getBigOnes( int nOnes, boolean negate )
  {
    BigInteger [] res = new BigInteger[ nOnes ];
    BigInteger bi = negate ? BigInteger.ONE.negate() : BigInteger.ONE;
    
    for( int i=0; i<nOnes; i++ )
      res[i] = bi;
    
    return toVec(res);
  }
  //-----------------------------------------------------------------------
  public static final IVec<BigInteger> getBigOnes( int first, int nOnes, boolean negateOnes )
  {
    int len = nOnes + 1;

    BigInteger [] res = new BigInteger[ len ];
    res[ 0 ] = BigInteger.valueOf(first);
    
    BigInteger bi = negateOnes ? BigInteger.ONE.negate() : BigInteger.ONE;
    
    for( int i=0; i<nOnes; i++ )
      res[i+1] = bi;
    
    return toVec(res);
  }
  //-----------------------------------------------------------------------
  public static final void show( int... ones )
  {
    for( int i=0; i<ones.length; i++ )
      System.out.print( " x"+ones[i] );
  }
  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
}
