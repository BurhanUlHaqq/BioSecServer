package com.src.server.actions;

import com.idea.builders.ActivityLog;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Audit {
    /*13*/ public static String addAudit(String sfEmpID, byte[] finISO, String sIPAddr, String sUAction, String sUActDetail, String sFlag, String oraConInfo, String oraConName, String oraConPass, String log, File file,ActivityLog activityLog) {
        try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

            String sql = "INSERT INTO AUDITLOG(TSTAMP, EMPID, IPADDR, UACTION, UACTDETAILS, FINDATA, SFLAG) VALUES( CURRENT_TIMESTAMP ,  '" + sfEmpID + "',  '" + sIPAddr + "',  '" + sUAction + "',  '" + sUActDetail + "',  ?, '" + sFlag + "')";
            PreparedStatement pstmt = con.prepareStatement(sql);

            //Blob blob = con.createBlob();
            //blob.setBytes(1, finISO);    
//            pstmt.setString(1, sEmpno);
//            pstmt.setString(2, sFNo);
            pstmt.setBytes(1, finISO);

            //pstmt.setBlob(3, blob);
            pstmt.executeUpdate();
            con.commit();
            con.close();

        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
        activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
        activityLog.setSTATUS(activityLog.FAIL);
        activityLog.setRESPONSE_CODE("NOO 13 3");
            return "NOO 13 3 Unable to connect to database";
        } catch (SQLException e) {
            System.out.println(e);
        activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
        activityLog.setSTATUS(activityLog.FAIL);
        activityLog.setRESPONSE_CODE("NOO 13 4");
            return "NOO 13 4 Unable to connect to database";
        }
        return "OK";
    }

}
