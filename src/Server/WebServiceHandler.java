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
    private static int serverId = -1;

    // region Server utilities

    /**
     * Tests if the Web Service is up.
     * @return True if the service is up, false otherwise.
     */
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
                System.out.println("Server response: " + output);
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

    /**
     * Gets a list of active servers connected to the Web Service.
     */
    public static void getServerList() {
        try {
            // Connect to the URL
            URL url = new URL(WS_URL + "/sget");
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
                System.out.println("Server response: " + output);
            }

            // Close connection
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL.");
        } catch (IOException e) {
            System.out.println("IO Exception. Web service may be down.");
        }
    }

    /**
     * Joins the Web Service network, obtaining a key
     */
    public static void joinWebService() {
        try {
            // Connect to the URL
            URL url = new URL(WS_URL + "/spost");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);
            conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

            // TODO Create a class to pass ONLY the ip and the port
            // Send the DataFile in JSON format
            String input = "{\"id\":-1,\"ip\":\"" + MediaServerLauncher.getAddress() + "\",\"port\":" + MediaServerLauncher.getPort() + "}";

            // Open stream
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            // Expect 201
            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String message = br.readLine();
            serverId = Integer.valueOf(br.readLine());

            System.out.println("Server response: " + message);
            System.out.println("Designated id for this server: " + serverId);

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

    // endregion

    // region File utilities

    /**
     * Posts a DataFile into the WS.
     * @param dataFile The new DataFile.
     */
    public static void postContent(DataFile dataFile) {
        try {
            // Connect to the URL
            URL url = new URL(WS_URL + "/fpost");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);
            conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

            // Send the DataFile in JSON format
            String input = dataFile.toJson(serverId);

            // Open stream
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            // Expect 403 if the file exists already
            if (conn.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new RuntimeException("File exists already." + conn.getResponseCode());
            }
            // Expect 201 otherwise
            else if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            }

            // Read server output, if any
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output;
            while ((output = br.readLine()) != null) {
                System.out.println("Server response: " + output);
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

    /**
     * Deletes a DataFile from the WS.
     * @param dataFile The DataFile to delete.
     */
    public static void deleteContent(DataFile dataFile) {
        try {
            // Connect to the URL
            URL url = new URL(WS_URL + "/fdelete");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);
            conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

            // Send the DataFile in JSON format
            String input = dataFile.toJson(serverId);

            // Open stream
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            // Expect 404 if the file does not exist
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new RuntimeException("File could not be found." + conn.getResponseCode());
            }
            // Expect 200 otherwise
            else if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            }

            // Read server output, if any
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output;
            while ((output = br.readLine()) != null) {
                System.out.println("Server response: " + output);
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

    /**
     * Updates a DataFile from the WS.
     * @param title The original title of the DataFile.
     * @param owner The owner of the DataFile.
     * @param newDataFile The new information.
     */
    public static void putContent(String title, String owner, DataFile newDataFile) {
        try {
            // Connect to the URL
            URL url = new URL(WS_URL + "/fput");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);
            conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

            // Send the DataFile in JSON format
            String input = "{"
                    + "\"serverId\":\"" + serverId
                    + "\",\"title\":\"" + title
                    + "\",\"owner\":\"" + owner
                    + "\",\"newTitle\":\"" + newDataFile.getTitle()
                    + "\",\"newTopic\":\"" + newDataFile.getTopic()
                    + "\",\"newDescription\":\"" + newDataFile.getDescription()
                    + "\"}";

            System.out.println(input);

            // Open stream
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            // Expect 404 if the file does not exist
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new RuntimeException("File could not be found." + conn.getResponseCode());
            }
            // Expect 200 otherwise
            else if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            }

            // Read server output, if any
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output;
            while ((output = br.readLine()) != null) {
                System.out.println("Server response: " + output);
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

    // endregion
}
