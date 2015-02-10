package io.healthsamurai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class JavaWebServer {
    private static final int fNumberOfThreads = 100;
    private static final Executor fThreadPool = Executors.newFixedThreadPool(fNumberOfThreads);

    public static void main(String[] args) throws IOException {
        ServerSocket socket = new ServerSocket(11133);
        System.out.println("Started");
        while (true) {
            final Socket connection = socket.accept();
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    HandleRequest(connection);
                }
            };
            fThreadPool.execute(task);
        }
    }

    private static void HandleRequest(Socket s) {
        BufferedReader in;
        PrintWriter out;
        try {
            String webServerAddress = s.getInetAddress().toString();
            System.out.println("New Connection: " + webServerAddress);
            System.out.println("Current Thread: " + Thread.currentThread().getId());
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ArrayList<String> request = new ArrayList<String>();
            request.add(in.readLine());
            request.add(in.readLine());
            while (in.ready()) {
                request.add(in.readLine());
            }
            System.out.println("--- Client request ---");
            for (String l : request) {
                System.out.println(l);
            }
            out = new PrintWriter(s.getOutputStream(), true);
            out.println("HTTP/1.0 200");
            out.println("Content-type: text/html");
            out.println("Server-name: server");
            String response = "<!DOCTYPE html><html lang=\"en\">" +
                    "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css\">" +
                    "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css\">" +
                    "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js\"></script>"  +
                    "<head><title>Web Server</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                    "<style>\n" +
                    "body {\n" +
                    "  padding-top: 50px;\n" +
                    "}\n" +
                    ".starter-template {\n" +
                    "  padding: 40px 15px;\n" +
                    "  text-align: center;\n" +
                    "}" +
                    "</style>" +
                    "</head>" +
                    " <div class=\"container\"><div class=\"starter-template\"><h1>Connection: " + webServerAddress + "</h1></div>" +
                    "<p>Thread: " + Thread.currentThread().getId() + "</p>" +
                    "<h2>Request</h2><blockquote>";
            for (String l : request) {
                response += "<p>" + l + "</p>";
            }
            response += "</blockquote></div></html>";
            out.println("Content-length: " + response.length());
            out.println("");
            out.println(response);
            out.flush();
            out.close();
            s.close();
        } catch (Exception e) {
            System.out.println("Failed respond to client request: " + e.getMessage());
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
