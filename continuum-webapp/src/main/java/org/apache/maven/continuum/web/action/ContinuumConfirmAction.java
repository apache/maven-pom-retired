package org.apache.maven.continuum.web.action;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public class ContinuumConfirmAction 
    extends ContinuumActionSupport
{

    public static final String CONFIRM = "confirm";

    protected boolean confirmed = false;

    protected String confirmedDisplay;

    protected String confirmationTitle;

    protected String confirmedName;

    protected String confirmedValue;

    protected String action;

    protected void setConfirmationInfo( String title, String actionName, String displayString, 
                                        String propertyName, String propertyValue )
    {
        action = actionName;
        confirmationTitle = title;
        confirmedDisplay = displayString;
        confirmedName = propertyName;
        confirmedValue = "" + propertyValue;
    }

    // setters and getters

    public void setConfirmedName( String name )
    {
        confirmedName = name;
    }

    public String getConfirmedName()
    {
        return confirmedName;
    }

    public void setConfirmedValue( String value )
    {
        confirmedValue = value;
    }

    public String getConfirmedValue()
    {
        return confirmedValue;
    }

    public void setConfirmationTitle( String title )
    {
        confirmationTitle = title;
    }

    public String getConfirmationTitle()
    {
        return confirmationTitle;
    }

    public void setConfirmedDisplay( String display )
    {
        confirmedDisplay = display;
    }

    public String getConfirmedDisplay()
    {
        return confirmedDisplay;
    }

    public void setConfirmed( boolean _confirmed )
    {
        confirmed = _confirmed;
    }

    public boolean getConfirmed()
    {
        return confirmed;
    }

    public void setAction( String _action )
    {
        action = _action;
    }

    public String getAction()
    {
        return action;
    }

}
