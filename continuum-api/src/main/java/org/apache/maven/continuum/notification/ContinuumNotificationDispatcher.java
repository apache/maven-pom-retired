package org.apache.maven.continuum.notification;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.ScmResult;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ContinuumNotificationDispatcher
{
    String ROLE = ContinuumNotificationDispatcher.class.getName();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    String MESSAGE_ID_BUILD_STARTED = "BuildStarted";

    String MESSAGE_ID_CHECKOUT_STARTED = "CheckoutStarted";

    String MESSAGE_ID_CHECKOUT_COMPLETE = "CheckoutComplete";

    String MESSAGE_ID_RUNNING_GOALS = "RunningGoals";

    String MESSAGE_ID_GOALS_COMPLETED = "GoalsCompleted";

    String MESSAGE_ID_BUILD_COMPLETE = "BuildComplete";

    String CONTEXT_BUILD = "build";

    String CONTEXT_BUILD_OUTPUT = "build-output";

    String CONTEXT_PROJECT = "project";

    String CONTEXT_BUILD_RESULT = "result";

    String CONTEXT_UPDATE_SCM_RESULT = "scmResult";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    void buildStarted( ContinuumProject project );

    void checkoutStarted( ContinuumProject project );

    void checkoutComplete( ContinuumProject project );

    void runningGoals( ContinuumProject project, ContinuumBuild build );

    void goalsCompleted( ContinuumProject project, ContinuumBuild build );

    void buildComplete( ContinuumProject project, ContinuumBuild build );
}
