package org.apache.maven.continuum.web.tool;


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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: CssTool.java,v 1.2 2005/04/06 14:10:38 trygvis Exp $
 */
public class CssTool
{
    private String[] classes = { "a", "b" };

    private int classState = 0;

    public String getNextClass()
    {
        if ( classState == 0 )
        {
            classState = 1;
        }
        else
        {
            classState = 0;
        }

        return classes[classState];
    }
}
