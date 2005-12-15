package org.apache.maven.continuum.web.action;

import org.apache.maven.continuum.Continuum;

import com.opensymphony.xwork.ActionSupport;

import java.util.Collection;

public class LoginAction
    extends ActionSupport
{
    private String username;

    private String password;

    public String execute()
        throws Exception
    {
        //TODO
        if ( username == null || password == null || !"testuser".equals( username ) )
        {
            //TODO : i18n
            addFieldError( "username", "Username/password incorrect");

            System.out.println( "Username=" + username + " - password = " + password);

            return INPUT;
        }
        System.out.println( "SUCESS" + "Username=" + username + " - password = " + password);
        return SUCCESS;
    }

    public String doDefault()
    {
        return INPUT;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }
}
