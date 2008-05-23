package org.apache.maven.mercury.validate;

import org.codehaus.plexus.util.FileUtils;

import java.io.IOException;
import java.util.List;

public class PomValidator implements Validator
{
    public String getFileExtension()
    {
        return "pom";
    }

    public boolean validate(String stagedFile, List<String> errors)
    {
        try
        {
            String file = FileUtils.fileRead( stagedFile );

            if ( file.contains( "<project>" ) )
            {
                return true;
            }
            else
            {
                errors.add( "file not valid" );
            }
        }
        catch ( IOException ioe )
        {
            errors.add( "ioe" );
        }

        return false;
    }
}
