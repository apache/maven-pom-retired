package org.apache.maven.continuum.web.view.jsp.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;

import com.opensymphony.webwork.views.jsp.ui.TextTag;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.util.OgnlValueStack;

/**
 * First attempt at creating a date tag for the webwork framework. The tag will
 * format a date by using either a specified format attribute, or by falling
 * back on to a globally defined 'webwork.date' property. 
 * When nice="true" is specified, it will return a human readable string (in 2 hours, 3 minutes).
 *
 * From http://jira.opensymphony.com/browse/WW-805
 * 
 * @author <a href="mailto:philip.luppens@gmail.com">Philip Luppens</a>
 */
public class DateTag
    extends TextTag
{
    /*
     * the name of our property which we will use if the optional format
     * parameter is not specified.
     */
    public final static String DATETAG_PROPERTY = "webwork.date";

    public final static String DATETAG_PROPERTY_PAST = "webwork.date.format.past";
    public final static String DATETAG_DEFAULT_PAST = "{0} ago";
    public final static String DATETAG_PROPERTY_FUTURE = "webwork.date.format.future";
    public final static String DATETAG_DEFAULT_FUTURE = "in {0}";

    public final static String DATETAG_PROPERTY_SECONDS = "webwork.date.format.seconds";
    public final static String DATETAG_DEFAULT_SECONDS = "an instant";
    public final static String DATETAG_PROPERTY_MINUTES = "webwork.date.format.minutes";
    public final static String DATETAG_DEFAULT_MINUTES = "{0,choice,1#one minute|1<{0} minutes}";
    public final static String DATETAG_PROPERTY_HOURS = "webwork.date.format.hours";
    public final static String DATETAG_DEFAULT_HOURS = "{0,choice,1#one hour|1<{0} hours}{1,choice,0#|1#, one minute|1<, {1} minutes}";
    public final static String DATETAG_PROPERTY_DAYS = "webwork.date.format.days";
    public final static String DATETAG_DEFAULT_DAYS = "{0,choice,1#one day|1<{0} days}{1,choice,0#|1#, one hour|1<, {1} hours}";
    public final static String DATETAG_PROPERTY_YEARS = "webwork.date.format.years";
    public final static String DATETAG_DEFAULT_YEARS = "{0,choice,1#one year|1<{0} years}{1,choice,0#|1#, one day|1<, {1} days}";

    //our optional format parameter
    private String format;
    private String actualName;
    private String nameAttr;
    private boolean nice;
    private Date date;
    private TextProvider tp;

    public int doEndTag()
        throws JspException
    {
        actualName = (String) findString( nameAttr );
        String msg = null;
        OgnlValueStack stack = getStack();
        //find the name on the valueStack, and cast it to a date
        Object dateObj = stack.findValue( actualName );
        if ( dateObj instanceof Date )
        {
            date = (Date) dateObj;
        }
        else if ( dateObj instanceof Long )
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis( ( (Long) dateObj).longValue() );
            date = cal.getTime();
        }
        else
        {
            throw new JspException( "Could not cast the requested object " + nameAttr + " to a java.util.Date", e );
        }

        if ( date != null )
        {
            tp = findProviderInStack();

            if ( tp == null )
            {
                throw new JspException( "Could not find a TextProvider on the stack." );
            }

            if ( nice )
            {
                msg = formatTime( date );
            }
            else
            {
                if ( format == null )
                {
                    String globalFormat = null;
                    //if the format is not specified, fall back using the defined
                    // property DATETAG_PROPERTY

                    globalFormat = tp.getText( DATETAG_PROPERTY );

                    if ( globalFormat != null )
                    {
                        msg = new SimpleDateFormat( globalFormat, ActionContext.getContext().getLocale() ).format( date );
                    }
                    else
                    {
                        //fall back using the xwork date format ?
                    }
                }
                else
                {
                    msg = new SimpleDateFormat( format ).format( date );
                }
            }
        }

        if ( msg != null )
        {
            try
            {
                //if we used the id attribute, we will store the formatted date
                // in the valuestack, otherwise, we write it to the
                // outputstream.
                if ( getId() == null )
                {
                    pageContext.getOut().write( msg );
                }
                else
                {
                    stack.getContext().put( getId(), msg );
                }
            }
            catch ( IOException e )
            {
                throw new JspException( e );
            }
        }
        return EVAL_PAGE;
    }

    private TextProvider findProviderInStack()
    {
        for ( Iterator iterator = getStack().getRoot().iterator(); iterator.hasNext(); )
        {
            Object o = iterator.next();

            if ( o instanceof TextProvider )
            {
                return (TextProvider) o;
            }

        }
        return null;
    }

    public String formatTime( Date date )
    {
        StringBuffer sb = new StringBuffer();
        List args = new ArrayList();
        long secs = ( new Date().getTime() - date.getTime() ) / 1000;
        long mins = secs / 60;
        int min = (int) mins % 60;
        long hours = mins / 60;
        int hour = (int) hours % 24;
        int days = (int) hours / 24;
        int day = days % 365;
        int years = days / 365;

        if ( Math.abs( secs ) < 60 )
        {
            args.add( new Long( secs ) );
            args.add( sb );
            args.add( null );
            sb.append( tp.getText( DATETAG_PROPERTY_SECONDS, DATETAG_DEFAULT_SECONDS, args ) );

        }
        else if ( hours == 0 )
        {
            args.add( new Long( min ) );
            args.add( sb );
            args.add( null );
            sb.append( tp.getText( DATETAG_PROPERTY_MINUTES, DATETAG_DEFAULT_MINUTES, args ) );

        }
        else if ( days == 0 )
        {
            args.add( new Long( hour ) );
            args.add( new Long( min ) );
            args.add( sb);
            args.add( null);
            sb.append( tp.getText( DATETAG_PROPERTY_HOURS, DATETAG_DEFAULT_HOURS, args ) );
        }
        else if ( years == 0 )
        {
            args.add( new Long( days ) );
            args.add( new Long( hour ) );
            args.add( sb );
            args.add( null );
            sb.append( tp.getText( DATETAG_PROPERTY_DAYS, DATETAG_DEFAULT_DAYS, args ) );
        }
        else
        {
            args.add( new Object[]{ new Long( years ) } );
            args.add( new Object[]{ new Long( day ) } );
            args.add( sb );
            args.add( null );

            sb.append( tp.getText( DATETAG_PROPERTY_YEARS, DATETAG_DEFAULT_YEARS, args ) );
        }
        args.clear();
        args.add( sb.toString() );
        if ( date.before( new Date() ) )
        {
            //looks like this date is passed
            return tp.getText( DATETAG_PROPERTY_PAST, DATETAG_DEFAULT_PAST, args );
        } else {
            return tp.getText( DATETAG_PROPERTY_FUTURE, DATETAG_DEFAULT_FUTURE, args );
        }
    }

    public int doStartTag()
        throws JspException
    {
        return super.doStartTag();
    }

    public void setName( String name )
    {
        this.nameAttr = name;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat( String format )
    {
        this.format = format;
    }

    public boolean isNice()
    {
        return nice;
    }

    public void setNice( boolean nice )
    {
        this.nice = nice;
    }
}
