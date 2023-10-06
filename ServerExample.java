package sslconnection.test;

import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.security.*;
import java.util.stream.Collectors;

public class ServerExample {
    private static final String KEY_STORE_PATH = System.getProperty("user.dir") + "/keystore.jks";
    private static final String KEY_STORE_PASSWORD = "123456";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // Load the keystore
        char[] password = KEY_STORE_PASSWORD.toCharArray();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEY_STORE_PATH)) {
            keyStore.load(fis, password);
        }

        // Create the SSL context
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        // Create the embedded HTTPS server
        HttpsServer server = HttpsServer.create(new InetSocketAddress(8080), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext));

        // Define the request handler
        server.createContext("/login", new LoginHandler());

        // Start the server
        server.start();

        System.out.println("Server started on port 8080.");
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                // Read login data from the request body
                String formData = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));

                // Extract the username and password from the form data
                String username = formData.split("&")[0].split("=")[1];
                String password = formData.split("&")[1].split("=")[1];

                // Process the login data
                String response = "Login received: User: " + username + " Password: " + password;

                // Send the response back to the client
                Headers headers = exchange.getResponseHeaders();
                headers.set("Content-Type", "text/plain");
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                // Unsupported method
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}