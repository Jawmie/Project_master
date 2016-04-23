package mainPack;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Jawmie on 02/04/2016.
 */
public class AirServletMod extends HttpServlet {
    private Connection connection;
    private PreparedStatement insertdata, roomquery;
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

            // PreparedStatement to add one to vote total for a
            insertdata =
                    connection.prepareStatement(
                            "INSERT INTO roomdata ( temperature, light, room, datelogged )  " +
                                    "VALUES  ( ? , ? , ? , CURRENT_DATE() ) " );

        } catch (Exception e) {  // NullPointerException and JSONException
            // Use default values assigned before the try block
        }
    }// end of init

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException,
            java.io.IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter( );

        String inputString1 = request.getParameter("temperature");
        System.out.println(inputString1);

        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject obj = (JSONObject) jsonParser.parse(inputString1);
            String room = (String) obj.get("roomVal");
            String light = (String) obj.get("lightVal");
            String temp = (String) obj.get("tempVal");

            //send data to database
            insertdata.setString( 1, temp );
            insertdata.setString( 2, light);
            insertdata.setString( 3, room);
            insertdata.executeUpdate();
        }
        // if database exception occurs, return error page
        catch ( SQLException sqlException ) {
            sqlException.printStackTrace();
            out.println( "<title>Error</title>" );
            out.println( "</head>" );
            out.println( "<body><p>Database error occurred. " );
            out.println( "Try again later.</p></body></html>" );
            out.close();
        }
        catch(ParseException e){
            e.printStackTrace();
        }
    }// end of doPost

    // close SQL statements and database when servlet terminates
    public void destroy()
    {
        // attempt to close statements and database connection
        try {
            //updateVotes.close();
            //totalVotes.close();
            insertdata.close();
            //roomquery.close();
            //result.close();
            //connection.close();
        }
        // handle database exceptions by returning error to client
        catch( SQLException sqlException ) {
            sqlException.printStackTrace();
        }
    }  // end of destroy method
}
