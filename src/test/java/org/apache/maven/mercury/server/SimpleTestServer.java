package org.apache.maven.mercury.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.URL;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.util.IO;

public class SimpleTestServer extends Server
{

    public SimpleTestServer()
        throws Exception
    {
        super(0);
        
        HandlerCollection handlers = new HandlerCollection();
        setHandler(handlers);
        
        Context context = new Context(handlers,"/maven2/repo");
        handlers.addHandler(new DefaultHandler());
        
        File base = File.createTempFile("simpleTestServer",null);
        base.delete();
        base.mkdir();
        base.deleteOnExit();
        
        
        URL list = SimpleTestServer.class.getResource("/testRepo/");
        LineNumberReader in = new LineNumberReader(new InputStreamReader(list.openStream()));
        String file=null;
        while ((file=in.readLine())!=null)
        {
            if (!file.startsWith("file"))
                continue;
            OutputStream out=new FileOutputStream(new File(base,file));
            IO.copy(SimpleTestServer.class.getResource("/testRepo/"+file).openStream(),out);
            out.close();
        }
        context.addServlet(DefaultServlet.class,"/");
        context.setResourceBase(base.getCanonicalPath());
    }
    
    public int getPort()
    {
        return getConnectors()[0].getLocalPort();
    }

    public static void main(String[] args)
        throws Exception
    {
        SimpleTestServer server = new SimpleTestServer();
        server.start();
        server.join();
    }
}
