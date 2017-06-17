package com.src.server.actions;

import com.idea.builders.ActivityLog;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonActions {
    /*05*/ public static String checkUsr(String sUid, String sCnic, String oraConInfo, String oraConName, String oraConPass, String log, File file, ActivityLog activityLog) {
        String EmpName = null;
        //select * from VIEWEMPIBST24 where (NICNO='" + sUsrCNIC + "' AND EMPLOYEEID='" + sUid + "') "
        try {
            Integer.parseInt(sUid);
        } catch (Exception e) {
            activityLog.setRESPONSE_CODE("NOO 05 9");
            activityLog.setRESPONSE_MESSAGE("Invalid input");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 05 9 Invalid input";
        }
        try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

            //step3 create the statement object  
            Statement stmt = con.createStatement();

            //step4 execute query  SELECT   BIOSEC.BMDATA.FNO FROM   BIOSEC.BMDATA WHERE  BIOSEC.BMDATA.EMPNO = 12345
            StringBuilder sUsrCNIC = new StringBuilder("00000-0000000-9");
            for (int i = 0; i < 15; i++) {
                if (i < 5) {
                    sUsrCNIC.setCharAt(i, sCnic.charAt(i));
                } else if (i == 5) {
                    sUsrCNIC.setCharAt(i, '-');
                } else if (i < 13) {
                    sUsrCNIC.setCharAt(i, sCnic.charAt(i - 1));
                } else if (i == 13) {
                    sUsrCNIC.setCharAt(i, '-');
                } else if (i == 14) {
                    sUsrCNIC.setCharAt(i, sCnic.charAt(i - 2));
                }
            }
            System.out.println(sUsrCNIC);
            ResultSet rs = stmt.executeQuery("select * from VIEWEMPIBST24_1 where (NICNO='" + sUsrCNIC + "' AND EMPLOYEEID='" + sUid + "')");

            //System.out.println("select * from VIEWEMPIBST24 where (NICNO='" + sCnic + "' AND EMPLOYEEID='" + sUid + "')");
            if (rs.next()) {
                EmpName = rs.getString("NAME");
                return EmpName;
                // System.out.println(rs.getString(1) + " = " + rs.getString(2) + " = " + rs.getString(3));
            }

            //step5 close the connection object  
            con.close();

        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 05 3");
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 05 3 Unable to connect to database";
        } catch (SQLException ex) {
            System.out.println(ex);
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 05 4");
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 05 4 Unable to connect to database";
        }
        return null;
    }

    /*06*/ public static String getEmpFingers(String sUid, String sEmpName, String oraConInfo, String oraConName, String oraConPass, String log, File file, ActivityLog activityLog) {
        String fingList = "";
        try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

            //step3 create the statement object  
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT VALUE FROM APPCONFIG WHERE ITEM='FINGERLIST'");
            if (rs.next()) {
                fingList = rs.getString("VALUE");
            }
//            writeLog("TfinToShow="+fingList, bufferedwriter);
//            System.out.print("|TfinToShow="+fingList);
            //step4 execute query  SELECT   BIOSEC.BMDATA.FNO FROM   BIOSEC.BMDATA WHERE  BIOSEC.BMDATA.EMPNO = 12345
            fingList = fingList.replace(" ", "");
            fingList = fingList + "|";
            ResultSet rs1 = stmt.executeQuery("SELECT BMDATA.FNO FROM BMDATA WHERE ( BMDATA.EMPNO = '" + sUid + "') ");

            while (rs1.next()) {
                fingList = fingList + rs1.getString(1);
                // System.out.println(rs.getString(1) + " = " + rs.getString(2) + " = " + rs.getString(3));
            }

            //step5 close the connection object  
            con.close();
            stmt.close();

        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 06 3");
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 06 3 Unable to connect to database";
        } catch (SQLException ex) {
            System.out.println(ex);
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 06 4");
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 06 4 Unable to connect to database";
        }
        return fingList;
    }

    /*07*/ public static String getEmpnoFromUid(String sUid, String oraConInfo, String oraConName, String oraConPass, String log, File file, ActivityLog activityLog) {
        String sEmpno = " ", sEmpName = " ", sEmpID = " ", sEmpnoR = " ";
        //System.out.println("in getEmpnoFromUid");
        try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

            //step3 create the statement object  
            Statement stmt = con.createStatement();
            //System.out.println(sUid);
            ResultSet rs;
            try {
                Integer.parseInt(sUid);
                rs = stmt.executeQuery("SELECT EMPLOYEEID, USERID, NAME,EMPSTATUS FROM VIEWEMPIBST24_1 WHERE ( EMPLOYEEID = '" + sUid + "')");

            } catch (Exception e) {
                rs = stmt.executeQuery("SELECT EMPLOYEEID, USERID, NAME,EMPSTATUS FROM VIEWEMPIBST24_1 WHERE ( USERID = '" + sUid + "')");
            }
            //step4 execute query  
            // ResultSet rs = stmt.executeQuery("SELECT EMPLOYEEID, USERID, NAME,EMPSTATUS FROM VIEWEMPIBST24_1 WHERE ( USERID = '" + sUid + "') OR ( EMPLOYEEID = '" + sUid + "')");

            sEmpno = sUid; // by default, make EmpNo=Uid    

            if (rs.next()) {
                // System.out.println("\n\n"+sUid);
                if (!(rs.getString("EMPSTATUS")).contains("Active")) {
                    activityLog.setRESPONSE_CODE("NOO 08 6");
                    activityLog.setRESPONSE_MESSAGE("Inactive user");
                    activityLog.setSTATUS(activityLog.FAIL);
                    return "NOO 08 6 Inactive user";
                }
                sEmpID = rs.getString(1);
                // System.out.println("EMpID="+sEmpID);
                sEmpno = rs.getString(2);
                //  System.out.println("EMpNO="+sEmpno);
                sEmpName = rs.getString(3);

                //  System.out.println("EMpName="+sEmpName);
                //     System.out.println("\n\n\n\n" + rs.getString(2) + "\n");
                sEmpnoR = sEmpno;
                if (sEmpnoR != null) {
                    if (sEmpnoR.contains(".")) {
                        sEmpnoR = sEmpno.replace(".", "");
                    }
                }
                //   System.out.println("\n\n\n\n" + sEmpnoR + "\n");
            }

            //step5 close the connection object  
            con.close();

        } catch (ClassNotFoundException e) {
            System.err.println(e);
            activityLog.setRESPONSE_CODE("NOO 07 3");
            activityLog.setRESPONSE_MESSAGE("Unable to connect Database");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 07 3 Unable to connect Database";
        } catch (SQLException ex) {
            System.err.println(ex);
            activityLog.setRESPONSE_CODE("NOO 07 4");
            activityLog.setRESPONSE_MESSAGE("Unable to connect Database");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 07 4 Unable to connect Database";

        }
        return sEmpID + "\n" + sEmpnoR + "\n" + sEmpName;
    }

    /*14*/ public static void remFile(String Addr) throws IOException {
        try {
            Process p1;
            p1 = Runtime.getRuntime().exec("rm " + Addr);
            p1.waitFor();
        } catch (InterruptedException ex) {
            System.out.print("|ISO not Removed");
        }
    }

    /*20*/
    public static String getCustomerFingers(String sUid, String oraConInfo, String oraConName, String oraConPass, String log, File file, ActivityLog activityLog) {
        String fingList = "";
        try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

            //step3 create the statement object  
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT VALUE FROM APPCONFIG WHERE ITEM='FINGERLIST'");
            if (rs.next()) {
                fingList = rs.getString("VALUE");
            }
//            writeLog("TfinToShow="+fingList, bufferedwriter);
//            System.out.print("|TfinToShow="+fingList);
            //step4 execute query  SELECT   BIOSEC.BMDATA.FNO FROM   BIOSEC.BMDATA WHERE  BIOSEC.BMDATA.EMPNO = 12345
            fingList = fingList.replace(" ", "");
            fingList = fingList + "|";
            StringBuilder sUsrCNIC;
            if (sUid.length() == 13) {
                sUsrCNIC = new StringBuilder("00000-0000000-9");
                for (int i = 0; i < 15; i++) {
                    if (i < 5) {
                        sUsrCNIC.setCharAt(i, sUid.charAt(i));
                    } else if (i == 5) {
                        sUsrCNIC.setCharAt(i, '-');
                    } else if (i < 13) {
                        sUsrCNIC.setCharAt(i, sUid.charAt(i - 1));
                    } else if (i == 13) {
                        sUsrCNIC.setCharAt(i, '-');
                    } else if (i == 14) {
                        sUsrCNIC.setCharAt(i, sUid.charAt(i - 2));
                    }
                }
            } else {
                activityLog.setRESPONSE_CODE("NOO 20 9");
                activityLog.setRESPONSE_MESSAGE("Invalid Input");
                activityLog.setSTATUS(activityLog.FAIL);
                return "NOO 20 9 Invalid Input";
            }
            ResultSet rs1 = stmt.executeQuery("SELECT CUS_BMDATA.CNIC, CUS_BMDATA.FNO,  CUS_BMDATA.\"TIMESTAMP\" FROM CUS_BMDATA WHERE ( CUS_BMDATA.CNIC = '" + sUsrCNIC + "') ");
            Date date[] = new Date[10];
            String fNos = "";
            int count = 0;
            while (rs1.next()) {
                // time[count]=rs1.getDate("TIMESTAMP");
                try {
                    // SimpleDateFormat dateFormat = new SimpleDateFormat("mm-dd-yyyy hh:mm:ss");
                    date[count] = rs1.getDate("TIMESTAMP"); //dateFormat.parse(rs1.getString("TIMESTAMP"));
                    // if (SideWork.getDiffDays(date[count]) != 0) {
                    fNos = fNos + rs1.getString("FNO");
                    // }
                } catch (Exception e) {
                    System.out.println(e);
                    activityLog.setRESPONSE_CODE("NOO 20 2");
                    activityLog.setRESPONSE_MESSAGE("Format Error");
                    activityLog.setSTATUS(activityLog.FAIL);
                    return "NOO 20 2 Format Error";
                }
                count++;
                System.out.println(date[count - 1] + " and FNo: " + fNos);
            }
            if (count > 1) {
                for (int check = 0; check < count; check++) {
                    if (SideWork.getDiffDays(date[check]) == 0) {//0 mean any of one finger is verified today
                        activityLog.setRESPONSE_CODE("NOO 2016");
                        activityLog.setRESPONSE_MESSAGE("This customer has already been verified today through Biometric. Just validate and commit in T24.");
                        activityLog.setSTATUS(activityLog.FAIL);
                        return "NOO 2016 This customer has already been verified today through Biometric. Just validate and commit in T24.";//fNos = fNos + rs1.getString("FNO");
                    }
                }
                fNos = "";
            }
            fingList = fingList + fNos;
            //step5 close the connection object  
            con.close();
            stmt.close();

        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 20 3");
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 20 3 Unable to connect to database";
        } catch (SQLException ex) {
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 20 4");
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 20 4 Unable to connect to database";
        } catch (Exception ex) {
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 20 5");
            activityLog.setRESPONSE_MESSAGE("Error contact with support");
            activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 20 5 Error contact with support";
        }
        activityLog.setRESPONSE_CODE("YOO O");
        activityLog.setRESPONSE_MESSAGE(fingList + "|");
        activityLog.setSTATUS(activityLog.SUCCESS);
        return fingList + "|";
    }

}
