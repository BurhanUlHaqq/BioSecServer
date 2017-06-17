package com.src.server.actions;

import com.idea.builders.ActivityLog;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import nadra.verification.verification.NADRACustomerVerification;
import nadra.verification.verification.NADRAMobileVerification;

public class RegistrationProcess {
    
    /*08*/ public static String registerFP(String sUid, String sEmpName, String sCnic, String sFNo, byte[] finISO, String cmd, String superVisor, String clientIp, String oraConInfo, String oraConName, String oraConPass, String log, File file, ActivityLog activityLog) throws ClassNotFoundException {
         String returnedSessionId="";
        try {
            //step1 load the driver class
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

            //  System.out.println("connected oracle\n");
            //step3 create the statement object
            Statement stmt = con.createStatement();
            //step4 execute query
//    ResultSet rs=stmt.executeQuery("select EMPLOYEEID,NAME from VIEWEMPIBST24 where (EMPLOYEEID='33333') OR (EMPLOYEEID='44444') ");
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
            System.out.println(sUid);
            ResultSet rs = stmt.executeQuery("select * from VIEWEMPIBST24_1 where (NICNO='" + sUsrCNIC + "' AND EMPLOYEEID='" + sUid + "') ");
            if (rs.next()) {
                if (!(rs.getString("EMPSTATUS")).contains("Active")) {
                    //LogCreation.writeLog("Inactive user", bufferedwriter);
                    log = log + "Inactive user";
                    Audit.addAudit(sUid, finISO, clientIp, "REGISTER", sFNo + "/" + sCnic + "/" + "Inactive user", "RG", oraConInfo, oraConName, oraConPass, log, file, activityLog);
                    activityLog.setRESPONSE_MESSAGE("Inactive user");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 08 6");
                    return "NOO 08 6 Inactive user";
                }
                if (!cmd.contains("OK")) {
                    String sessionID=superVisor;
                    String NADRAresponce = NADRAMobileVerification.FingerVerification("1024", "alliedbank", "Pak@1947.All13d", sCnic, "03004070110", Integer.parseInt(sFNo) + 1 + "", finISO, clientIp, oraConInfo, oraConName, oraConPass, sessionID, activityLog);
                    System.out.println(NADRAresponce);
                    //   System.out.println(NADRAresponce.charAt(0));
                    String rtnMsg = NADRAresponce.substring(2, NADRAresponce.length());
                    if (NADRAresponce.charAt(0) == 'N') {
                        System.out.print("|NOO 08 7 " + rtnMsg);
                        //LogCreation.writeLog(rtnMsg, file);
                        Audit.addAudit(sUid, finISO, clientIp, "REGISTER", sFNo + "/" + sCnic + "/" + rtnMsg, "RG", oraConInfo, oraConName, oraConPass, log, file, activityLog);
                        //writeLog("|NOO 08 7 " + rtnMsg, bufferedwriter);
                        //return "NOO 08 7 " + "Functionaly is not available";
                        activityLog.setRESPONSE_MESSAGE(rtnMsg);
                        activityLog.setSTATUS(activityLog.FAIL);
                        activityLog.setRESPONSE_CODE("NOO 08 7");
                        String msg[]=rtnMsg.split("|");
                        returnedSessionId=msg[1];
                        return "NOO 08 7 " + rtnMsg;
                    }
                } else {
                    try {
                        //select EMPLOYEEID from BMSSUPERVISORS where (EMPLOYEEID='" + sUid + "') "
                        Class.forName("oracle.jdbc.driver.OracleDriver");
                        //step2 create  the connection object
                        Connection conn = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

                        //   System.out.println("connected oracle\n");
                        //step3 create the statement object
                        stmt = conn.createStatement();
                        rs = stmt.executeQuery("select * from SUPVERIUSERS where (EMPID='" + sUid + "')");
                        if (rs.next())//this is real SupperVisor
                        {

                        } else {
                            Audit.addAudit(sUid, finISO, clientIp, "REGISTER", sFNo + "/" + sCnic + "/" + "NADRA verification required/" + superVisor, "RG", oraConInfo, oraConName, oraConPass, log, file, activityLog);
                            activityLog.setRESPONSE_MESSAGE("NADRA verification required");
                            activityLog.setSTATUS(activityLog.FAIL);
                            activityLog.setRESPONSE_CODE("NOO 08 6");
                            return "NOO 08 6 NADRA verification required";
                        }
                    } catch (SQLException ex) {
                        activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
                        activityLog.setSTATUS(activityLog.FAIL);
                        activityLog.setRESPONSE_CODE("NOO 08 4");
                        return "NOO 08 4 Unable to connect to database";
                    }
                }
                //System.out.println("NADRA Less....");
            } else {
                activityLog.setRESPONSE_MESSAGE("Invalid EIN / CNIC");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 08 6");
                return "NOO 08 6 Invalid EIN / CNIC";
            }
        } catch (ClassNotFoundException e) {
            System.err.println(e);
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            activityLog.setRESPONSE_CODE("NOO 08 3");
            return "NOO 08 3 Unable to connect to database";
        } catch (SQLException ex) {
            System.err.println("EXCEPTION: " + ex);
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            activityLog.setRESPONSE_CODE("NOO 08 4");
            return "NOO 08 4 Unable to connect to database";
        }
        try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            try ( //step2 create  the connection object

                    Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass)) {
                String nadraYN = "Y";
                if (cmd.contains("OK")) {
                    nadraYN = "N";
                }
           
                String sql = "INSERT INTO BMDATA(EMPNO,  FNO,  NADRAVERIFIED,  NADRAREF,  BMTYPE,  ISACTIVE, TSTAMP, BDATA) VALUES(  ?,  ?,  '" + nadraYN + "',  'REF',  'F',  'Y', CURRENT_TIMESTAMP, ?)";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setString(1, sUid);
                pstmt.setString(2, sFNo);
                pstmt.setBytes(3, finISO);
                pstmt.executeUpdate();
                con.commit();
            } catch (SQLException ex) {
                System.err.println(ex);
                activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 08 4");
                return "NOO 08 4 Unable to connect to database";
            }
        } catch (ClassNotFoundException e) {
            System.err.println(e);
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            activityLog.setRESPONSE_CODE("NOO 08 3");
            return "NOO 08 3 Unable to connect to database";
        }
        String result;
        if (cmd.contains("OK")) {
            result = Audit.addAudit(sUid, finISO, clientIp, "REGISTER", sFNo + "/" + sCnic + "/" + superVisor, "RG", oraConInfo, oraConName, oraConPass, log, file, activityLog);
        } else {
            result = Audit.addAudit(sUid, finISO, clientIp, "REGISTER", sFNo + "/" + sCnic + "/NADRA", "RG", oraConInfo, oraConName, oraConPass, log, file, activityLog);
        }

        if (result.contains("OK")) {
            
            
            
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
            try {
                //select EMPLOYEEID from BMSSUPERVISORS where (EMPLOYEEID='" + sUid + "') "
          
                Class.forName("oracle.jdbc.driver.OracleDriver");
                //step2 create  the connection object
                Connection conn = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select * from LOCALEMPRCD where (NICNO='" + sUsrCNIC + "' AND EMPLOYEEID='" + sUid + "') ");
                if ((rs.next()))//THIS RECORD IS ALREADY EXIST IN BACKUP_OF_VIEWEMPIBST24_1
                {
                    System.out.println("BM Record already exists \n");
                } else {
                    
                    String viewBackup = "INSERT INTO LOCALEMPRCD(EMPLOYEEID,NAME,EMPSTATUS,NICNO,USERID,BRANCH) SELECT EMPLOYEEID,NAME,EMPSTATUS,NICNO,USERID,BRANCH FROM VIEWEMPIBST24_1 WHERE NICNO='" + sUsrCNIC + "' AND EMPLOYEEID=" + sUid;
                    PreparedStatement pstmt = conn.prepareStatement(viewBackup);
                    pstmt.executeUpdate();
                    conn.commit();
                    conn.close();
                }
            } catch (SQLException ex) {
                return "NOO 08 4 Unable to connect to database";
            }
            activityLog.setRESPONSE_MESSAGE("Registered " + sUid + " " + sEmpName+"|"+returnedSessionId);
            activityLog.setSTATUS(activityLog.SUCCESS);
            activityLog.setRESPONSE_CODE("YOO 00 0");
            return "YOO 00 0 Registered " + sUid + " " + sEmpName+"|"+returnedSessionId;
        } else {
            return result;
        }

    }

    /*19*/
    public static String registerCustomer(String sCnic, String sessionId, byte[] finISO, byte[] finISOLocal, String sFNo, String clientIp, String oraConInfo, String oraConName, String oraConPass, String log, File file, ActivityLog activityLog,String ContactNumber) {
        try {

            //System.out.println("\n\n\n\n\n"+sessionId);
            //step1 load the driver class
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

            //  System.out.println("connected oracle\n");
            //step3 create the statement object
            Statement stmt = con.createStatement();
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

            String NADRAresponce = NADRACustomerVerification.VerifyCustomerFinger("1466", "abl_branched", "aLL!Ed@Br4#D", sCnic, sessionId, ContactNumber, Integer.parseInt(sFNo) + 1 + "", finISO, clientIp, oraConInfo, oraConName, oraConPass, log, file, activityLog);
            System.out.println(NADRAresponce);
            //   System.out.println(NADRAresponce.charAt(0));
            String rtnSessionId=activityLog.getCustomerSessionId();
            String rtnMsg = NADRAresponce.substring(2, NADRAresponce.length());
            boolean isShowInfoAtClient = true;
            if (NADRAresponce.charAt(0) == 'N') {
                System.out.print("|NOO 08 7 " + rtnMsg);
                //                   LogCreation.writeLog(rtnMsg, bufferedwriter);
                //                   Audit.addAudit("Customer", finISO, clientIp, "REGISTER", sFNo + "/" + sCnic + "/" + rtnMsg, "RG", oraConInfo, oraConName, oraConPass, LogMode, bufferedwriter);
                //writeLog("|NOO 08 7 " + rtnMsg, bufferedwriter);
                activityLog.setRESPONSE_MESSAGE(rtnMsg);
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 08 7");
                return "NOO 08 7 " + rtnMsg;
            }
            if (NADRAresponce.charAt(1) == '1') {
                isShowInfoAtClient = false;
            }
            //step3 create the statement object  
            Statement stmtSel = con.createStatement();

            ResultSet rs = stmtSel.executeQuery("SELECT CNIC FROM CUS_BMDATA WHERE CNIC='" + sUsrCNIC + "' AND FNO=" + sFNo);
            if (rs.next()) {
                try {

                    Class.forName("oracle.jdbc.driver.OracleDriver");

                    //step2 create  the connection object
                    Connection conbk = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

                    String sqlBackup = "INSERT INTO BACKUP_CUS_BMDATA(CNIC,FNO,BDATA,\"TIMESTAMP\") SELECT CNIC,FNO,BDATA,\"TIMESTAMP\" FROM CUS_BMDATA WHERE CNIC='" + sUsrCNIC + "' AND FNO=" + sFNo;
                    PreparedStatement pstmtbk = con.prepareStatement(sqlBackup);
                    pstmtbk.executeUpdate();
                    conbk.commit();
                    conbk.close();
                } catch (SQLException ex) {
                    System.err.println(ex);
                    activityLog.setRESPONSE_MESSAGE("Unable to connect to database for backup");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 19 4");
                    return "NOO 19 4 Unable to connect to database for backup"+"|"+rtnSessionId;
                } catch (ClassNotFoundException ex) {
                    activityLog.setRESPONSE_MESSAGE("Unable to connect to database for backup");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 19 3");
                    return "NOO 19 3 Unable to connect to database for backup"+"|"+rtnSessionId;
                }
                String sqlUpdate = "UPDATE  CUS_BMDATA SET  CNIC = '" + sUsrCNIC + "',  FNO = " + sFNo + ",  BDATA = ?,  \"TIMESTAMP\" = CURRENT_TIMESTAMP WHERE CNIC='" + sUsrCNIC + "' AND FNO=" + sFNo;
                System.out.println(sqlUpdate);
                PreparedStatement pstmt = con.prepareStatement(sqlUpdate);

                pstmt.setBytes(1, finISOLocal);

                pstmt.executeUpdate();
                con.commit();
                con.close();
                if (isShowInfoAtClient) {
                    activityLog.setRESPONSE_MESSAGE("Finger Verified Successfully");
                    activityLog.setSTATUS(activityLog.SUCCESS);
                    activityLog.setRESPONSE_CODE("YOO 00 0");
                    return "YOO 00 0 Finger Verified Successfully|" + rtnMsg;
                } else {
                    activityLog.setRESPONSE_MESSAGE("Finger Verified Successfully");
                    activityLog.setSTATUS(activityLog.SUCCESS);
                    activityLog.setRESPONSE_CODE("YOO 0010");
                    return "YOO 0010 Finger Verified Successfully"+"|"+rtnSessionId;//0010 mean don't show info for customer
                }
            } else {

                String sql = "INSERT INTO CUS_BMDATA(CNIC,  FNO, BDATA, TIMESTAMP) VALUES(  '" + sUsrCNIC + "',  ?,  ?, CURRENT_TIMESTAMP)";
                // System.out.println("\n"+sql+"\n");
                PreparedStatement pstmt = con.prepareStatement(sql);

                pstmt.setInt(1, Integer.parseInt(sFNo));
                pstmt.setBytes(2, finISOLocal);

                pstmt.executeUpdate();
                con.commit();
                con.close();
                if (isShowInfoAtClient) {
                    activityLog.setRESPONSE_MESSAGE("Finger Verified Successfully");
                    activityLog.setSTATUS(activityLog.SUCCESS);
                    activityLog.setRESPONSE_CODE("YOO 00 0");
                    return "YOO 00 0 Finger Verified Successfully|" + rtnMsg;
                } else {
                    activityLog.setRESPONSE_MESSAGE("Finger Verified Successfully");
                    activityLog.setSTATUS(activityLog.SUCCESS);
                    activityLog.setRESPONSE_CODE("YOO 0010");
                    return "YOO 0010 Finger Verified Successfully"+"|"+rtnSessionId;//0010 mean don't show info for customer//0010 mean don't show info for customer
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex);
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            activityLog.setRESPONSE_CODE("NOO 19 4");
            return "NOO 19 4 Unable to connect to database";
        } catch (ClassNotFoundException ex) {
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            activityLog.setRESPONSE_CODE("NOO 19 3");
            return "NOO 19 3 Unable to connect to database";
        }

    }

}
