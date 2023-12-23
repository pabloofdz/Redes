package es.udc.redes.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**Para ejecutar wl web server hacer:
 * 1. Situarse en: p1-files (Se supone que los ficheros se encuentran en el mismo directorio desde el que estamos ejecutando)**/
public class WebServer {
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Format: es.udc.redes.webserver.WebServer <port>");
            System.exit(-1);
        }
        ServerSocket sSocket = null;
        Socket client = null;
        ServerThread sThread = null;
        try {
            int serverPort = Integer.parseInt(args[0]);
            // Create a server socket
            sSocket = new ServerSocket(serverPort);
            // Set a timeout of 300 secs
            sSocket.setSoTimeout(300000);
            while (true) {
                // Wait for connections
                client = sSocket.accept();
                // Create a ServerThread object, with the new connection as parameter
                sThread = new ServerThread(client);
                // Initiate thread using the start() method
                sThread.start();
            }

        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally{
            //Close the socket
            try {
                sSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
