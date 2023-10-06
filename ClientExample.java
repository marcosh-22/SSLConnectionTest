package sslconnection.test;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

public class ClientExample {
    private static final String KEY_STORE_PATH = System.getProperty("user.dir") + "/keystore.jks";
    private static final String KEY_STORE_PASSWORD = "123456";
    private static final String URL_STRING = "https://localhost:8080/login";

    public static void main(String[] args) throws Exception {
        System.out.println("Starting client");

        // Create the SSL context with the truststore
        SSLContext sslContext = createSSLContext();

        String username = "admin";
        String password = "123";

        // Configure the secure connection
        HttpsURLConnection connection = createHttpsConnection(sslContext);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Send login data
        String postData = "username=" + username + "&password=" + password;
        try (OutputStream os = connection.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }

        // Read the server response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            try (InputStream is = connection.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String response = br.readLine();
                System.out.println("Server response: " + response);
            }
        } else {
            System.out.println("Error code: " + responseCode);
        }

        // Close the connection
        System.out.println("Closing connection");
        connection.disconnect();
    }

    private static SSLContext createSSLContext() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        FileInputStream trustStoreFile = new FileInputStream(KEY_STORE_PATH);
        trustStore.load(trustStoreFile, KEY_STORE_PASSWORD.toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    private static HttpsURLConnection createHttpsConnection(SSLContext sslContext) throws Exception {
        URL url = new URL(URL_STRING);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        return connection;
    }
}