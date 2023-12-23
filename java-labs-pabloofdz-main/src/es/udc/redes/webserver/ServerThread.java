package es.udc.redes.webserver;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**Para ejecutar wl web server hacer:
 * 1. Situarse en: p1-files (Se supone que los ficheros se encuentran en el mismo directorio desde el que estamos ejecutando)**/
public class ServerThread extends Thread {

    private final Socket socket;
    private Date lastModified;
    private final SimpleDateFormat dateformat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private Date ifmodsinceDate;

    public ServerThread(Socket s) {
        // Store the socket s
        socket = s;
    }

    public void run() {
        try {
            // Set the input channel
            BufferedReader sInput = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            // Set the output channel
            PrintWriter sOutput = new PrintWriter(socket.getOutputStream(), true);
            // Receive the message from the client
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while (!(line = sInput.readLine()).isBlank()) {
                requestBuilder.append(line).append("\n");
            }

            String received = requestBuilder.toString();
            String[] requestsLines = received.split("\n");
            String[] requestLine = requestsLines[0].split(" ");
            String method = requestLine[0];

            String ifmodsinceDateaux = "";
            for (String requestsLine : requestsLines) {
                if (requestsLine.contains("If-Modified-Since")) {
                    ifmodsinceDateaux = requestsLine.substring(19);
                    break;
                }
            }
            if(!ifmodsinceDateaux.equals(""))
                ifmodsinceDate = dateformat.parse(ifmodsinceDateaux);

            System.out.println("SERVER: Received " + requestsLines[0]
                    + " from " + socket.getInetAddress().toString()
                    + ":" + socket.getPort());

            boolean get = method.equals("GET");
            // Sent the echo message to the client
            if (get || method.equals("HEAD")) {
                File file;
                if(requestLine[1].length() == 1)
                    file = new File("_");
                else
                    file = new File(requestLine[1].substring(1));
                Date lastModifiedms = new Date(file.lastModified());
                Path filePath = file.toPath();
                if (Files.exists(filePath)) {
                    // file exist
                    String contentType = guessContentType(filePath);
                    if(get) {
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(lastModifiedms.getTime());
                        lastModified = new Date(seconds*1000);
                        if (ifmodsinceDateaux.equals(""))
                            sendResponse(socket, "200 OK", contentType, lastModified, Files.readAllBytes(filePath), true);
                        else if (lastModified.after(ifmodsinceDate))
                            sendResponse(socket, "200 OK", contentType, lastModified, Files.readAllBytes(filePath), true);
                        else
                            sendResponse(socket, "304 Not Modified", contentType, lastModified, Files.readAllBytes(filePath), false);
                    }else
                        sendResponse(socket, "200 OK", contentType, lastModified, Files.readAllBytes(filePath), false);
                } else {
                    // 404 Not Found
                    File file404 = new File("error404.html");
                    lastModified = new Date(file404.lastModified());
                    Path filePath404 = file404.toPath();
                    sendResponse(socket, "404 Not Found", "text/html", lastModified, Files.readAllBytes(filePath404), get);
                }
            }else{
                //400 Bad Request
                File file400 = new File("error400.html");
                lastModified = new Date(file400.lastModified());
                Path filePath400 = file400.toPath();
                sendResponse(socket, "400 Bad Request", "text/html", lastModified, Files.readAllBytes(filePath400), true);
            }
            // Close the streams
            sOutput.close();
            sInput.close();

        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Close the socket
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(Socket client, String status, String contentType, Date lastModified, byte[] content, boolean sendR) throws IOException {
        Date date = new Date();
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.0 " + status + "\n").getBytes());
        clientOutput.write(("Date: " + dateformat.format(date) + "\n").getBytes());
        clientOutput.write(("Server: WebServer_250 \n").getBytes());
        clientOutput.write(("Content-Length: " + content.length + "\n").getBytes());
        clientOutput.write(("Content-Type: " + contentType + "\n").getBytes());
        clientOutput.write(("Last-Modified: " + dateformat.format(lastModified) + "\n").getBytes());
        if(sendR) {
            clientOutput.write("\n".getBytes());
            clientOutput.write(content);
        }
        clientOutput.flush();
        client.close();
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }
}
