package nadra.verification.verification;

import com.idea.builders.ActivityLog;
import com.idea.builders.Config;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import static nadra.verification.verification.NADRACustomerVerification.deleteFromSessionTempRecord;
import static nadra.verification.verification.NADRACustomerVerification.insertToSessionTempRecord;
import org.datacontract.schemas._2004._07.nadra_biometric.TemplateType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import verification.biometric.nadra.BioVeriSysStandard;
import verification.biometric.nadra.IBioVeriSysStandard;

public class NADRAMobileVerification {
    public static String FingerVerification(String franchizeID, String username, String password, String userID, String userContactNo, String fingerIndex, byte[] template, String sIPAddr, String oraConInfo, String oraConName, String oraConPass, String sessionId, ActivityLog activityLog) {
       try {
            Boolean sessionFound = true;
            String oldTranID="";
            byte[] data = new byte[16384];
            Random ran = new Random();
            long TransactionId = ran.nextLong() / 10000;
            while (true) {
                if (TransactionId < 0) {
                    TransactionId = -(TransactionId);

                }
                String transactionIDstr = TransactionId + "";
                if (transactionIDstr.length() == 15) {
                    break;
                } else {
                    TransactionId = ran.nextLong() / 10000;
                }
            }
            String template1 = new String(DatatypeConverter.printBase64Binary(template));
            String xmlsrc = null ;
             try {
                //cnic = sUsrCNIC.toString();
           
                Class.forName("oracle.jdbc.driver.OracleDriver");
                Connection conCh = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);
                Statement stmtCh = conCh.createStatement();
                String statement = "SELECT * FROM SESSION_TEMP_LOGS WHERE CNIC='" + userID+ "'";
                ResultSet rsCh = stmtCh.executeQuery(statement);
                if (rsCh.next()) {
                    sessionFound = false;
                    Time sessionTime = rsCh.getTime("TSTAMP");
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    String now=sdf.format(cal.getTime());
                    Date   nowTime=sdf.parse(now);
                    long diffMS= nowTime.getTime()-sessionTime.getTime();
                    double diffMinutes = (diffMS /1000.0)/ 60;
                    System.out.println("Time Diiference="+diffMinutes);
                    if (diffMinutes < 10) {
                        sessionId = rsCh.getString("SESS_ID");
                        oldTranID = rsCh.getString("TRANS_ID");
                    } else {
                        sessionFound = true;
                        deleteFromSessionTempRecord(userID);
                        sessionId = "";
                
                    }
                } else {
                    sessionId = "";
                }
            } catch (ClassNotFoundException ex) {
                activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("N");
                 return "N " + "Unable to connect to database";
            } catch (SQLException ex) {
                System.err.println(ex);
                activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("N");
                return "N " + "Unable to connect to database";
            }
             if (sessionId.equalsIgnoreCase("")) { 
            oldTranID=Config.getNADRA_Staff_FranchiseID()  +""+ Long.toString(TransactionId);
             xmlsrc = "<BIOMETRIC_VERIFICATION>"
                    + "<USER_VERIFICATION>"
                    + "   <USERNAME>" + Config.getNADRA_Staff_UserName() + "</USERNAME>"
                    + "   <PASSWORD>" + Config.getNADRA_Staff_Password() + "</PASSWORD>"
                    + " </USER_VERIFICATION>"
                    + " <REQUEST_DATA>"
                    + "   <SESSION_ID></SESSION_ID>"
                    + "   <TRANSACTION_ID>" + Config.getNADRA_Staff_FranchiseID() + TransactionId + "</TRANSACTION_ID>"
                    + "   <CITIZEN_NUMBER>" + userID + "</CITIZEN_NUMBER>"
                    + "   <AREA_NAME>punjab</AREA_NAME>"
                    + "   <FINGER_INDEX>" + fingerIndex + "</FINGER_INDEX>"
                    + "   <FINGER_TEMPLATE>" + template1 + "</FINGER_TEMPLATE>"
                    + "   <TEMPLATE_TYPE>" + TemplateType.ISO_19794_2 + "</TEMPLATE_TYPE>"
                    + " </REQUEST_DATA>"
                    + "</BIOMETRIC_VERIFICATION>";
             }else{
                 //  String str[] = sessionId.split("/");
               // oldTranID=str[1];
                  xmlsrc = "<BIOMETRIC_VERIFICATION>"
                    + "<USER_VERIFICATION>"
                    + "   <USERNAME>" + Config.getNADRA_Staff_UserName() + "</USERNAME>"
                    + "   <PASSWORD>" + Config.getNADRA_Staff_Password() + "</PASSWORD>"
                    + " </USER_VERIFICATION>"
                    + " <REQUEST_DATA>"
                    + "   <SESSION_ID>"+sessionId+"</SESSION_ID>"
                    + "   <TRANSACTION_ID>" +oldTranID+ "</TRANSACTION_ID>"
                    + "   <CITIZEN_NUMBER>" + userID + "</CITIZEN_NUMBER>"
                    + "   <AREA_NAME>punjab</AREA_NAME>"
                    + "   <FINGER_INDEX>" + fingerIndex + "</FINGER_INDEX>"
                    + "   <FINGER_TEMPLATE>" + template1 + "</FINGER_TEMPLATE>"
                    + "   <TEMPLATE_TYPE>" + TemplateType.ISO_19794_2 + "</TEMPLATE_TYPE>"
                    + " </REQUEST_DATA>"
                    + "</BIOMETRIC_VERIFICATION>";
             }
            BioVeriSysStandard bioverisys = new BioVeriSysStandard();
            IBioVeriSysStandard ibvs = bioverisys.getBasicHttpBindingIBioVeriSysStandard();
            System.out.println("Requst Data\n\n" + xmlsrc);
            String timeStamp = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
            PrintWriter outRequstWriter = new PrintWriter("/tmp/Request" + timeStamp + ".txt");

            outRequstWriter.write(xmlsrc);
            outRequstWriter.flush();
            outRequstWriter.close();
            String response = ibvs.verifyFingerPrints(Config.getNADRA_Staff_FranchiseID(), xmlsrc);
            System.out.println("Response Data\n\n" + response);
            PrintWriter outResponseWriter = new PrintWriter("/tmp/Response" + timeStamp + ".txt");
            outResponseWriter.write(response);
            outResponseWriter.flush();
            outResponseWriter.close();
            DocumentBuilder db = null;
            String sMessage=null;
            String sSession=null;
            try {
                db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                InputSource isa = new InputSource();
                isa.setCharacterStream(new StringReader(response));
                org.w3c.dom.Document doc = null;

                doc = db.parse(isa);
                doc.getDocumentElement().normalize();

                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

                NodeList CODE = doc.getElementsByTagName("CODE");
                Node CODEn = CODE.item(0);
                String sCode=CODEn.getTextContent();
                
                NodeList MESSAGE = doc.getElementsByTagName("MESSAGE");
                Node MESSAGEn = MESSAGE.item(0);
                sMessage=MESSAGEn.getTextContent();
                
                NodeList SESSION_ID = doc.getElementsByTagName("SESSION_ID");
                Node SESSION_IDn = SESSION_ID.item(0);
                sSession=SESSION_IDn.getTextContent();
                
                NodeList CITIZEN_NUMBER = doc.getElementsByTagName("CITIZEN_NUMBER");
                Node CITIZEN_NUMBERn = CITIZEN_NUMBER.item(0);
                String sCitiznNum=CITIZEN_NUMBERn.getTextContent();
                System.out.println(sCode+sMessage+sSession+sCitiznNum);
                addNADRAAudit(sCode,sMessage,sSession,sCitiznNum,template,fingerIndex,sIPAddr,oraConInfo,oraConName, oraConPass,activityLog);
                if(sCode.contains("100"))
                {
                    activityLog.setRESPONSE_MESSAGE(sMessage);
                    activityLog.setSTATUS(activityLog.SUCCESS);
                    activityLog.setRESPONSE_CODE("Y");
                    deleteFromSessionTempRecord(userID);
                    return "Y "+sMessage;
                }
                else
                {
                    
                      if (sCode.equals("118")) {
                         activityLog.setRESPONSE_MESSAGE("Select an other finger to verify");
                         activityLog.setSTATUS(activityLog.FAIL);
                         activityLog.setRESPONSE_CODE("N");
                         if (sessionFound) {
                            insertToSessionTempRecord(userID, sessionId, oldTranID, "");
                        }
                        return "N Select an other finger to verify|" + sSession + "/" + oldTranID;
                        // return "N Select an other finger to verify"+ "|" + sSession+"/"+franchizeID + TransactionId;
                    }
                    else if(sCode.equals("110") ||sCode.equals("111")||sCode.equals("112")||sCode.equals("114")||sCode.equals("115")||sCode.equals("119")||sCode.equals("120")||sCode.equals("121")||sCode.equals("122")||sCode.equals("123")||sCode.equals("124")||sCode.equals("125")||sCode.equals("175")||sCode.equals("185")||sCode.equals("186")){
                    activityLog.setRESPONSE_MESSAGE(sMessage);
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("N");
                    if (sessionFound) {
                            insertToSessionTempRecord(userID, sessionId, oldTranID, "");
                        }
                    return "N " + sMessage + "|" + sSession + "/" + oldTranID;
                    }else{
                         activityLog.setRESPONSE_MESSAGE("Try Again Verification Failed");
                         activityLog.setSTATUS(activityLog.FAIL);
                         activityLog.setRESPONSE_CODE("N");
                         if (sessionFound) {
                            insertToSessionTempRecord(userID, sessionId, oldTranID, "");
                        }
                        return "N Try Again Verification Failed|";
                    }
                }
                
            } catch (Exception ex) {
                activityLog.setRESPONSE_MESSAGE(sMessage);
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("N");
                    if (sessionFound) {
                            insertToSessionTempRecord(userID, sessionId, oldTranID, "");
                        }
                return "N "+sMessage+"|"+sSession;
            }
        } catch (Exception e) {
                    activityLog.setRESPONSE_MESSAGE("Unable to connect with external biometric server");
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("N");
            return "N|Unable to connect with external biometric server";
        }
       // return "N|NOO 08 9 Invalied Input for NADRA request";
    }

    public static void addNADRAAudit(String code, String Msg,String SessionID, String CNIC,byte[] finISO,String fingerIndex, String sIPAddr, String oraConInfo, String oraConName, String oraConPass,ActivityLog activityLog) {
        try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);
            String sql = "INSERT INTO NADRALOG(RESPONSECODE, RESPONSEMSG, SESSIONID, CNIC, FNO, TIME, BDATA,CLIENTIP) VALUES( '"+code+"','"+Msg+"','"+SessionID+"','"+CNIC+"',"+fingerIndex+",CURRENT_TIMESTAMP"+", ?,'"+sIPAddr+"')";
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
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

}
