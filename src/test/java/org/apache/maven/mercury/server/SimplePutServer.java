package org.apache.maven.mercury.server;

import java.io.File;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.servlet.PutFilter;

public class SimplePutServer extends Server
{
    private File _base;
    
    public SimplePutServer()
    throws Exception
    {
        super(0);

        HandlerCollection handlers = new HandlerCollection();
        setHandler(handlers);

        Context context = new Context(handlers,"/maven2/repo");
        handlers.addHandler(new DefaultHandler());

        _base = File.createTempFile("simplePutServer",null);
        _base.delete();
        _base.mkdir();
        _base.deleteOnExit();
        FilterHolder holder = context.addFilter(PutFilter.class, "/*", 0);
        holder.setInitParameter("delAllowed","true");
        context.addServlet(DefaultServlet.class,"/");
        context.setResourceBase(_base.getCanonicalPath());
    }
    
    public void destroy ()
    {
        super.destroy();
        File[] files = _base.listFiles();
        for (int i=0;files!=null && i>0; i++)
        {
            files[i].delete();
        }
        _base.delete();
    }
    
    public File getPutDir ()
    {
        return _base;
    }

    public int getPort()
    {
        return getConnectors()[0].getLocalPort();
    }

    public static void main(String[] args)
    throws Exception
    {
        SimplePutServer server = new SimplePutServer();
        server.start();
        server.join();
    }

}
