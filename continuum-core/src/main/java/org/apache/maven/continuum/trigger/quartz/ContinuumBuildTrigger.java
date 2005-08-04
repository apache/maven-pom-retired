package org.apache.maven.continuum.trigger.quartz;

import org.quartz.SimpleTrigger;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ContinuumBuildTrigger
    extends SimpleTrigger
{
    public void setRepeatCount( int repeatCount )
    {
        super.setRepeatCount( SimpleTrigger.REPEAT_INDEFINITELY );
    }
}
