package main.java.android;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.URI;
import java.util.Properties;

//Jety server container to handle http servlet

public class AndroidHttpRestServer {
    Server server;
    public AndroidHttpRestServer(Properties properties) throws Exception {
        this(properties.getProperty("uri"));
    }

    /**
     * Construct a Jetty based http server for hosting rest service.
     *
     * @param uri           Provide a sample uri where the rest service will be hosted.
     *                      e.g., http://xxx:1234/myapp/rest
     *                      - scheme (http) is ignored
     *                      - hostname (xxx) is ignored
     *                      - port (1234) where the server will listen for requests
     *                      - path (/myapp/rest) context path
     * @throws Exception
     */
    public AndroidHttpRestServer(String uri) throws Exception {
        URI uri2 = new URI(uri);
        server = new Server(uri2.getPort());
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(uri2.getPath());
        server.setHandler(context);
        context.addServlet(new ServletHolder(new AndroidCameraOpenhomeController()
            ), "/*");

    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
        server.join();
    }
}
