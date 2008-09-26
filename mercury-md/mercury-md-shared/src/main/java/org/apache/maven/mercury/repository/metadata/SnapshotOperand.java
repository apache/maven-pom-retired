package org.apache.maven.mercury.repository.metadata;

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * String storage
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class SnapshotOperand
    extends AbstractOperand
{
  private static final Language lang = new DefaultLanguage( SnapshotOperand.class );
  Snapshot snapshot;
  
  public SnapshotOperand( Snapshot data )
  {
    this.snapshot = data;
  }
  
  public Snapshot getOperand()
  {
    return snapshot;
  }
}
