package org.apache.maven.mercury.representation.conflict;

/**
 * 
 * @author <a href="mailto:oleg@codehaus.org">Oleg Gusakov</a>
 * 
 * @version $Id$
 */

public class ConflictResolutionException
extends Exception
{
	public ConflictResolutionException()
	{
	}

	public ConflictResolutionException(String message)
	{
		super(message);
	}

	public ConflictResolutionException(Throwable cause)
	{
		super(cause);
	}

	public ConflictResolutionException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
