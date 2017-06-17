
package com.src.server.actions;

import com.idea.builders.ActivityLog;
import com.idea.builders.Config;
import static com.src.server.actions.CommonActions.getEmpnoFromUid;
import static com.src.server.actions.CommonActions.remFile;
import static com.src.server.actions.SideWork.addLeadingZero;
import static com.src.server.actions.SideWork.addLeadingZeroToBCode;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AttendanceProcess {
     /*09*/ public static  String addAttandence(String sfEmpID, String sIPAddr, String sRemarks, String sFlag, String branchCode, String oraConInfo, String oraConName, String oraConPass, String log, File file,ActivityLog activityLog) {
        try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

            String sql = "INSERT INTO ATTENDANCE(TSTAMP, EMPID, IPADDR, REMARKS, SFLAG,COMPANY_CODE) VALUES( CURRENT_TIMESTAMP ,  '" + sfEmpID + "',  '" + sIPAddr + "',  '" + sRemarks + "', '" + sFlag + "', '" + branchCode + "')";
            PreparedStatement pstmt = con.prepareStatement(sql);

            //Blob blob = con.createBlob();
            //blob.setBytes(1, finISO);    
//            pstmt.setString(1, sEmpno);
//            pstmt.setString(2, sFNo);
            // pstmt.setBytes(1, finISO);
            //pstmt.setBlob(3, blob);
            pstmt.executeUpdate();
            con.commit();
            con.close();

        } catch (ClassNotFoundException e) {
            System.out.println(e);
            activityLog.setRESPONSE_CODE("NOO 09 3");
               activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
               activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 09 3 Unable to connect to database";
        } catch (SQLException e) {
            System.out.println(e);
            activityLog.setRESPONSE_CODE("NOO 09 4");
               activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
               activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 09 4 Unable to connect to database";
        }
        return "OK";
    }

 /*10*/ public static String attendFP(byte[] finISO, String clientIp, String oraConInfo, String oraConName, String oraConPass, String log, File file,ActivityLog activityLog) {
         long err;
        byte kbBuffer[] = new byte[100];
        byte kbWhichFinger[] = new byte[100];
        int fingerLength = 0;
        String finger = new String("Finger");
        int a1 = 1;
        int b1 = 1;
        String sResStr = "Mismatch";
        String fpRes = "";
        byte[][] bytewr = new byte[31][800];
        FileWriter fw = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        String sUid = "";
        String sEmpName = "";
        String EmpID = null;
        String FileAddr = "";
        String sEmpBranch = "";
        String modifiedClientIp = addLeadingZero(clientIp);
        int totalFetched=0;
        try {

            String timeStamp = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
            FileAddr = Config.MATCHING_FILE_PATH + clientIp + "123456789" + timeStamp + ".iso";
            fos = new FileOutputStream(FileAddr);
            bos = new BufferedOutputStream(fos);
            try {
                int i = 0;
                int k = 1;
                bytewr[0] = finISO;
                try {
                    //step1 load the driver class
                    Class.forName("oracle.jdbc.driver.OracleDriver");

                    //step2 create  the connection object
                    Connection conn = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

                    //   System.out.println("connected oracle\n");
                    //step3 create the statement object
                    Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.TYPE_FORWARD_ONLY);
                    //step4 execute query
                    String query = "SELECT BMDATA.EMPNO,BMDATA.BDATA,BMDATA.FNO,LOCALEMPRCD.NICNO,LOCALEMPRCD.NAME,LOCALEMPRCD.USERID,LOCALEMPRCD.BRANCH, IP_RANGE.STATUS FROM BMDATA,LOCALEMPRCD,IP_RANGE WHERE LOCALEMPRCD.BRANCH = " + "get_branch_from_ip" + "('" + modifiedClientIp + "') AND BMDATA.EMPNO=LOCALEMPRCD.EMPLOYEEID AND IP_RANGE.STATUS='Active'AND IP_RANGE.BRANCH= LOCALEMPRCD.BRANCH ORDER BY BMDATA.EMPNO";
                    activityLog.setAttendCall(true);
                    ResultSet rs = stmt.executeQuery(query);
               
                    if (!rs.next()) {

                        ResultSet res = stmt.executeQuery("SELECT * FROM IP_RANGE WHERE BRANCH=" + "get_branch_from_ip" + "('" + modifiedClientIp + "')");
                        if (res.next()) {
                            if (!(res.getString("STATUS")).contains("Active")) {
                                activityLog.setRESPONSE_MESSAGE("This branch is Inactive for Biometric Attendance");
                                activityLog.setSTATUS(activityLog.FAIL);
                                activityLog.setRESPONSE_CODE("NOO 00 0");
                                return "NOO 00 0 This branch is Inactive for Biometric Attendance";
                            }

                        }
                    } else {
                              activityLog.setAttendCall(false);
                        rs.last();
                        totalFetched=rs.getRow();
                        rs.beforeFirst();

                    }

                    Blob blob;
                    String empTmp = "";
                    String empName = "";
                    String empBranch = "";
                    //System.out.println("Total Fetched="+totalFetched);
                   int counter=0;
                    boolean lastFound=false;
                    boolean otherLast=false;
                    String totStr=Integer.toString(totalFetched);
                    if(totStr.equalsIgnoreCase("1")){
                        lastFound=true;
                        otherLast=true;
                    }
                       while (rs.next()) {
                        blob = rs.getBlob("BDATA");
                        EmpID = rs.getString("EMPNO");
                        sEmpName = rs.getString("Name");
                        sEmpBranch = rs.getString("BRANCH");
                        // System.out.println(EmpNo);
                        int blobLength = (int) blob.length();
                       // System.out.println("\nEmpID: \"" + EmpID + "\" and empTmp: \"" + empTmp + "\"");
                            if ("" == empTmp && lastFound==false) {
                               // System.out.println("In  empty");
                             empTmp = EmpID;
                            empName = sEmpName;
                            empBranch = sEmpBranch;
                            bytewr[k] = blob.getBytes(1, blobLength);
                            k++;
                            i++;
                            counter++;
                              if(counter==totalFetched-1){
                             lastFound=true;
                            }
                             } 
                            else if (empTmp.compareTo(EmpID) == 0 && lastFound==false && counter!=totalFetched-1) {
                            // System.out.println("In  compare");
                             empTmp = EmpID;
                            bytewr[k] = blob.getBytes(1, blobLength);
                            k++;
                            i++;
                            counter++;
                              // continue;
                        } else {
                            if (lastFound && counter==totalFetched-1 && (empTmp.compareTo(EmpID) == 0 || otherLast==true)) {
                           // System.out.println("Ciunter in if"+counter);
                             //System.out.println("\nEmpID: \"" + EmpID + "\" and empTmp: \"" + empTmp + "\"");
                            empTmp = EmpID;
                            empName = sEmpName;
                            empBranch = sEmpBranch;
                            bytewr[k] = blob.getBytes(1, blobLength);
                            k++;
                            i++;
                            counter++;
                            //continue;
                        }
                                 for (int j = 0; j < k; j++) {
                                bos.write(bytewr[j]);
                            }
                            if(counter==totalFetched-1){
                                lastFound=true;
                                otherLast=true;
                            }
                            System.out.print("|TFound=" + i);
                              //  System.out.println("EmpID "+empTmp);
                            bos.flush();
                            fos.close();
                            bos.close();

                            String consoleStatmnt = Config.MATCHING_API_PATH + " " + FileAddr;
                            try {
                                Process p1 = Runtime.getRuntime().exec(consoleStatmnt);
                                p1.waitFor();
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(p1.getInputStream())
                                );
                                String line;
                                line = reader.readLine();
                                if (line == null) {
                                    System.err.print("|DvrFult");
                                    activityLog.setRESPONSE_CODE("NOO 10 8");
                                    activityLog.setRESPONSE_MESSAGE("Matching error (Not Matched)");
                                    activityLog.setSTATUS(activityLog.FAIL);
                                    return "NOO 10 8 Matching error (Not Matched)";
                                    //line = "0";
                                }

                                int ch = Integer.parseInt(line);
                                System.out.print("|DvcRes=" + ch);

                                switch (ch) {
                                    case 0: {
                                        activityLog.setRESPONSE_CODE("NOO 10 2");
                                        activityLog.setRESPONSE_MESSAGE("Matching error");
                                        activityLog.setSTATUS(activityLog.FAIL);
                                        return "NO0 10 2 Matching error";
                                    }
                                    case 1: {
                                        activityLog.setRESPONSE_CODE("NOO 10 3");
                                        activityLog.setRESPONSE_MESSAGE("Matching error");
                                        activityLog.setSTATUS(activityLog.FAIL);
                                        return "NO0 10 3 Matching error";
                                    }
                                    case 4: {
                                        String resul = addAttandence(empTmp, clientIp, "remarks", "ok", empBranch, oraConInfo, oraConName, oraConPass, log, file, activityLog);
                                        if (resul.contains("OK")) {
                                            String res = addDailyAttendance(empTmp, empBranch, clientIp, oraConInfo, oraConName, oraConPass, activityLog);
                                            if (res.contains("OK")) {
                                                String AttendTime = new SimpleDateFormat("hh:mm:ss a").format(Calendar.getInstance().getTime());
                                                remFile(FileAddr);
                                                System.out.print("|Verified");
                                                log = log + "Verified";
                                                activityLog.setRESPONSE_CODE("YOO 00 0");
                                                activityLog.setRESPONSE_MESSAGE("Welcome|" + empTmp + "|" + empName + "|Time: " + AttendTime);
                                                activityLog.setSTATUS(activityLog.SUCCESS);
                                                return "YOO 00 0 Welcome|" + empTmp + "|" + empName + "|Time: " + AttendTime;
                                            } else {
                                                remFile(FileAddr);
                                                System.out.println("|Verified but not logged in database for Daily Attendace");
                                                return res;
                                            }

                                        } else {
                                            System.out.print("|Verified but not loged in database");
                                            // writeLog("|Verified but not loged in database", bufferedwriter);
                                            log = log + "|Verified but not loged in database";
                                            return resul;
                                        }
                                    }
                                    
                                }
                            } catch (IOException ex) {
                                System.out.println(ex);
                                activityLog.setRESPONSE_CODE("NOO 10 2");
                                activityLog.setRESPONSE_MESSAGE("I/O error");
                                activityLog.setSTATUS(activityLog.FAIL);
                                return "NOO 10 2 I/O error";
                            } catch (InterruptedException ex) {
                                System.out.println(ex);
                                activityLog.setRESPONSE_CODE("NOO 10 1");
                                activityLog.setRESPONSE_MESSAGE("Timeout Disconnected");
                                activityLog.setSTATUS(activityLog.FAIL);
                                return "NOO 10 1 Timeout Disconnected";
                            } finally {
                                remFile(FileAddr);
                            }
                            i = 0;
                            k = 1;
                            empTmp = "";
                            if(counter!=totalFetched){
                            rs.previous();
                            }
                            timeStamp = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
                            FileAddr = Config.MATCHING_FILE_PATH + clientIp + "123456789" + timeStamp + ".iso";
                            fos = new FileOutputStream(FileAddr);
                            bos = new BufferedOutputStream(fos);

                        }
                    }
                      // System.out.println("Total Counter"+counter);
                     rs.close();
                    stmt.close();
                    conn.close();
                    remFile(FileAddr);
                    activityLog.setRESPONSE_CODE("NOO 10 6");
                    activityLog.setRESPONSE_MESSAGE("Not Matched");
                    activityLog.setSTATUS(activityLog.FAIL);
                    return "NOO 10 6 Not Matched";

                } catch (SQLException ex) {
                    System.err.println(ex);
                    int errorCode=ex.getErrorCode();
                    String code=Integer.toString(errorCode);
                    if(code.equalsIgnoreCase("1422")){
                        activityLog.setRESPONSE_CODE("NOO 10 6");
                    activityLog.setRESPONSE_MESSAGE("Invalid Branch");
                    activityLog.setSTATUS(activityLog.FAIL);
                    return "NOO 10 6 Invalid Branch";
                    }
                    activityLog.setRESPONSE_CODE("NOO 10 3");
                    activityLog.setRESPONSE_MESSAGE("Unable to connect database");
                    activityLog.setSTATUS(activityLog.FAIL);
                    return "NOO 10 3 Unable to connect database";
                } catch (ClassNotFoundException ex) {
                    System.err.println("EXCEPTION: " + ex);
                    activityLog.setRESPONSE_CODE("NOO 10 4");
                    activityLog.setRESPONSE_MESSAGE("Unable to connect database");
                    activityLog.setSTATUS(activityLog.FAIL);
                    return "NOO 10 4 Unable to connect database";
                }

            } catch (FileNotFoundException ex) {
                remFile(FileAddr);
                System.out.println(ex);
                activityLog.setRESPONSE_CODE("NOO 10 2");
                activityLog.setRESPONSE_MESSAGE("Unable to write");
                activityLog.setSTATUS(activityLog.FAIL);
                return "NOO 10 2 Unable to write";
            } catch (IOException ex) {
                remFile(FileAddr);
                System.out.println(ex);
                activityLog.setRESPONSE_CODE("NOO 10 2");
                activityLog.setRESPONSE_MESSAGE("I/O error");
                activityLog.setSTATUS(activityLog.FAIL);
                return "NOO 10 2 I/O error";
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 10 2");
            activityLog.setRESPONSE_MESSAGE("Unable to write");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 10 2 Unable to write";
        } catch (IOException ex) {
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 10 2");
            activityLog.setRESPONSE_MESSAGE("I/O error");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 10 2 I/O error";
        }
    }
 /*21*/ public static  String addDailyAttendance(String sfEmpID, String branchCode,String branchIp,String oraConInfo, String oraConName, String oraConPass,ActivityLog activityLog){
      try {
            //step1 load the driver class  
          String modifiedBCode=addLeadingZeroToBCode(branchCode);
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);
            Statement stmtCheck = con.createStatement();
            ResultSet rsCheck = stmtCheck.executeQuery("Select * From DAILY_ATTEND where \"DATE\"=to_date(sysdate,'DD/MM/YYYY') and empid='" + sfEmpID + "' and FLAG='IN'");
             if (rsCheck.next()) {
                System.out.println("Found");
                try {
                    int count=0;
                     Statement stmt = con.createStatement();
                     ResultSet rsFlag = stmtCheck.executeQuery("Select * From DAILY_ATTEND where \"DATE\"=to_date(sysdate,'DD/MM/YYYY') and empid='" + sfEmpID + "' and FLAG='OUT'");
                    if(rsFlag.next()){
                    count = stmt.executeUpdate("UPDATE DAILY_ATTEND SET SWIPE_TIME=CURRENT_TIMESTAMP WHERE \"DATE\"=to_date(sysdate,'DD/MM/YYYY') and empid='" + sfEmpID + "'AND FLAG='OUT'");
                    }
                    else{
                      count = stmt.executeUpdate("INSERT INTO DAILY_ATTEND (EMPID,BRANCH_CODE,BRANCH_IP,FLAG) VALUES('" + sfEmpID + "','"+modifiedBCode+"','"+branchIp+"','OUT')");  
                    }
                    System.out.println(count + " Out");

                } catch (SQLException ex) {
                    System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 21 4");
               activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
               activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 21 4 Unable to connect to database";
                }
            } else {
                try {
                    Statement stmt = con.createStatement();
                    int count = stmt.executeUpdate("INSERT INTO DAILY_ATTEND (EMPID,BRANCH_CODE,BRANCH_IP,FLAG) VALUES('" + sfEmpID + "','"+modifiedBCode+"','"+branchIp+"','IN')");
                      System.out.println(count + " IN");
                } catch (SQLException ex) {
                   System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 21 4");
               activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
               activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 21 4 Unable to connect to database";
                }
            }
            con.commit();
            con.close();

        } catch (ClassNotFoundException e) {
            System.out.println(e);
            activityLog.setRESPONSE_CODE("NOO 21 3");
               activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
               activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 21 3 Unable to connect to database";
        } catch (SQLException e) {
            System.out.println(e);
            activityLog.setRESPONSE_CODE("NOO 21 4");
               activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
               activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 21 4 Unable to connect to database";
        }
     return "OK";
 }
}
