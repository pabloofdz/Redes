package es.udc.redes.tutorial.udp.server;

import java.net.*;

/**
 * Implements a UDP echo server.
 */
public class UdpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.udp.server.UdpServer <port_number>");
            System.exit(-1);
        }
        DatagramSocket sDatagram = null;
        try {
            int serverPort = Integer.parseInt(argv[0]);
            // Create a server socket
            sDatagram = new DatagramSocket(serverPort);
            // Set maximum timeout to 300 secs
            sDatagram.setSoTimeout(300000);

            byte array[] = new byte[1024];

            while (true) {
                // Prepare datagram for reception
                DatagramPacket dgramRec = new DatagramPacket(array, array.length);
                // Receive the message
                sDatagram.receive(dgramRec);
                System.out.println("SERVER: Received "
                        + new String(dgramRec.getData(), 0, dgramRec.getLength())
                        + " from " + dgramRec.getAddress().toString() + ":"
                        + dgramRec.getPort());
                // Prepare datagram to send response
                DatagramPacket dgramSent = new DatagramPacket(dgramRec.getData(),
                        dgramRec.getLength(), dgramRec.getAddress(), dgramRec.getPort());
                // Send response
                sDatagram.send(dgramSent);
                System.out.println("SERVER: Sending "
                        + new String(dgramSent.getData()) + " to "
                        + dgramSent.getAddress().toString() + ":"
                        + dgramSent.getPort());
            }

        } catch (SocketTimeoutException e) {
            System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
        // Close the socket
            sDatagram.close();
        }
    }
}
