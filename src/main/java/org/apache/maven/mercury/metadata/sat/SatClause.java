package org.apache.maven.mercury.metadata.sat;

import java.math.BigInteger;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public class SatClause
{
  int [] _vars;
  int [] _coeff;
  int _sz = 0;
  int _ptr = 0;
  //---------------------------------------------------------------------------
  public SatClause( int sz )
  {
    _vars = new int [ sz ];
    _coeff = new int[ sz ];
    _sz = sz;
    _ptr = 0;
  }
  //---------------------------------------------------------------------------
  public void add( Integer v, Integer c )
  throws SatException
  {
    if( _ptr >= _sz )
      throw new SatException("trying to add element "+_ptr+" outsize of initialized size "+_sz);
    
    _vars[_ptr] = v.intValue();
    _coeff[_ptr++] = c.intValue();
  }
  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------
}

