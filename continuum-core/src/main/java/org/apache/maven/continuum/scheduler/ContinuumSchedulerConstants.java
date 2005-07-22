package org.apache.maven.continuum.scheduler;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ContinuumSchedulerConstants
{
    /** Checkout before performing a build */
    public static final int SCM_MODE_CHECKOUT = 0;

    /** Update before performing a build */
    public static final int SCM_MODE_UPDATE = 1;

    // ----------------------------------------------------------------------
    // Default Schedule
    // ----------------------------------------------------------------------

    /** Default schedule name */
    public static final String DEFAULT_SCHEDULE_NAME = "Default";

    /** Default schedule description */
    public static final String DEFAULT_SCHEDULE_DESC = "Default Continuum Schedule";

    /** Default scm mode which is to update */
    public static final int DEFAULT_SCHEDULE_SCM_MODE = SCM_MODE_UPDATE;

    /** Every hour on the hour */
    public static final String DEFAULT_CRON_EXPRESSION = "0 0 * * * ?";

    // ----------------------------------------------------------------------
    // Keys for JobDataMap
    // ----------------------------------------------------------------------

    public static final String LOGGER = "logger";

    public static final String CONTINUUM = "continuum";

    public static final String SCHEDULE = "schedule";

    public static final String BUILD_SETTINGS = "build-settings";
}
