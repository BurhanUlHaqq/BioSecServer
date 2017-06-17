/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.src.server.actions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author dev
 */
public class ServerLog {
    public static String addServerLog(String SERVERID,String EMPID,String NICNO,String IPADDR,String COMMAND,String SUPERVISION,String RESPONSE_CODE,String RESPONSE,String REQUEST_STATUS,String FLAG,String oraConInfo,String oraConName,String oraConPass)
    {
        
         try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);
            String sql = "INSERT INTO SERVERLOG(TSTAMP,SERVERID, EMPID, NICNO, IPADDR, COMMAND, SUPERVISION, RESPONSE_CODE, RESPONSE, REQUEST_STATUS, FLAG) "+ "VALUES( CURRENT_TIMESTAMP , '"+SERVERID+"' ,  '" + EMPID + "',  '" + NICNO + "',  '" + IPADDR + "',  '" + COMMAND + "',  '"+SUPERVISION+"', '" + RESPONSE_CODE + "', '" + RESPONSE + "', '" + REQUEST_STATUS + "', '" + FLAG + "')";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.executeUpdate();
            con.commit();
            con.close();

        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
           // return "NOO 13 3 Unable to connect to database";
        } catch (SQLException e) {
            System.out.println(e);
            //return "NOO 13 4 Unable to connect to database";
        }
        return "OK";
        
    }
    
}
