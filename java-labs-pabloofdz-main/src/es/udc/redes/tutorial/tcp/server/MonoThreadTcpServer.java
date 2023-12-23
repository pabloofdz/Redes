package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;

/**
 * MonoThread TCP echo server.
 */
public class MonoThreadTcpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.tcp.server.MonoThreadTcpServer <port>");
            System.exit(-1);
        }
        ServerSocket sSocket = null;
        Socket socket2 = null;
        try {
            int serverPort = Integer.parseInt(argv[0]);
            // Create a server socket
            sSocket = new ServerSocket(serverPort);
            // Set a timeout of 300 secs
            sSocket.setSoTimeout(300000);
            while (true) {
                // Wait for connections
                socket2 = sSocket.accept();
                // Set the input channel
                BufferedReader sInput = new BufferedReader(new InputStreamReader(
                        socket2.getInputStream()));
                // Set the output channel
                PrintWriter sOutput = new PrintWriter(socket2.getOutputStream(), true);
                // Receive the client message
                String received = sInput.readLine();
                System.out.println("SERVER: Received " + received
                        + " from " + socket2.getInetAddress().toString()
                        + ":" + socket2.getPort());
                // Send response to the client
                System.out.println("SERVER: Sending " + received +
                        " to " + socket2.getInetAddress().toString() +
                        ":" + socket2.getPort());
                sOutput.println(received);
                // Close the streams
                sOutput.close();
                sInput.close();
                socket2.close();
            }

        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
	        //Close the socket
            try {
                sSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
