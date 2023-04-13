<%@ page import="org.bson.Document" %>
<%@ page import="java.util.List" %>

<%--
  Created by IntelliJ IDEA.
  User: wentingyu
  Date: 4/6/23
  Time: 9:25 PM
  @author Wenting Yu
  Andrew ID: wy2
  This JSP is the dashboard for this web service. It record all the operation analysis and logs from the client.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>Dashboard</title>
    </head>
    <style>
        table, th, td {
            border:1px solid black;
        }
    </style>
    <body>

        <h1>Dashboard</h1>

            <h2>Operations Analysis</h2>

                <h3>Most Searched Show Genre: <%=request.getAttribute("mostSearchedGenre")%></h3>
                <h3>Average Fetch Latency from 3rd Party API (in milliseconds): <%=request.getAttribute("averageFetchLatency")%></h3>
                <h3>Years Recommended Shows are Released: <%=request.getAttribute("yearsReleased")%></h3>

            <h2>Logs</h2>

            <table style="width:100%">
                <tr>
                    <th>Android Request Timestamp</th>
                    <th>Android Requested Genre</th>
                    <th>Time to Fetch from 3rd Party API (in miliseconds)</th>
                    <th>Number of Shows Returned from 3rd Party API</th>
                    <th>Android Response Timestamp</th>
                    <th>Shows Send Back</th>
                </tr>
                    <!--table entries for all logs-->

                    <!-- get document lists -->
                    <% List<Document> documentList = (List<Document>) request.getAttribute("documentList");%>

                    <!-- loop through each document to print them as table entries -->
                    <% for (int i = 0; i < documentList.size(); i++ ) { %>

                <tr>
                    <td><%= documentList.get(i).get("Android Request Timestamp") %></td>
                    <td><%= documentList.get(i).get("Android Requested Genre") %></td>
                    <td><%= documentList.get(i).get("Time to Fetch from 3rd Party API (in miliseconds)") %></td>
                    <td><%= documentList.get(i).get("Number of Shows Returned from 3rd Party API") %></td>
                    <td><%= documentList.get(i).get("Android Response Timestamp") %></td>
                    <td><%= documentList.get(i).get("Shows Send Back") %></td>
                </tr>

                    <% } %>

            </table>
    </body>
</html>
