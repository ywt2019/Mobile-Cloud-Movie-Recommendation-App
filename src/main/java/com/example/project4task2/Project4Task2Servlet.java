package com.example.project4task2;

import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Wenting Yu
 * Andrew ID: wy2
 * Email: wy2@andrew.cmu.edu
 * Last Modified: 4/3/23
 * This program is a servlet to receive http request from a client, send response, and store client logs in a MongoDB database.
 */

@WebServlet(name = "Project4Task2", urlPatterns = {"/shows","/dashboard"})
public class Project4Task2Servlet extends HttpServlet {

    Project4Task2Model ptm = null;  // The "business model" for this app

    MongoDatabase database; // database connection instance

    // to find which genre is requested by the user (as user only type in a number the genre)
    static final String[] genreNumberToName = new String[] {"Action","Adventure","Animation","Comedy","Crime","Documentary","Drama","Family","Fantasy","History"};

    // logs to be stored in MongoDB
    Timestamp androidRequestTime;
    Timestamp androidSentBackTime;
    long fetchTime;
    String requestedGenre;
    int numOfShowsReturned;
    String showsSentBack;

    // Initiate this servlet by instantiating the model that it will use.
    public void init() {
        ptm = new Project4Task2Model();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String nextView;

        long startTime;
        long endTime;

        // connect to database
        connectToMongoDB();

        // show dashboard when user type in "dashboard" in the url
        if (request.getServletPath().equals("/dashboard")) {

            setUpDashboard(request);

            nextView = "dashboard.jsp";

            // Transfer control over the correct "view"
            RequestDispatcher view = request.getRequestDispatcher(nextView);
            view.forward(request, response);
        } else {

            // record request from android timestamp
            androidRequestTime = new Timestamp(System.currentTimeMillis());

            // getting user choice from http request
            String genreNumber = request.getParameter("genreNumber");

            // record requested genre
            requestedGenre = genreNumberToName[Integer.parseInt(genreNumber)-1];

            // test for invalid mobile app input, genre number has to be a number
            if (genreNumber!= null && genreNumber.matches("[0-9]+")) {

                // pass user choice into model and calculate corresponding result
                // now ptm.g.titles has all the movies in that genre
                startTime = System.currentTimeMillis();
                ptm.fetchData(Integer.parseInt(genreNumber));
                endTime = System.currentTimeMillis();

                // record fetch time
                fetchTime = endTime - startTime;

                // if there is an invalid server input, the titles list will be empty
                if (!ptm.g.titles.isEmpty()) {

                    // record number of shows returned by the 3rd party API
                    numOfShowsReturned = ptm.g.titles.size();

                    // select the first 10 show recommendations
                    List<Title> output10Titles = ptm.g.titles.subList(0, 10);

                    // convert resulting list to json
                    Gson gson = new Gson();
                    String resultJson = gson.toJson(output10Titles);

                    // put the json message in the response
                    PrintWriter out = response.getWriter();
                    out.println(resultJson);

                    // record response to android timestamp
                    androidSentBackTime = new Timestamp(System.currentTimeMillis());

                    // record shows sent back to android
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 9; i++) {
                        sb.append(output10Titles.get(i).toString()).append(", ");
                    }
                    sb.append(output10Titles.get(9).toString());
                    showsSentBack = sb.toString();

                    // save application logs to MongoDB
                    saveLogsToMongoDB();
                }
            }
        }
    }

    private void connectToMongoDB() {

        // connection to MongoDB
        ConnectionString connectionString = new ConnectionString("mongodb://wy2:k2i2t2t2y@ac-i6cidqk-shard-00-00.5x210zx.mongodb.net:27017,ac-i6cidqk-shard-00-01.5x210zx.mongodb.net:27017,ac-i6cidqk-shard-00-02.5x210zx.mongodb.net:27017/test?w=majority&retryWrites=true&tls=true&authMechanism=SCRAM-SHA-1");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);

        // get database
        database = mongoClient.getDatabase("MyDatabase");
    }

    private void saveLogsToMongoDB() {

        // record >=6 pieces of data each request/response about the application

        // information about the request from the mobile phone
        // - timestamp for request
        // - what genre is requested

        // information about the request and reply to the 3rd party API
        // - how long the fetch took
        // - how many shows are returned

        // information about the reply to the mobile phone
        // - timestamp for sending response back to android
        // - what shows are sent back to the user

        // create a document from the 6 recorded values
        // https://www.tutorialspoint.com/how-to-insert-a-document-into-a-mongodb-collection-using-java#:~:text=Connect%20to%20a%20database%20using,created%20above)%20as%20a%20parameter.
        Document d = new Document();

        d.append("Android Request Timestamp", androidRequestTime);
        d.append("Android Requested Genre", requestedGenre);
        d.append("Time to Fetch from 3rd Party API (in miliseconds)", fetchTime);
        d.append("Number of Shows Returned from 3rd Party API", numOfShowsReturned);
        d.append("Android Response Timestamp", androidSentBackTime);
        d.append("Shows Send Back", showsSentBack);

        // insert document to the collection in database
        database.getCollection("MyCollection").insertOne(d);
    }

    private void setUpDashboard(HttpServletRequest request) {

        // pull data from mongoDB and display them in the dashboard jsp page

        // get all documents from database
        //https://www.tutorialspoint.com/how-to-retrieve-all-the-documents-from-a-mongodb-collection-using-java
        FindIterable<Document> iterDoc = database.getCollection("MyCollection").find();
        Iterator<Document> it = iterDoc.iterator();

        // create a list to store all documents pulled from the database
        List<Document> documentList = new ArrayList<>();

        // add all documents to the list
        while (it.hasNext()) {
            documentList.add((Document) it.next());
        }

        // pass document list to jsp
        request.setAttribute("documentList", documentList);

        // local variables for getting the 3 operation analysis
        List<String> genresSearched = new ArrayList<>();
        long sumOfFetchLatency = 0;

        List<Integer> yearsReleased = new ArrayList<>();
        String showTitles;
        String[] splitShowTitles;
        String[] tempYearSplit;
        String tempYear;

        // loop though document list to get the data we want
        for (Document d: documentList) {

            genresSearched.add((String)d.get("Android Requested Genre"));
            sumOfFetchLatency += (long)d.get("Time to Fetch from 3rd Party API (in miliseconds)");

            showTitles = (String) d.get("Shows Send Back");
            splitShowTitles = showTitles.split(", ");

            for (String s: splitShowTitles) {
                tempYearSplit = s.split(" ");
                tempYear = tempYearSplit[tempYearSplit.length - 1];

                if (tempYear.matches("[0-9]+") && !yearsReleased.contains(Integer.parseInt(tempYear))) {
                    yearsReleased.add(Integer.parseInt(tempYear));
                }
            }
        }

        // Most searched show genre
        String mostSearchedGenre = genresSearched.get(0);
        Set<String> mySet = new HashSet<>(genresSearched);
        int maxFrequency = 0;
        int frequency;

        for (String s: mySet) {

            frequency = Collections.frequency(genresSearched,s);

            if (frequency > maxFrequency) {
                mostSearchedGenre = s;
                maxFrequency = frequency;
            }
        }

        request.setAttribute("mostSearchedGenre", mostSearchedGenre);

        // average fetch latency across genres
        long averageFetchLatency;
        averageFetchLatency = sumOfFetchLatency / documentList.size();

        request.setAttribute("averageFetchLatency", averageFetchLatency);

        // Show released years for this recommendation app
        Collections.sort(yearsReleased);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < yearsReleased.size() - 1; i++) {
            sb.append(yearsReleased.get(i)).append(", ");
        }
        sb.append(yearsReleased.get(yearsReleased.size() - 1));
        String yearsReleasedString = sb.toString();

        request.setAttribute("yearsReleased", yearsReleasedString);
    }
}