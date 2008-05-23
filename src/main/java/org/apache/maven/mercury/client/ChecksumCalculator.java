package org.apache.maven.mercury.client;

import org.mortbay.util.IO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ChecksumCalculator
{
    private static final byte[] __HEX_DIGITS = "0123456789abcdef".getBytes();


    public static String encodeToAsciiHex( byte[] bytes )
    {
        int l = bytes.length;

        byte[] raw = new byte[l * 2];

        for ( int i = 0, j = 0; i < l; i++ )
        {
            raw[j++] = __HEX_DIGITS[( 0xF0 & bytes[i] ) >>> 4];
            raw[j++] = __HEX_DIGITS[0x0F & bytes[i]];
        }

        return new String( raw );
    }

    public static String readChecksumFromFile( File f ) throws FileNotFoundException, IOException
    {
        if ( f == null )
        {
            return null;
        }
        if ( !isChecksumFile( f ) )
        {
            throw new IOException( "Not a checksum file" );
        }

        FileInputStream fis = new FileInputStream( f );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IO.copy( fis, baos );
        return baos.toString( "UTF-8" );
    }

    public static boolean isChecksumFile( File f )
    {
        if ( f == null )
        {
            return false;
        }

        String fileName = f.getName().toLowerCase();
        if ( fileName.endsWith( ".sha" ) || fileName.endsWith( ".sha1" ) )
        {
            return true;
        }

        return false;
    }

}
