package Server;

import Logic.DataFile;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WebServiceHandler {

    private static final String WS_URL = "http://localhost:8080/WebServiceWeb/rest/service";

    public static boolean testWebService() {

        boolean status = true;

        try {
            // Connect to the URL
            URL url = new URL(WS_URL + "/test");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);

            // Expect 200
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            }

            // Read server output, if any
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output;
            while ((output = br.readLine()) != null) {
                System.out.println("\nServer response: " + output);
            }

            // Close connection
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL.");
            status = false;
        } catch (IOException e) {
            System.out.println("IO Exception. Web service may be down.");
            status = false;
        }

        return status;
    }

    public static void joinWebService() {
        try {
            // Connect to the URL
            URL url = new URL(WS_URL + "/spost");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);
            conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

            // Send the DataFile in JSON format
            String input = "{\"id\":1,\"ip\":\"127.0.0.1\",\"port\":8080}";

            // Open stream
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            // Expect 201
            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            }

            // Read server output, if any
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output;
            while ((output = br.readLine()) != null) {
                System.out.println("\nServer response: " + output);
            }

            // Close connection
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO Exception.");
            e.printStackTrace();
        }
    }

    public static void postContent() {
        try {
            // Connect to the URL
            URL url = new URL(WS_URL + "/fpost");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);
            conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

            // TODO Pass the file by parameter and allow no the path parameter
            // Send the DataFile in JSON format
            DataFile datafile = new DataFile("Edge of tomorrow", DataFile.Topic.Action, "The best movie ever dude.", "GOD", "");
            // TODO Pass the actual server id
            String input = datafile.toJson(1);
            System.out.println(input);

            // Open stream
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            // Expect 201
            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            }

            // Read server output, if any
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output;
            while ((output = br.readLine()) != null) {
                System.out.println("\nServer response: " + output);
            }

            // Close connection
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO Exception.");
            e.printStackTrace();
        }
    }
}
