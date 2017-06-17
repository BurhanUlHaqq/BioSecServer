package com.src.server.actions;

import com.idea.builders.ActivityLog;
import static com.src.server.actions.Audit.addAudit;
import static com.src.server.actions.CommonActions.remFile;
import static com.src.server.actions.LogCreation.writeLog;
import static com.src.server.actions.SideWork.addDot;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class VerificationProcess {
    
    /*11*/ public static String verifyFP(String extractedEIN, String sEmpno, byte[] finISO, String ClientIp, String oraConInfo, String oraConName, String oraConPass, String log, File file,ActivityLog activityLog) throws InterruptedException {
        //System.out.println(extractedEIN+"in verify");
        activityLog.setFLAG(extractedEIN);
        long err;
        byte kbBuffer[] = new byte[100];
        byte kbWhichFinger[] = new byte[100];
        int fingerLength = 0;
        String finger = new String("Finger");
        int a1 = 1;
        int b1 = 1;
        String sResStr = "Mismatch";
        String fpRes = "";
        byte[][] bytewr = new byte[11][800];
        FileWriter fw = null;
        FileOutputStream fos = null;
        Process p1 = null;
        BufferedOutputStream bos = null;
        String FileAddr = "";
        try {
//            String res = ClientIp;
//            // System.out.println(res);
//            String[] sSplit = res.split("/");
//            String clientIp = sSplit[1];
//            String[] sSplit2 = clientIp.split(":");
//            clientIp = sSplit2[0];
            String timeStamp = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
            FileAddr = "/usr/tmp/" + ClientIp + extractedEIN + timeStamp + ".iso";
            //FileAddr = "/usr/tmp/ali.iso";
            //System.out.println(extractedEIN+"after File rite verify");
            fos = new FileOutputStream(FileAddr);
            bos = new BufferedOutputStream(fos);
            //  System.out.println("File addr:" + FileAddr);
            try {
                int i = 1;
                // System.out.println("init oracle\n");
                //bos.write(finISO);
                bytewr[0] = finISO;
                try {
                    //step1 load the driver class
                    Class.forName("oracle.jdbc.driver.OracleDriver");

                    //step2 create  the connection object
                    Connection conn = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

                    //   System.out.println("connected oracle\n");
                    //step3 create the statement object
                    Statement stmt = conn.createStatement();

                    //step4 execute query
//    ResultSet rs=stmt.executeQuery("select EMPLOYEEID,NAME from VIEWEMPIBST24 where (EMPLOYEEID='33333') OR (EMPLOYEEID='44444') ");
                    //   System.out.println("UserID to search BMDATA: " + sUid);
                    ResultSet rs = stmt.executeQuery("select EMPNO,FNO, BDATA from BMDATA where (EMPNO='" +extractedEIN+ "') ");
                    Blob blob;
                   // System.err.println("After Query"+extractedEIN);
                    while (rs.next()) {
                        //(assuming you have a ResultSet named RS)
                        blob = rs.getBlob("BDATA");
                        int blobLength = (int) blob.length();
                        bytewr[i] = blob.getBytes(1, blobLength);
                        i++;
// -------------------------------
                        //blob.setBytes(posInBlobField, blobAsBytes);
                        //release the blob and free up memory. (since JDBC 4.0)
                        //blob.free();
//                        try {
//                            // blob.free();
//                        } catch (java.lang.AbstractMethodError err2) {
//                            // Blob gained a new method in jdbc4 (drivers compiled
//                            // against older jdks don't provide this methid
//                            remFile(FileAddr);
//                            System.err.println("EXCEPTION: " + err2);
//                            return "NOO 06 Reading error!";
//                        }
//                        catch (SQLException ex) {
//                            // DBMS failed to free resource or does not support call
//                            // ignore this, as we can't do more
//                            remFile(FileAddr);
//                            System.err.println("EXCEPTION: " + ex);
//                            return "NOO 05 Unable to connect Database!";
//                        }
                    }
                    rs.close();
                    stmt.close();
                    conn.close();

                } catch (SQLException ex) {
                    remFile(FileAddr);
                    System.err.println(ex);
                    activityLog.setRESPONSE_MESSAGE("Unable to connect database");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 4");
                    return "NOO 11 4 Unable to connect database";
                } catch (ClassNotFoundException ex) {
                    remFile(FileAddr);
                    System.err.println("EXCEPTION: " + ex);
                    activityLog.setRESPONSE_MESSAGE("Unable to connect database");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 3");
                    return "NOO 11 3 Unable to connect database";
                }
                System.out.print("|TFound=" + (i - 1));
                // writeLog("|TFound=" + (i - 1), bufferedwriter);
                log = log + "|TFound=" + (i - 1);
                // File ff = new File("/home/dev/FDx SDK Pro for Linux v3.71b/FDx_SDK_PRO_LINUX_X86_3_7_1_REV570/sgfplibtest/data.dll");
                // fw = new FileWriter(ff, true);
                //BufferedWriter writer give better performance
                // BufferedWriter bw = new BufferedWriter(fw);
                if (i > 1) {
                    for (int j = 0; j < i; j++) {
                        bos.write(bytewr[j]);
                        //bos.flush();
                    }
                    //Closing BufferedWriter Stream
                    //  bw.close();
                    bos.flush();
                    fos.close();
                    bos.close();

                    String consoleStatmnt = "/usr/local/bin/biosecfpm " + FileAddr;

                    try {
                        int tries = 5;
                        String line;
                        do {
                            p1 = Runtime.getRuntime().exec(consoleStatmnt);
                            p1.waitFor();
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(p1.getInputStream())
                            );
                            tries--;
                            line = reader.readLine();
                            if ((tries == 0) && (line == null)) {
                                remFile(FileAddr);
                                System.err.println("Check sported app ");
                    activityLog.setRESPONSE_MESSAGE("Matching error");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 8");
                                System.out.println("Matching error 1");
                                return "NOO 11 8 Matching error";
                            } else if (line == null) {
                                // System.out.println("login try" + tries);

                            }
                        } while ((tries > 0) && (line == null));
                        int ch = Integer.parseInt(line);
                        System.out.print("|DvrFult=" + ch);
                        // writeLog("|DvrFult=" + ch, bufferedwriter);
                        // writeLog("|DvrFult=" + ch, bufferedwriter);
                        log = log + "|DvrFult=" + ch;
                        switch (ch) {
                            case 0: {
                               remFile(FileAddr);
                    activityLog.setRESPONSE_MESSAGE("Matching error");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 2");
                    System.out.println("Matching error 2");
                                return "NOO 11 2 Matching error";
                            }
                            case 1: {
                                remFile(FileAddr);
                                activityLog.setRESPONSE_MESSAGE("Matching error");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 3");
                    System.out.println("Matching error 3");
                                return "NOO 11 3 Matching error";
                            }
                            case 4: {
                                remFile(FileAddr);
                                System.out.print("|Verified");
                                // writeLog("|Verified", bufferedwriter);
                                log = log + "|Verified";
                                log = log + "|Verified";
                                String[] IPs = new String[20];
                                int[] Ports = new int[20];
                                int TotalFound = 0;
                                //geting servers list to write file
                                try {
                                    Class.forName("oracle.jdbc.driver.OracleDriver");
                                    Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

                                    Statement stmt = con.createStatement();
                                    ResultSet rs = stmt.executeQuery("SELECT SNAME, SPORT FROM TSERVERS");
                                    for (int cnt = 0; (rs.next()) && (cnt < 20); cnt++) {
                                        IPs[cnt] = rs.getString("SNAME");
                                        Ports[cnt] = rs.getInt("SPORT");
                                        TotalFound++;
                                    }
                                    con.close();

                                } catch (ClassNotFoundException ex) {
                                    System.out.println(ex);
                     activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 3");
                                    return "NOO 11 3 Unable to connect to database";
                                } catch (SQLException ex) {
                                    System.out.println(ex);
                     activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 4");
                                    return "NOO 11 4 Unable to connect to database";
                                }
                                if (TotalFound == 0) {
                                    System.out.print("|Not loged in|NoServerToWrite");
                                    //writeLog("|Not loged in|NoServerToWrite", bufferedwriter);
                                    log = log + "Not loged in|NoServerToWrite";
                    activityLog.setRESPONSE_MESSAGE("Unable to write");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 1");
                                    return "NOO 11 1 Unable to write";
                                }
                                //done selecting servers
                               // System.out.println("\nTotal Found: " + TotalFound);
                                for (int tm = 0; tm < TotalFound; tm++) {
                                   // System.out.println("Server: " + IPs[tm] + "\tPort: " + Ports[tm] + "|");
                                }
                                Socket s = null;
                                Random rand = new Random();
                                int randomNum = rand.nextInt(((TotalFound - 1) - 0) + 1) + 0;//
                                boolean IsRepeat = false;
                                int Current = randomNum;
                                //System.out.print("\n\nRndom: " + randomNum);
                                //Round Robning for server Connection
                                for (int tryNo = 0; tryNo < TotalFound; tryNo++) {

                                    try {
                                        s = new Socket(IPs[Current], Ports[Current]);
                                       // System.out.println("Connected With Server: " + IPs[Current] + "\tPort: " + Ports[Current]);
                                    } catch (Exception ex) {

                                    }
                                    if (s != null) {
                                        break;
                                    }
                                    if (IsRepeat) {
                                        Current++;
                                        if (Current == randomNum) {
                                            System.out.print("|Not loged in|CanNotConnectin");
                                            //  writeLog("|Not loged in|CanNotConnect", bufferedwriter);
                                            log = log + "|Not loged in|CanNotConnect";
                     activityLog.setRESPONSE_MESSAGE("Unable to write");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 1");
                       return "NOO 11 1 Unable to write";
                                        }
                                    } else {
                                        Current++;
                                        if (Current == TotalFound) {
                                            IsRepeat = true;
                                            Current = 0;
                                        }
                                    }

                                }
                                //end Roun Robning                                
                                if (s == null) {
                                    System.out.print("|Not loged in|CanNotConnect");
                                    // writeLog("|Not loged in|CanNotConnect", bufferedwriter);
                                    log = log + "|Not loged in|CanNotConnect";
                    activityLog.setRESPONSE_MESSAGE("Unable to write");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 1");
                                    return "NOO 11 1 Unable to write";
                                }
                                try {
                                    //s = new Socket(IPs[Current], Integer.parseInt(Ports[Current]));
                                    DataOutputStream ps = new DataOutputStream(s.getOutputStream());
                                    //System.out.println("\nnow empno="+sEmpno);
                                    String now = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(Calendar.getInstance().getTime());
                                    //String RsEmpno = addDot(sEmpno);
                                    //String EMpNoWODot = RsEmpno.replace(".", "");
                                   // String UID=activityLog.getUSER_ID();
                                    //String string = EMpNoWODot + "#" + EMpNoWODot + ".txt:\nFNG:VERIFIED\nTIME:" + now + "\n" + EMpNoWODot + ":" + sUid;
                                    String string = sEmpno + "#" + sEmpno + ".txt:\nFNG:VERIFIED\nTIME:" + now + "\n" + sEmpno + ":" + extractedEIN; 
                                     byte[] b = string.getBytes(Charset.forName("UTF-8"));
                                    ps.write(b);
                                    ps.flush();
                                    DataInputStream dis = new DataInputStream((s.getInputStream()));
                                    String responce = dis.readLine();
                                    s.close();
                                    System.out.print("|loged in");
                                    //writeLog("|loged in", bufferedwriter);
                                    log = log + "|loged in";
                                } catch (IOException e) {
                                    System.out.print("|Not loged in");
                                    //writeLog("|Not loged in", bufferedwriter);
                                    log = log + "|Not loged in";
                    activityLog.setRESPONSE_MESSAGE("Unable to write");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 1");
                                    return "NOO 11 1 Unable to write";
                                }
                     activityLog.setRESPONSE_MESSAGE("Welcome"+extractedEIN);
                    activityLog.setSTATUS(activityLog.SUCCESS);
                    activityLog.setRESPONSE_CODE("YOO 00 0");
                     return "YOO 00 0 Welcome " + extractedEIN;
                            }
                            case 5: {
                                remFile(FileAddr);
                    activityLog.setRESPONSE_MESSAGE("Not Matched");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 6");
                                return "NOO 11 6 Not Matched";
                            }
                        }
                         remFile(FileAddr);

                    } catch (IOException ex) {
                        remFile(FileAddr);
                        System.out.println(ex);
                    activityLog.setRESPONSE_MESSAGE("I/O error");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 2");
                        return "NOO 11 2 I/O error";
                    } finally {
                        remFile(FileAddr);
                    }
                } else {
                    System.out.print("|Mismatch");
                    // writeLog("|Mismatch", bufferedwriter);
                    log = log + "|Mismatch";
                    activityLog.setRESPONSE_MESSAGE("Invalid user/Not registered");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 11 6");
                    return "NOO 11 6 Invalid user/Not registered";
                }
            } catch (FileNotFoundException ex) {
                remFile(FileAddr);
                System.out.println(ex);
                activityLog.setRESPONSE_MESSAGE("Unable to write");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 11 2");
                return "NOO 11 2 Unable to write";
            } catch (IOException ex) {
                System.out.println(ex);
                activityLog.setRESPONSE_MESSAGE("I/O error");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 11 2");
                return "NOO 11 2 I/O error";
            } catch (Exception ex) {
                System.out.println(ex);
                activityLog.setRESPONSE_MESSAGE("Unhandald error");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 11 8");
                return "NOO 11 8 Unhandald error";
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
             activityLog.setRESPONSE_MESSAGE("Unable to write");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 11 2");
            return "NOO 11 2 Unable to write";
        } catch (IOException ex) {
            System.out.println(ex);
            activityLog.setRESPONSE_MESSAGE("I/O error");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 11 2");
            return "NOO 11 2 I/O error";
        } catch (Exception ex) {
            System.out.println(ex);
             activityLog.setRESPONSE_MESSAGE("Unhandald error");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 11 9");
            return "NOO 11 9 Unhandald error";
        }
        System.out.print("|End of function");
//        writeLog("|End of function", bufferedwriter);
        activityLog.setRESPONSE_MESSAGE("Invalid input");
          activityLog.setSTATUS(activityLog.FAIL);
         activityLog.setRESPONSE_CODE("NOO 11 9");
        return "NOO 11 9 Invalid input";
    }

    /*12*/ public static String verifySupervisor(String sUid, byte[] finISO, String sc, String oraConInfo, String oraConName, String oraConPass, String log, File file,ActivityLog activityLog) throws InterruptedException {
        // return "YOO";
          System.out.println("IN verifySupervisor");
        String clientIp = sc;
        //    System.out.println(res);
//        String[] sSplit = res.split("/");
//          System.out.println("CHECK 1");
//        String clientIp = sSplit[1];
//          System.out.println("CHECK 2");
//        String[] sSplit2 = clientIp.split(":");
//          System.out.println("CHECK 3");
//        clientIp = sSplit2[0];
//          System.out.println("CHECK 4");
        // System.out.println("\n\n\ncall \n\n\n");
        try {
           
            //select EMPLOYEEID from BMSSUPERVISORS where (EMPLOYEEID='" + sUid + "') "
            Class.forName("oracle.jdbc.driver.OracleDriver");
            //step2 create  the connection object
            Connection conn = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);
            //  System.out.println("connected oracle\n");
            //step3 create the statement object
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from BMSUPERVISORS where (EMPLOYEEID='" + sUid + "')");
            if (rs.next())//this is real SupperVisor
            {
                //now verify finger data
                long err;
                byte kbBuffer[] = new byte[100];
                byte kbWhichFinger[] = new byte[100];
                int fingerLength = 0;
                String finger = new String("Finger");
                int a1 = 1;
                int b1 = 1;
                String sResStr = "Mismatch";
                String fpRes = "";
                byte[][] bytewr = new byte[11][800];
                FileWriter fw = null;
                FileOutputStream fos = null;
                Process p1 = null;
                BufferedOutputStream bos = null;
                String FileAddr = "";
                int i = 1;

                //logic
                String timeStamp = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
                FileAddr = "/usr/tmp/" + clientIp + sUid + timeStamp + ".iso";
                fos = new FileOutputStream(FileAddr);
                bos = new BufferedOutputStream(fos);
            //    System.out.println("File addr:" + FileAddr);

                //   System.out.println("UserID to search BMDATA: " + sUid);
                rs = stmt.executeQuery("select EMPNO,FNO, BDATA from BMDATA where EMPNO='" + sUid + "'");
                Blob blob;
                bytewr[0] = finISO;
                while (rs.next()) {
                    //(assuming you have a ResultSet named RS)
                    blob = rs.getBlob("BDATA");
                    int blobLength = (int) blob.length();
                    bytewr[i] = blob.getBytes(1, blobLength);
                    i++;
                }
                System.out.print("|TFound=" + (i - 1));
                //   writeLog("|TFound=" + (i - 1), bufferedwriter);
                log = log + "|TFound=" + (i - 1);
                // File ff = new File("/home/dev/FDx SDK Pro for Linux v3.71b/FDx_SDK_PRO_LINUX_X86_3_7_1_REV570/sgfplibtest/data.dll");
                // fw = new FileWriter(ff, true);
                //BufferedWriter writer give better performance
                // BufferedWriter bw = new BufferedWriter(fw);
                if (i > 1) {
                    for (int j = 0; j < i; j++) {
                        bos.write(bytewr[j]);
                        //bos.flush();
                    }
                    //Closing BufferedWriter Stream
                    //  bw.close();
                    bos.flush();
                    fos.close();
                    bos.close();

                    String consoleStatmnt = "/usr/local/bin/biosecfpm " + FileAddr;
                    // System.out.println(consoleStatmnt);
                    int tries = 5;
                    String line;
                    do {
                        p1 = Runtime.getRuntime().exec(consoleStatmnt);
                        p1.waitFor();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(p1.getInputStream())
                        );
                        tries--;
                        line = reader.readLine();
                        if ((tries == 0) && (line == null)) {
                            remFile(FileAddr);
                            System.err.print("|SegmntFault");
                            activityLog.setRESPONSE_CODE("NOO 12 8");
                            activityLog.setRESPONSE_MESSAGE("Matching error");
                            activityLog.setSTATUS(activityLog.FAIL);
                            return "NOO 12 8 Matching error";
                        } else if (line == null) {
                            //   System.out.println("login try" + tries);

                        }
                    } while ((tries > 0) && (line == null));
                    int ch = Integer.parseInt(line);
                    System.out.println("|DrvRes=" + ch);
                    switch (ch) {
                        case 0: {
                            remFile(FileAddr);
                             activityLog.setRESPONSE_CODE("NOO 12 2");
                            activityLog.setRESPONSE_MESSAGE("Matching error");
                            activityLog.setSTATUS(activityLog.FAIL);
                            return "NOO 12 2 Matching error";
                        }
                        case 1: {
                            remFile(FileAddr);
                            activityLog.setRESPONSE_CODE("NOO 12 3");
                            activityLog.setRESPONSE_MESSAGE("Matching error");
                            activityLog.setSTATUS(activityLog.FAIL);
                            return "NOO 12 3 Matching error";
                        }
                        case 4: {
                            remFile(FileAddr);
                            addAudit(sUid, finISO, clientIp, "SUPERVISOR", "Verified", "SP", oraConInfo, oraConName, oraConPass, log, file,activityLog);
                            System.out.print("|Verified");
                            // writeLog("|Verified", bufferedwriter);
                            log = log + "|Verified";
                            activityLog.setRESPONSE_CODE("YOO 00 0");
                            activityLog.setRESPONSE_MESSAGE("Welcome supervisor verified");
                            activityLog.setSTATUS(activityLog.SUCCESS);
                            return "YOO 00 0 Welcome supervisor verified";
                        }
                        case 5: {
                            remFile(FileAddr);
                            System.out.print("|Mismatch");
                            addAudit(sUid, finISO, clientIp, "SUPERVISOR", "Mismatch", "SP", oraConInfo, oraConName, oraConPass, log, file,activityLog);
                            //writeLog("|Mismatch", bufferedwriter);
                            log = log + "|Mismatch";
                             activityLog.setRESPONSE_CODE("NOO 12 6");
                            activityLog.setRESPONSE_MESSAGE("Not Matched");
                            activityLog.setSTATUS(activityLog.FAIL);
                            return "NOO 12 6 Not Matched";
                        }
                    }
                } else // zero recodr in bmdata
                {
                            activityLog.setRESPONSE_CODE("NOO 12 6");
                            activityLog.setRESPONSE_MESSAGE("Record not found");
                            activityLog.setSTATUS(activityLog.FAIL);
                    return "NOO 12 6 Record not found";
                }
            } else //Fack supervisor
            {
                addAudit(sUid, finISO, clientIp, "SUPERVISOR", "Invalid Supervisor", "SP", oraConInfo, oraConName, oraConPass, log, file,activityLog);
                activityLog.setRESPONSE_CODE("NOO 12 6");
               activityLog.setRESPONSE_MESSAGE("Invalid Supervisor");
               activityLog.setSTATUS(activityLog.FAIL);
                return "NOO 12 6 Invalid Supervisor";
            }
        } catch (ClassNotFoundException ex) {
             activityLog.setRESPONSE_CODE("NOO 12 3");
               activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
               activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 12 3 Unable to connect to database";
        } catch (SQLException ex) {
              activityLog.setRESPONSE_CODE("NOO 12 4");
               activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
               activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 12 4 Unable to connect to database";
        } catch (FileNotFoundException ex) {
              activityLog.setRESPONSE_CODE("NOO 12 2");
               activityLog.setRESPONSE_MESSAGE("Unable to write");
               activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 12 2 Unable to write";
        } catch (IOException ex) {
            activityLog.setRESPONSE_CODE("NOO 12 2");
               activityLog.setRESPONSE_MESSAGE("I/O error");
               activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 12 2 I/O error";
        } finally {

        }
        activityLog.setRESPONSE_CODE("NOO 12 9");
         activityLog.setRESPONSE_MESSAGE("Invalid input");
         activityLog.setSTATUS(activityLog.FAIL);
        return "NOO 12 9 Invalid input";
    }

    /*18*/
    public static synchronized String verifyCustomer(String sCnic, byte[] finISO, String oraConInfo, String oraConName, String oraConPass, String log, File file,ActivityLog activityLog) throws IOException, InterruptedException {
        //System.out.println("|" + sCnic);
        try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

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
            ResultSet rs = stmt.executeQuery("select * from CUS_VIEWCUSTOMMER where (CNIC='" + sUsrCNIC + "')");
            if (rs.next()) {
                //now verify finger data
                long err;
                byte kbBuffer[] = new byte[100];
                byte kbWhichFinger[] = new byte[100];
                int fingerLength = 0;
                String finger = new String("Finger");
                int a1 = 1;
                int b1 = 1;
                String sResStr = "Mismatch";
                String fpRes = "";
                byte[][] bytewr = new byte[11][800];
                FileWriter fw = null;
                FileOutputStream fos = null;
                Process p1 = null;
                BufferedOutputStream bos = null;
                String FileAddr = "";
                int i = 1;

                //logic
                String timeStamp = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
                FileAddr = "/usr/tmp/" + sCnic + timeStamp + ".iso";
                fos = new FileOutputStream(FileAddr);
                bos = new BufferedOutputStream(fos);
            //    System.out.println("File addr:" + FileAddr);

                //   System.out.println("UserID to search BMDATA: " + sUid);
                rs = stmt.executeQuery("select BDATA from CUS_BMDATA where CNIC='" + sUsrCNIC + "'");
                Blob blob;
                bytewr[0] = finISO;
                while (rs.next()) {
                    //(assuming you have a ResultSet named RS)
                    blob = rs.getBlob("BDATA");
                    int blobLength = (int) blob.length();
                    bytewr[i] = blob.getBytes(1, blobLength);
                    i++;
                }
                System.out.print("|TFound=" + (i - 1));
                // writeLog("|TFound=" + (i - 1), bufferedwriter);
                log = log + "|TFound=" + (i - 1);
                // File ff = new File("/home/dev/FDx SDK Pro for Linux v3.71b/FDx_SDK_PRO_LINUX_X86_3_7_1_REV570/sgfplibtest/data.dll");
                // fw = new FileWriter(ff, true);
                //BufferedWriter writer give better performance
                // BufferedWriter bw = new BufferedWriter(fw);
                if (i > 1) {
                    for (int j = 0; j < i; j++) {
                        bos.write(bytewr[j]);
                        //bos.flush();
                    }
                    //Closing BufferedWriter Stream
                    //  bw.close();
                    bos.flush();
                    fos.close();
                    bos.close();

                    String consoleStatmnt = "/usr/local/bin/biosecfpm " + FileAddr;
                    // System.out.println(consoleStatmnt);
                    int tries = 5;
                    String line;
                    do {
                        p1 = Runtime.getRuntime().exec(consoleStatmnt);
                        p1.waitFor();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(p1.getInputStream())
                        );
                        tries--;
                        line = reader.readLine();
                        if ((tries == 0) && (line == null)) {
                            remFile(FileAddr);
                            System.err.print("|SegmntFault");
                            activityLog.setRESPONSE_CODE("NOO 18 8");
                            activityLog.setRESPONSE_MESSAGE("Matching error");
                            activityLog.setSTATUS(activityLog.FAIL);
                            return "NOO 18 8 Matching error";
                        } else if (line == null) {
                            //   System.out.println("login try" + tries);

                        }
                    } while ((tries > 0) && (line == null));
                    int ch = Integer.parseInt(line);
                    System.out.println("|DrvRes=" + ch);
                    switch (ch) {
                        case 0: {
                            remFile(FileAddr);
                            activityLog.setRESPONSE_CODE("NOO 18 2");
                            activityLog.setRESPONSE_MESSAGE("Matching error");
                            activityLog.setSTATUS(activityLog.FAIL);
                            return "NOO 18 2 Matching error";
                        }
                        case 1: {
                            remFile(FileAddr);
                            activityLog.setRESPONSE_CODE("NOO 18 3");
                            activityLog.setRESPONSE_MESSAGE("Matching error");
                            activityLog.setSTATUS(activityLog.FAIL);
                            return "NOO 18 3 Matching error";
                        }
                        case 4: {
                            remFile(FileAddr);
                            System.out.print("|Verified");
                            // writeLog("|Verified", bufferedwriter);
                            log = log + "|Verified";
                            activityLog.setRESPONSE_CODE("YOO 00 0");
                            activityLog.setRESPONSE_MESSAGE("Welcome custommer verified");
                            activityLog.setSTATUS(activityLog.SUCCESS);
                            return "YOO 00 0 Welcome custommer verified";
                        }
                        case 5: {
                            remFile(FileAddr);
                            System.out.print("|Mismatch");
                            // writeLog("|Mismatch", bufferedwriter);
                            log = log + "|Mismatch";
                            activityLog.setRESPONSE_CODE("NOO 18 6");
                            activityLog.setRESPONSE_MESSAGE("Not Matched");
                            activityLog.setSTATUS(activityLog.FAIL);
                            return "NOO 18 6 Not Matched";
                        }
                    }
                } else // zero recodr in bmdata
                {
                    activityLog.setRESPONSE_CODE("NOO 18 6");
                    activityLog.setRESPONSE_MESSAGE("Record not found");
                     activityLog.setSTATUS(activityLog.FAIL);
                    return "NOO 18 6 Record not found";
                }

            }

            //step5 close the connection object  
            con.close();
            System.out.print("|Coustommer not found");
            activityLog.setRESPONSE_CODE("NOO 18 6");
            activityLog.setRESPONSE_MESSAGE("Coustommer not found");
              activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 18 6 Coustommer not found";
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 18 3");
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
              activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 18 3 Unable to connect to database";
        } catch (SQLException ex) {
            System.out.println(ex);
            activityLog.setRESPONSE_CODE("NOO 18 4");
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
              activityLog.setSTATUS(activityLog.FAIL);
            return "NOO 18 4 Unable to connect to database";
        }
        // return "NOO 18 9 Invalid input";
    }

}
