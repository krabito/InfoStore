//package InfoStore;

import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class InfoStore extends HttpServlet implements SingleThreadModel {

//Initialize global variables
boolean Debug = true;     // Debugging purposes.
protected Connection dbCon = null; //database connection at startup.
protected Statement stmt = null;
protected ResultSet rs = null;
protected String debugLogFile = null;
private final String infoUpdate = "Update";
private final String infoSubmit = "Submit";
private final String infoRemove = "Remove";

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    //Doc Storage
    try { docPath = getInitParameter("docDir"); }
    catch (Exception e) {
      getServletContext().log("ERROR: docDir not defined!");
    }
    try { debugLogFile = getInitParameter("debugFile"); }
    catch (Exception e) {
      getServletContext().log("ERROR: debug file not defined!");
    }
    try{
      // Try connecting to the database.
      DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
      dbCon = DriverManager.getConnection("jdbc:oracle:thin:@wabbit:1525:JAVADB", "infostorerw", "docstore");
      dbCon.setAutoCommit(true);
      stmt = dbCon.createStatement();
    }
    catch (SQLException e){
      getServletContext().log("DB Connection Error" + e.getMessage());
    }
  }
  String docPath = "";
//Process the HTTP Get request

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    PrintWriter out = new PrintWriter (response.getOutputStream());
    out.println("<html>");
    out.println("<head><title>InfoStore</title></head>");
    out.println("<body>");
    out.println("</body></html>");
    out.close();
  }
//Process the HTTP Post request
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  String resultString = "";

    //Submit,Remove,Update
    String data_Action = "";
    try {
      data_Action = request.getParameter("data_Action");

    }
    catch (Exception e) { logMessage(e.getMessage()); }

        //Get Category
    String data_Category = "";
    try {
      data_Category = request.getParameter("data_Category");
     }
    catch (Exception e) { logMessage(e.getMessage()); }

    //Document Index
    String data_Proc = "";
    if ( ! data_Action.equalsIgnoreCase(infoSubmit)){ // Don't need data_Proc for submittals.

      //get the Index
      try { data_Proc = request.getParameter("data_Proc"); }
      catch (Exception e) { logMessage(e.getMessage()); }
    }
    //Document Title
    String data_Title = "";
    try { data_Title = request.getParameter("data_Title"); }
    catch (Exception e) { logMessage(e.getMessage()); }

    //Document Revision
    String data_Rev = "";
    try { data_Rev = request.getParameter("data_Rev"); }
    catch (Exception e) { logMessage(e.getMessage()); }

    //Document Date
    String data_Update = "";
    try { data_Update = request.getParameter("data_Update"); }
    catch (Exception e) { logMessage(e.getMessage()); }

    //Document Authore
    String data_Author = "";
    try { data_Author = request.getParameter("data_Author"); }
    catch (Exception e) { logMessage(e.getMessage()); }

    //Document Text
    String data_Text = "";
    try { data_Text = request.getParameter("data_Text"); }
    catch (Exception e) { logMessage(e.getMessage()); }

    //Author Email
    String data_Email = "";
    try { data_Email = request.getParameter("data_Email"); }
    catch (Exception e) { logMessage(e.getMessage()); }

    //Submit
    String submit = "Submit";
    try { submit = request.getParameter("Submit Button"); }
    catch (Exception e) { logMessage(e.getMessage()); }

    //Reset
    String reset = "Reset";
    try { reset = request.getParameter("Reset Button"); }
    catch (Exception e) { logMessage(e.getMessage()); }

    response.setContentType("text/html");  // Return type to client.

    // Submit information.....
    if( data_Action.equalsIgnoreCase(infoSubmit) ){

     /** Create a timeStamp to search on after the
        database update.  Need to search on this
        timeStamp to get index in database for use
        in the html file name, i.e. "indexnumber".html */

      long timeKey = System.currentTimeMillis();
      Long timeKeyObj = new Long(timeKey);
      String timeStamp = timeKeyObj.toString();
      timeKey = 0;
      timeKeyObj = null;

      // Database insert is next.
      String buildStmt = "insert into DOC values(0, '" + data_Title + "', " + data_Rev +
                         ", TO_DATE('" + data_Update + "'), '" + data_Author +
                         "', '" + data_Email + "', '" + timeStamp + "', 'stuff', '" +
                         data_Category + "')";

      try{
          stmt.executeUpdate(buildStmt);
     }
      catch ( SQLException e) {
        getServletContext().log("Insert Failed" + e.getMessage());
        writeLog(debugLogFile,e.getMessage());
        destroy();
      }

      // Query database with timeStamp to get index.
      String indexQuery = "select doc_id from doc where doc_datekey=" + timeStamp;
      try{
          ResultSet rs = stmt.executeQuery(indexQuery);

        // Write html file to dirPath.

          while (rs.next()) {
            Integer rsInt = new Integer(rs.getInt("doc_id"));
            resultString += rsInt.toString();
           }   // End while.
          // update the filename column.
          buildStmt = "update DOC set doc_filename = '" + resultString +
                      ".html' where doc_id = " + resultString;
          stmt.executeUpdate(buildStmt);
      }
      catch (SQLException e){
         writeLog(debugLogFile,buildStmt);
         getServletContext().log("Update Failed: " + e.getMessage());
      }

      try {              // false: Create new data file.
          writeHtml(docPath,resultString +".html",false);
      }
      catch ( IOException e){
        getServletContext().log("Error, Write HTML, Sequence Error.");
        destroy();  // Need to shutdown.
      }
    } // End if Submit.

    //  Information Update....

    if( data_Action.equalsIgnoreCase(infoUpdate) ){
     /** Create a timeStamp to search on after the
        database update.  Need to search on this
        timeStamp to get index in database for use
        in the html file name, i.e. "indexnumber".html */

      long timeKey = System.currentTimeMillis();
      Long timeKeyObj = new Long(timeKey);
      String timeStamp = timeKeyObj.toString();
      timeKey = 0;
      timeKeyObj = null;

      // Database update is next.
      String buildStmt = "update DOC set doc_title = '" + data_Title + "', doc_rev = " + data_Rev +
                         ",doc_date = TO_DATE('" + data_Update + "'), doc_Author = '" + data_Author +
                         "', doc_Auth_email = '" + data_Email + "', doc_datekey = '" + timeStamp +
                         "'where doc_id = " + data_Proc;

      // writeLog(debugLogFile,buildStmt);  // For debugging
      try{
          stmt.executeUpdate(buildStmt);
     }
      catch ( SQLException e) {
        getServletContext().log("Update failed: " + e.getMessage());
        destroy();
      }

      // Query database with timeStamp to get index.
      String indexQuery = "select doc_id from doc where doc_datekey=" + timeStamp;
      try{
          ResultSet rs = stmt.executeQuery(indexQuery);

        // Write html file to dirPath.

          while (rs.next()) {
            Integer rsInt = new Integer(rs.getInt("doc_id"));
            resultString += rsInt.toString();
           }   // End while.
      }
      catch (SQLException e){
        writeLog(debugLogFile,buildStmt);
        getServletContext().log("SQLException: " + e.getMessage());
      }
      try {              // true: Update existing data file.
          writeHtml(docPath,resultString +".html",true);
      }
      catch ( IOException e){
        getServletContext().log("Error, Update Sequence Error." + e.getMessage());
        destroy();  // Need to shutdown.
      }
    } // End if Update.
    
    if( data_Action.equalsIgnoreCase(infoRemove) ){
     /** Create a timeStamp to search on after the
        database update.  Need to search on this
        timeStamp to get index in database for use
        in the html file name, i.e. "indexnumber".html */

      long timeKey = System.currentTimeMillis();
      Long timeKeyObj = new Long(timeKey);
      String timeStamp = timeKeyObj.toString();
      timeKey = 0;
      timeKeyObj = null;

      // Delete database entry.
      String buildStmt = "delete from DOC where doc_id = " + data_Proc;
      writeLog(debugLogFile,buildStmt);
      try{
          stmt.executeUpdate(buildStmt);
     }
      catch ( SQLException e) {
        getServletContext().log("Delete failed: " + e.getMessage());
        destroy();
      }

    } // End if Remove.

    // Need to responsed.
    PrintWriter out = new PrintWriter (response.getOutputStream());
    out.println("<html>");
    out.println("<head><title>InfoStore Response</title></head>");
    out.println("<body>");
    out.println("<pre>");
    out.println("</pre>");
    out.println("<b>Your Document: " + resultString + ".html</b>");
    out.println(" Thank You!");
    out.println("</body></html>");
    out.close();
  }

  //Get Servlet information

  public String getServletInfo() {
    return "InfoStore.InfoStore Information";
  }
