package org.apache.maven.mercury.transport.api;

/**
 * supplies credentials to the server
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class Credentials
{
  private String user;
  private String pass;
  
  private byte [] cert;
  
  public Credentials( String user, String pass )
  {
    this.user = user;
    this.pass = pass;
  }
  
  public Credentials( byte [] cert, String user, String pass )
  {
    this( user, pass );
    this.cert = cert;
  }
  
  public Credentials( byte [] cert )
  {
    this.cert = cert;
  }

  public String getUser()
  {
    return user;
  }

  public void setUser( String user )
  {
    this.user = user;
  }

  public String getPass()
  {
    return pass;
  }

  public void setPass( String pass )
  {
    this.pass = pass;
  }

  public byte [] getCertificate()
  {
    return cert;
  }
  
  public boolean isCertificate()
  {
    return cert != null && cert.length > 1;
  }

  public void setCertificate( byte [] cert )
  {
    this.cert = cert;
  }
  
  
}
