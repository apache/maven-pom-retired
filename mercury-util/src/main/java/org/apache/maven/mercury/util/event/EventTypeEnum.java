package org.apache.maven.mercury.util.event;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public enum EventTypeEnum
{
      dependencyBuilder(0)
    , satSolver(1)
    
    , virtualRepositoryReader(2)
    
    , localRepository(3)
    , localRepositoryReader(4)
    , localRepositoryWriter(5)
    
    , remoteRepository(6)
    , remoteRepositoryReader(7)
    , remoteRepositoryWriter(8)
    
    , cache(9)
    , fsCache(10)
    ;
    
    int bitNo;
    
    EventTypeEnum( int bitNo )
    {
      this.bitNo = bitNo;
    }
  }
