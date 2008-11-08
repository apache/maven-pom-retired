package org.sonatype.maven.plugins.crypto;

import org.apache.maven.artifact.manager.CredentialsChangeRequest;
import org.apache.maven.artifact.manager.CredentialsDataSource;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;

/**
 * 
 * @author Oleg Gusakov
 *
 *  @goal compare
 *  @requiresProject false
 */

public class MercuryCompareMojo
extends AbstractMojo
implements Initializable
{
  private static Log _log;
	//----------------------------------------------------------------
	/**
	 * @parameter expression="${listFile}"
	 */
	String listFile;
	/**
	  * @parameter expression="${session}"
	  */
	MavenSession _session;
//
//	/**
//	  * @parameter expression="${project}"
//	  */
//	MavenProject _project;
//
//	/**
//	  * @component
//	  */
//	Prompter _prompter;
	//----------------------------------------------------------------
	public void execute()
	throws MojoExecutionException, MojoFailureException
	{
		try {

			if( _session == null )
				throw new Exception("session not injected");

_log.info("\n------------------------------->");

			AuthenticationInfo auth = new AuthenticationInfo();
			auth.setUserName(username);
			auth.setPassword(password);
_log.info("Auth = "+auth);
			
			CredentialsChangeRequest req = new CredentialsChangeRequest( serverid, auth, oldpassword);
_log.info("Req = "+req);
			CredentialsDataSource cds = (CredentialsDataSource) _session.getContainer().lookup(CredentialsDataSource.class);
_log.info("Cds = "+cds);
			cds.set(req);
			
_log.info("Password for "+serverid+" succesfully "+(oldpassword==null?"set":"reset") );

		} catch( Exception e ) {
		  _log.error("Error setting password for "+serverid+": "+e.getMessage() );
			throw new MojoExecutionException( e.getMessage() );
		}
	}
	
  public void initialize()
      throws InitializationException
  {
    _log = getLog();
  }
	
	//----------------------------------------------------------------
	//----------------------------------------------------------------
}
