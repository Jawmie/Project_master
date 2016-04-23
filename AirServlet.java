package mainPack;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * Created by Jawmie on 01/04/2016.
 */

public class AirServlet extends HttpServlet {
    private Connection connection;
    private PreparedStatement roomquery, allQuery;
    final String DEGREE  = "\u00b0";

    // set up database connection and prepare SQL statements
    public void init( ServletConfig config )
            throws ServletException
    {
        try {
            String driver = "com.mysql.jdbc.Driver";
            Class.forName( driver );
            // Step 1: Create a database "Connection" object
            // For MySQL
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/airproj",
                    "root", "AirProj2016!");

            allQuery = connection.prepareStatement("SELECT * FROM roomdata");

            roomquery =
                    connection.prepareStatement(
                            "SELECT room, temperature, light," +
                                    "DATE_FORMAT(datelogged, '%d-%m-%Y') as datelogged" +
                                    " FROM roomdata WHERE room = ? AND datelogged BETWEEN ? AND ?"
                    );

        } catch (Exception e) {  // NullPointerException and JSONException
            // Use default values assigned before the try block
        }
    } // End of init

    @Override
    protected void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException,
            java.io.IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String roomData = request.getParameter("room");
        String datefData = request.getParameter("datef");
        String datetData = request.getParameter("datet");

        System.out.println(dateRearrange(datefData));
        System.out.println(dateRearrange(datetData));

        try {
            out.println("<html><head><title>Air Quality Module</title>" +
                    "<link rel=\"stylesheet\" href=\"styles.css\">" +
                    "</head><body>" +
                    "<meta http-equiv=\"refresh\" content=\"30\" /></head><body>");
            out.println("<h2>Results as follows:</h2>");
            out.println("<p><font color=white face=\"verdana\"><h3>Date range: </h3>" +
                    dateRearrange(datefData) + "<b> to </b>" + dateRearrange(datetData) + "</font></p>");

            out.println( "<p><INPUT TYPE=\"button\" style=\"background-image:url('img/searchAnButt.png'); width: 146px; height: 24px;\" onClick=\"history.go(-1)\"></p>" );

            out.println("<div class=\"scroll\"><table style=\"width:100%\">"
                    + "<tr><th><font color=white face=\"verdana\">Room</font></th>" +
                    "<th><font color=white face=\"verdana\">Temperature</font></th>" +
                    "<th><font color=white face=\"verdana\">Light(lux)</font></th>" +
                    "<th><font color=white face=\"verdana\">Date Logged</font></th>");

              if(roomData == "" && datefData == "" && datetData == "") {
                  ResultSet rset = allQuery.executeQuery();

                  int count = 0;
                  while (rset.next()) {
                      out.println(
                              "<tr align=\"center\"><td><font color=white face=\"verdana\">" + rset.getString("room") + "</font></td>" +
                                      "<td><font color=white face=\"verdana\">" + rset.getString("temperature") + " " + DEGREE + "C" + "</font></td>" +
                                      "<td><font color=white face=\"verdana\">" + rset.getString("light") + " lux" + "</font></td>" +
                                      "<td><font color=white face=\"verdana\">" + rset.getString("datelogged") + "</font></td>" +
                                      "</font></td></tr>");
                      ++count;
                  }
                  out.println("</div></table>");
                  out.println("<t><p>==== " + count + " records found ====</p></t>");
              }
              else {
                  roomquery.setString(1, checkData(roomData));
                  roomquery.setString(2, checkData(datefData));
                  roomquery.setString(3, checkData(datetData));
                  ResultSet rset = roomquery.executeQuery();

                  int count = 0;
                  while (rset.next()) {
                      out.println(
                              "<tr align=\"center\"><td><font color=white face=\"verdana\">" + rset.getString("room") + "</font></td>" +
                                      "<td><font color=white face=\"verdana\">" + rset.getString("temperature") + " " + DEGREE + "C" + "</font></td>" +
                                      "<td><font color=white face=\"verdana\">" + rset.getString("light") + " lux" + "</font></td>" +
                                      "<td><font color=white face=\"verdana\">" + rset.getString("datelogged") + "</font></td>" +
                                      "</font></td></tr>");
                      ++count;
                  }
                  out.println("</div></table>");
                  out.println("<t><p>==== " + count + " records found ====</p></t>");
              }

            out.println("</body></html>");

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<title>Error</title>");
            out.println("</head>");
            out.println("<body><p>Database error occurred. ");
            out.println("Try again later.</p></body></html>");
            out.close();
        } finally {
            out.close();
            /*try {
                // Step 5: Close the Statement and Connection
                if (connection != null) connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }*/
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request,response);
    }

    public String dateRearrange(String rearr){
        if (rearr == "") {
            String result = "No Date";
            return result;
        }
        else {
            String str1[] = rearr.split("-");
            String last = str1[0];
            String middle = str1[1];
            String first = str1[2];

            String result = first + "-" + middle + "-" + last;
            return result;
        }
    }

    public String checkData(String data){
        String blank = "";

        if(data.compareTo(blank) == 0){
            data = "No Entry";
        }
        return data;
    }// End of checkData

    // close SQL statements and database when servlet terminates
    public void destroy()
    {
        // attempt to close statements and database connection
        try {
            //updateVotes.close();
            //totalVotes.close();
           // insertdata.close();
            roomquery.close();
            allQuery.close();
            //result.close();
            connection.close();
        }
        // handle database exceptions by returning error to client
        catch( SQLException sqlException ) {
            sqlException.printStackTrace();
        }
    }  // end of destroy method
}
