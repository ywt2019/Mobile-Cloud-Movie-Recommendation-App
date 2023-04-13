package com.example.project4task2;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * @author Wenting Yu
 * Andrew ID: wy2
 * Email: wy2@andrew.cmu.edu
 * Last Modified: 4/3/23
 * This program is a model to fetch shows of a specific genre from the 3rd party API WatchMore.
 */
public class Project4Task2Model {

    static final String myAPIKey = "Uv3lPPo2IjUTyZuenTAdfRRJPhY09RfKFjX1eQIu";
    Genre g = new Genre(); // wrapper class to retrieve titles associated with a specific genre

    /**
     * function to fetch datqa from the 3rd party API
     * @param genreNumber input to the API
     */
    void fetchData(int genreNumber) {

        // test for invalid server-side input
        if (String.valueOf(genreNumber).matches("[0-9]+")) {

            // load all movies in genre genreNumber to g
            loadMovieLists(genreNumber);
        }
    }

    /**
     * load  a json file from an API and print the file to the console
     */
    public void loadMovieLists(int genreNumber){

        // fetch the whole json file from the url
        String movieListURL = "https://api.watchmode.com/v1/list-titles/?apiKey=" + myAPIKey + "&genres=" + genreNumber;

        String response = "";
        response = fetch(movieListURL, "TLSV1.3");

        // if third-party API is unavailable, response will be set to null by fetch()
        // isValid checks 3rd party invalid data
        if (response != null && isValid(response)) {

            //use gson.fromJson to convert the json file to an array of custom java class (defined in gsonClass.java)
            //this method only works if variable names in the custom java class matches each element of json components
            //name, code, etc.
            Gson gson = new Gson();
            Type userListType = new TypeToken<Genre>() {}.getType();
            this.g = gson.fromJson(response, userListType);
        }
    }

    /**
     * check if a given string is valid json or not (3rd party invalid data)
     * @param json input string
     * @return whether the string is valid json
     */
    public boolean isValid(String json) {
        try {
            JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            return false;
        }
        return true;
    }

    /*
     * Make an HTTP request to a given URL
     *
     * @param urlString The URL of the request
     * @return A string of the response from the HTTP GET.  This is identical
     * to what would be returned from using curl on the command line.
     */
    private static String fetch(String searchURL, String certType) {
        try {
            // Create trust manager, which lets you ignore SSLHandshakeExceptions
            createTrustManager(certType);
        } catch (KeyManagementException ex) {
            System.out.println("Shouldn't come here: ");
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Shouldn't come here: ");
            ex.printStackTrace();
        }

        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(searchURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String str;
            // Read each line of "in" until done, adding each to "response"
            while ((str = in.readLine()) != null) {
                // str is one line of text readLine() strips newline characters
                response.append(str);
            }
            in.close();
        } catch (IOException e) {
            System.err.println("Something wrong with URL");
            return null;
        }
        return response.toString();
    }

    private static void createTrustManager(String certType) throws KeyManagementException, NoSuchAlgorithmException{
        /**
         * Annoying SSLHandShakeException. After trying several methods, finally this
         * seemed to work.
         * Taken from: http://www.nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
         */
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance(certType);
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
