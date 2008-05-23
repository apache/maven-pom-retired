package org.apache.maven.mercury.client.deploy;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;


public class RandomBatchIdGenerator implements BatchIdGenerator
{
    protected final static String SESSION_ID_RANDOM_ALGORITHM = "SHA1PRNG";
    private Random _random;
    private boolean _initialized;


    public RandomBatchIdGenerator()
    {
    }

    public String getId()
    {
        init();
        String id = "";
        if ( !( _random instanceof SecureRandom ) )
        {
            id = String.valueOf( hashCode() ^ Runtime.getRuntime().freeMemory() ^ _random.nextInt() );
        }
        else
        {
            id = String.valueOf( _random.nextLong() );
        }
        return id;
    }

    public void setRandom( Random random )
    {
        _random = random;
    }

    public Random getRandom()
    {
        return _random;
    }

    private void init()
    {
        synchronized ( this )
        {
            if ( !_initialized )
            {
                try
                {
                    _random = SecureRandom.getInstance( SESSION_ID_RANDOM_ALGORITHM );
                }
                catch ( NoSuchAlgorithmException e )
                {
                    _random = new Random();
                }
                _random.setSeed(
                    _random.nextLong() ^ System.currentTimeMillis() ^ hashCode() ^ Runtime.getRuntime().freeMemory() );
                _initialized = true;
            }
        }
    }
}