//  private void logException(Exception e, String eMsg){
//     getServletContext().log(e,eMsg);
//  }
  private void logMessage(String logMsg){
     log(logMsg);
  }
  public void writeHtml(String htmlPath, String htmlFile, boolean upDate)throws IOException{
     File urlFile = new File(htmlPath + htmlFile);

     // If file exists and update is false (meaning a new file to create),
     // something is not right.  Abort so not to overwrite an
     // existing file.

     // File exists.
     if(urlFile.exists() && !upDate) throw new IOException("File Exists: " + urlFile.toString());

     // Return no file to update.
     if(!urlFile.exists() && upDate) throw new IOException("No File to Update: " + urlFile.toString());

     try{
        PrintWriter updateOut = new PrintWriter( new BufferedWriter(new FileWriter(urlFile)));
        updateOut.println("<html>");
        updateOut.println("<head><title></title></head>");
        updateOut.println("<body>");
        updateOut.println("<pre>");
        updateOut.println("</pre>");
        updateOut.println("<b>Your Document: </b>");
        updateOut.println(" Thank You!");
        updateOut.println("</body></html>");
        updateOut.close();
     }
     catch (IOException e){
         throw new IOException("Error on Open: " + urlFile.toString() + e.getMessage());
      }
  } // End Method writeHtml.

    private void writeLog(String logPath, String eventToLog){

    String [] monthIndex = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    Calendar rightNow = Calendar.getInstance();
    String logTimeStamp = monthIndex[rightNow.get(Calendar.MONTH)] + " " + rightNow.get(Calendar.DAY_OF_MONTH);
    logTimeStamp += " " + rightNow.get(Calendar.HOUR_OF_DAY) + ":" + rightNow.get(Calendar.MINUTE);
    logTimeStamp += ":" + rightNow.get(Calendar.SECOND);

 try{      // open to append a logger file to write to.
    FileOutputStream outSt = new FileOutputStream(logPath,true);
    PrintWriter outLog = new PrintWriter(outSt);
    outLog.println(logTimeStamp + " " + eventToLog);
    outLog.flush();
    outLog.close(); // Close the PrintWriter.
    outSt.close();
    }
    catch (IOException e) {
      System.out.println("Cannot write to Log File: " + logPath);
      System.out.println( e.getMessage());
    } // At this point, can not write to the logger file or to
      // a webserver log so this message basically falls on deaf ears.
  }
  

  // need to close the database connection when Servlet is stopped.
  public void destroy() {
    if (dbCon != null ) {
      try {
        dbCon.close();
        }
      catch (SQLException e) {
        getServletContext().log("Close Database Error! " + e.getMessage());
        }
    }
  }
}

