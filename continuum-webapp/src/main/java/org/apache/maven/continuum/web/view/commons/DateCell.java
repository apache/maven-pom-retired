package org.apache.maven.continuum.web.view.commons;

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

import java.util.Calendar;
import java.util.Locale;

import org.codehaus.plexus.util.StringUtils;

import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.BaseModel;
import org.extremecomponents.util.ExtremeUtils;

/**
 * Used in Project view
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DateCell
    extends DisplayCell
{
    public void init(BaseModel model, Column column)
    {
        super.init(model, column);

        String valueString = column.getPropertyValueAsString();

        if ( !StringUtils.isEmpty( valueString ) )
        {
            Locale locale = model.getTableHandler().getTable().getLocale();

            Object value = column.getPropertyValue();

            if ( value instanceof Long )
            {
                Calendar cal = Calendar.getInstance();

                cal.setTimeInMillis( ( (Long) value).longValue() );

                value = cal.getTime();
            }

            value = ExtremeUtils.formatDate( column.getParse(), column.getFormat(), value, locale );

            column.setValue(value);

            column.setPropertyValue(value);
        }
    }
}