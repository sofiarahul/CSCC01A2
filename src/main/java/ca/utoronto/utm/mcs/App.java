package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class App
{
    static int port = 8080;

    public static void main(String[] args) throws IOException
    {
    	Dagger service = DaggerDaggerComponent.create().buildMongoHttp();
    	
    	//Create your server context here
    	
    	Post parameter = DaggerPostComponent.create().getPost();
    	service.getServer().createContext("/api/v1/post", parameter);

    	service.getServer().start();
    	
    	System.out.printf("Server started on port %d", port);
    }
}
