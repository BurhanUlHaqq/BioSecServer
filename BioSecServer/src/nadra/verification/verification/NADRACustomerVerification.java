package nadra.verification.verification;

import com.idea.builders.ActivityLog;
import com.idea.builders.Config;
import com.src.server.actions.SideWork;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.datacontract.schemas._2004._07.nadra_biometric.TemplateType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import verification.biometric.nadra.BioVeriSysBranchBankAccount;
import verification.biometric.nadra.IBioVeriSysBranchBankAccount;

public class NADRACustomerVerification {

    public static String VerifyCustomerFinger(String franchizeID, String username, String password, String userID, String sessionId, String userContactNo, String fingerIndex, byte[] template, String sIPAddr, String oraConInfo, String oraConName, String oraConPass, String log, File file,ActivityLog activityLog) {
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
            StringBuilder sUsrCNIC = new StringBuilder("00000-0000000-9");
                        for (int i = 0; i < 15; i++) {
                            if (i < 5) {
                                sUsrCNIC.setCharAt(i, userID.charAt(i));
                            } else if (i == 5) {
                                sUsrCNIC.setCharAt(i, '-');
                            } else if (i < 13) {
                                sUsrCNIC.setCharAt(i, userID.charAt(i - 1));
                            } else if (i == 13) {
                                sUsrCNIC.setCharAt(i, '-');
                            } else if (i == 14) {
                                sUsrCNIC.setCharAt(i, userID.charAt(i - 2));
                            }
                        }
            String xmlSource;
             try {
                //cnic = sUsrCNIC.toString();
           
                Class.forName("oracle.jdbc.driver.OracleDriver");
                Connection conCh = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);
                Statement stmtCh = conCh.createStatement();
                String statement = "SELECT * FROM SESSION_TEMP_LOGS WHERE CNIC='" + sUsrCNIC + "'";
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
                    if (diffMinutes <= 9) {
                        sessionId = rsCh.getString("SESS_ID");
                        oldTranID = rsCh.getString("TRANS_ID");
                    } else {
                        sessionFound = true;
                        deleteFromSessionTempRecord(sUsrCNIC.toString());
                        sessionId = "";
                
                    }
                } else {
                    sessionId = "";
                }
            } catch (ClassNotFoundException ex) {
                activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 19 20");
                 return "N " + "NOO 19 20 Unable to connect to database";
            } catch (SQLException ex) {
                System.err.println(ex);
                activityLog.setRESPONSE_MESSAGE("Sql Error Occur");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 19 21");
                return "N " + "NOO 19 21 Unable to connect to database";
            }
            if (sessionId.equalsIgnoreCase("")) {
               oldTranID=Config.getNADRA_Customer_FranchiseID() +""+ Long.toString(TransactionId);
                xmlSource = "<BIOMETRIC_VERIFICATION>"
                        + "<USER_VERIFICATION>"
                        + "<USERNAME>" + Config.getNADRA_Customer_UserName() + "</USERNAME>"
                        + "<PASSWORD>" + Config.getNADRA_Customer_Password() + "</PASSWORD>"
                        + "</USER_VERIFICATION> "
                        + " <REQUEST_DATA> "
                        + " <TRANSACTION_ID>" + Config.getNADRA_Customer_FranchiseID() + TransactionId + "</TRANSACTION_ID> "
                        + " <SESSION_ID>" + "" + "</SESSION_ID> "
                        + " <CITIZEN_NUMBER>" + userID + "</CITIZEN_NUMBER> "
                        + " <CONTACT_NUMBER>0300-4070110</CONTACT_NUMBER> "
                        + " <FINGER_INDEX>" + fingerIndex + "</FINGER_INDEX> "
                        + " <FINGER_TEMPLATE>" + template1 + "</FINGER_TEMPLATE> "
                        + " <TEMPLATE_TYPE>" + TemplateType.ISO_19794_2 + "</TEMPLATE_TYPE> "
                        + " <AREA_NAME>punjab</AREA_NAME> "
                        + " <ACCOUNT_TYPE>current</ACCOUNT_TYPE> "
                        + " </REQUEST_DATA> "
                        + " </BIOMETRIC_VERIFICATION>";
            } else {
               
                xmlSource = "<BIOMETRIC_VERIFICATION>"
                        + "<USER_VERIFICATION>"
                        + "<USERNAME>" + Config.getNADRA_Customer_UserName() + "</USERNAME>"
                        + "<PASSWORD>" + Config.getNADRA_Customer_Password() + "</PASSWORD>"
                        + "</USER_VERIFICATION> "
                        + " <REQUEST_DATA> "
                         + " <TRANSACTION_ID>"+oldTranID+ "</TRANSACTION_ID> "
                        + " <SESSION_ID>"+sessionId+"</SESSION_ID> "
                        + " <CITIZEN_NUMBER>" + userID + "</CITIZEN_NUMBER> "
                        + " <CONTACT_NUMBER>0300-4070110</CONTACT_NUMBER> "
                        + " <FINGER_INDEX>" + fingerIndex + "</FINGER_INDEX> "
                        + " <FINGER_TEMPLATE>" + template1 + "</FINGER_TEMPLATE> "
                        + " <TEMPLATE_TYPE>" + TemplateType.ISO_19794_2 + "</TEMPLATE_TYPE> "
                        + " <AREA_NAME>punjab</AREA_NAME> "
                        + " <ACCOUNT_TYPE>current</ACCOUNT_TYPE> "
                        + " </REQUEST_DATA> "
                        + " </BIOMETRIC_VERIFICATION>";
            }
            System.out.println("Request Data\n\n" + xmlSource);
            String timeStamp = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
            PrintWriter outRequstWriter = new PrintWriter("/tmp/Request" + timeStamp + ".txt");
            outRequstWriter.write(xmlSource);
            outRequstWriter.flush();
            outRequstWriter.close();
            BioVeriSysBranchBankAccount BacnkAccount = new BioVeriSysBranchBankAccount();
            IBioVeriSysBranchBankAccount IBankAccout = BacnkAccount.getBasicHttpBindingIBioVeriSysBranchBankAccount();
            String response = IBankAccout.verifyFingerPrints(Config.getNADRA_Customer_FranchiseID(), xmlSource);
            PrintWriter outResponseWriter = new PrintWriter("/tmp/Response" + timeStamp + ".txt");
            //   outResponseWriter.write(response);
            outResponseWriter.flush();
            outResponseWriter.close();
            System.out.println("Response Data\n\n" + response);
            DocumentBuilder db = null;
            String sCode = null;
            String sMessage = "N Contact with supptor team";
            String sSession = null;
            String sCitiznNum = null;
            String sName = null;
            String sFater_Husband_Name = null;
            String sPresentAddress = null;
            String sPermanantAddress = null;
            String sDateOfBirth = null;
            String sBirthPlace = null;
            String sPhoto = null;
            String sExpiryDate = null;
            String sCardType = null;
            try {
                // System.out.println("Here 6");
                db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                InputSource isa = new InputSource();
                isa.setCharacterStream(new StringReader(response));
                org.w3c.dom.Document doc = null;

                doc = db.parse(isa);
                doc.getDocumentElement().normalize();

                // System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                NodeList CODE = doc.getElementsByTagName("CODE");
                Node CODEn = CODE.item(0);
                sCode = CODEn.getTextContent();
                System.out.println(sCode);
//success case
                if (sCode.equalsIgnoreCase("100")) {
                    NodeList MESSAGE = doc.getElementsByTagName("MESSAGE");
                    Node MESSAGEn = MESSAGE.item(0);
                    sMessage = MESSAGEn.getTextContent();
//System.out.println(sMessage );
                    NodeList SESSION_ID = doc.getElementsByTagName("SESSION_ID");
                    Node SESSION_IDn = SESSION_ID.item(0);
                    sSession = SESSION_IDn.getTextContent();
//System.out.println(sSession);
                    NodeList CITIZEN_NUMBER = doc.getElementsByTagName("CITIZEN_NUMBER");
                    Node CITIZEN_NUMBERn = CITIZEN_NUMBER.item(0);
                    sCitiznNum = CITIZEN_NUMBERn.getTextContent();
//System.out.println(sSession);
                    NodeList NAME = doc.getElementsByTagName("NAME");
                    Node NAMEn = NAME.item(0);
                    sName = NAMEn.getTextContent();
//System.out.println(sSession);
                    NodeList FATHER_HUSBAND_NAME = doc.getElementsByTagName("FATHER_HUSBAND_NAME");
                    Node FATHER_HUSBAND_NAMEn = FATHER_HUSBAND_NAME.item(0);
                    sFater_Husband_Name = FATHER_HUSBAND_NAMEn.getTextContent();
//System.out.println(sSession);
                    NodeList PRESENT_ADDRESS = doc.getElementsByTagName("PRESENT_ADDRESS");
                    Node PRESENT_ADDRESSn = PRESENT_ADDRESS.item(0);
                    sPresentAddress = PRESENT_ADDRESSn.getTextContent();
//System.out.println(sSession);
                    NodeList PERMANANT_ADDRESS = doc.getElementsByTagName("PERMANANT_ADDRESS");
                    Node PERMANANT_ADDRESSn = PERMANANT_ADDRESS.item(0);
                    sPermanantAddress = PERMANANT_ADDRESSn.getTextContent();
//System.out.println(sSession);
                    NodeList DATE_OF_BIRTH = doc.getElementsByTagName("DATE_OF_BIRTH");
                    Node DATE_OF_BIRTHn = DATE_OF_BIRTH.item(0);
                    sDateOfBirth = DATE_OF_BIRTHn.getTextContent();
//System.out.println(sSession);
                    NodeList BIRTH_PLACE = doc.getElementsByTagName("BIRTH_PLACE");
                    Node BIRTH_PLACEn = BIRTH_PLACE.item(0);
                    sBirthPlace = BIRTH_PLACEn.getTextContent();
//System.out.println(sSession);
                    NodeList PHOTOGRAPH = doc.getElementsByTagName("PHOTOGRAPH");
                    Node PHOTOGRAPHn = PHOTOGRAPH.item(0);
                    sPhoto = PHOTOGRAPHn.getTextContent();
//System.out.println(sSession);
                    NodeList EXPIRY_DATE = doc.getElementsByTagName("EXPIRY_DATE");
                    Node EXPIRY_DATEn = EXPIRY_DATE.item(0);
                    sExpiryDate = EXPIRY_DATEn.getTextContent();
//System.out.println(sSession);
                    NodeList CARD_TYPE = doc.getElementsByTagName("CARD_TYPE");
                    Node CARD_TYPEn = CARD_TYPE.item(0);
                    sCardType = CARD_TYPEn.getTextContent();

                } else {////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // System.out.println("Here 5");
                    NodeList MESSAGE = doc.getElementsByTagName("MESSAGE");
                    Node MESSAGEn = MESSAGE.item(0);
                    sMessage = MESSAGEn.getTextContent();
//System.out.println(sMessage );
                    NodeList SESSION_ID = doc.getElementsByTagName("SESSION_ID");
                    Node SESSION_IDn = SESSION_ID.item(0);
                    sSession = SESSION_IDn.getTextContent();
                    activityLog.setCustomerSessionId("");
                    activityLog.setCustomerSessionId(sSession);
//System.out.println(sSession);
                    NodeList CITIZEN_NUMBER = doc.getElementsByTagName("CITIZEN_NUMBER");
                    Node CITIZEN_NUMBERn = CITIZEN_NUMBER.item(0);
                    sCitiznNum = CITIZEN_NUMBERn.getTextContent();

                }
                // System.out.println("SELECT FNO, CNIC FROM CUS_BMDATA WHERE CNIC='" + userID + "' AND FNO=" + fingerIndex);
                addNADRAAuditCUS(sCode, sMessage, sSession, sCitiznNum, template, fingerIndex, sIPAddr, oraConInfo, oraConName, oraConPass, log, file,activityLog);
                if (sCode.contains("100")) {
                    String[] IPs = new String[20];
                    int[] Ports = new int[20];
                    int TotalFound = 0;
                    //geting servers list to write file
                   try {
                        
                        Class.forName("oracle.jdbc.driver.OracleDriver");
                        Connection conCh = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

                        Statement stmtCh = conCh.createStatement();
                        String statement = "SELECT FNO, CNIC FROM CUS_BMDATA WHERE CNIC='" + sUsrCNIC + "'";
                        // System.out.println(statement);
                        ResultSet rsCh = stmtCh.executeQuery(statement);
                        if (rsCh.next()) {
                            try {
                                Class.forName("oracle.jdbc.driver.OracleDriver");
                                Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);

                                Statement stmt = con.createStatement();
                                ResultSet rs = stmt.executeQuery("SELECT SNAME, SPORT FROM CUS_TSSERVERS");
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
                                activityLog.setRESPONSE_CODE("NOO 19 3");
                                if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                                return "N " + "NOO 19 3 Unable to connect to database";
                            } catch (SQLException ex) {
                                System.out.println(ex);
                                activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
                                activityLog.setSTATUS(activityLog.FAIL);
                                activityLog.setRESPONSE_CODE("NOO 19 4");
                                if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                                return "N " + "NOO 19 4 Unable to connect to database";
                            }
                            if (TotalFound == 0) {
                                System.out.print("|Not loged in|NoServerToWrite");
                                // LogCreation.writeLog("|Not loged in|NoServerToWrite", bufferedwriter);
                                log = log + "|Not loged in|NoServerToWrite";
                                activityLog.setRESPONSE_MESSAGE("Unable to write");
                                activityLog.setSTATUS(activityLog.FAIL);
                                activityLog.setRESPONSE_CODE("NOO 19 1");
                                if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                                return "N " + "NOO 19 1 Unable to write";
                            }
                            //done selecting servers
                            System.out.println("\nTotal Found: " + TotalFound);
                            for (int tm = 0; tm < TotalFound; tm++) {
                                System.out.println("Server: " + IPs[tm] + "\tPort: " + Ports[tm] + "|");
                            }
                            Socket s = null;
                            Random rand = new Random();
                            int randomNum = rand.nextInt(((TotalFound - 1) - 0) + 1) + 0;//
                            boolean IsRepeat = false;
                            int Current = randomNum;
                            System.out.print("\n\nRndom: " + randomNum);
                            //Round Robning for server Connection
                            for (int tryNo = 0; tryNo < TotalFound; tryNo++) {

                                try {
                                    s = new Socket(IPs[Current], Ports[Current]);
                                    System.out.println("Connected With Server: " + IPs[Current] + "\tPort: " + Ports[Current]);
                                } catch (Exception ex) {

                                    System.out.println("00 5" + ex);

                                }
                                if (s != null) {
                                    break;
                                }
                                if (IsRepeat) {
                                    Current++;
                                    if (Current == randomNum) {
                                        System.out.print("|Not loged in|CanNotConnect");
                                        //  LogCreation.writeLog("|Not loged in|CanNotConnect", bufferedwriter);
                                        log = log + "|Not loged in|CanNotConnect";
                                        activityLog.setRESPONSE_MESSAGE("Unable to write");
                                        activityLog.setSTATUS(activityLog.FAIL);
                                        activityLog.setRESPONSE_CODE("NOO 19 1");
                                        if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                                        return "N " + "NOO 19 1 Unable to write";
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
                                // LogCreation.writeLog("|Not loged in|CanNotConnect", bufferedwriter);
                                log = log + "|Not loged in|CanNotConnect";
                                activityLog.setRESPONSE_MESSAGE("Unable to write");
                                activityLog.setSTATUS(activityLog.FAIL);
                                activityLog.setRESPONSE_CODE("NOO 19 1");
                                if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                                return "N " + "NOO 19 1 Unable to write";
                            }
                            try {
                                if(sExpiryDate.equalsIgnoreCase("Lifetime")){
                                    sExpiryDate="2099-12-31";
                                    
                                }
                                s.setSoTimeout(60000);
                                DataOutputStream ps = new DataOutputStream(s.getOutputStream());
                                String string = "bMsOkK#" + userID + ".txt:" + sCardType + "|" + sExpiryDate + "\n";
                                byte[] b = string.getBytes(Charset.forName("UTF-8"));
                                ps.write(b);
                                ps.flush();
                                DataInputStream dis = new DataInputStream((s.getInputStream()));
                                String responce = dis.readLine();
                                s.close();
                                System.out.print("|BioVerified");
                                // LogCreation.writeLog("|BioVerified", bufferedwriter);
                                log = log + "|BioVerified";
                            } catch (IOException e) {
                                System.out.print("|Not Not Logged");
                                //LogCreation.writeLog("|Not Logged", bufferedwriter);
                                log = log + "|Not Logged";
                                activityLog.setRESPONSE_MESSAGE("Unable to write");
                                activityLog.setSTATUS(activityLog.FAIL);
                                activityLog.setRESPONSE_CODE("NOO 19 2");
                                if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                                return "N " + "NOO 19 2 Unable to write";

                            } catch (Exception ex) {
                                System.out.print("|Not Not Logged");
                                // LogCreation.writeLog("|Not Logged", bufferedwriter);
                                log = log + "|Not Logged";
                                activityLog.setRESPONSE_MESSAGE("Unable to write");
                                activityLog.setSTATUS(activityLog.FAIL);
                                activityLog.setRESPONSE_CODE("NOO 19 2");
                                if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                                return "N " + "NOO 19 2 Unable to write";
                            }

                            String newImg = new String(sPhoto.getBytes(), StandardCharsets.UTF_8);
                            String newDOB=SideWork.formateDOB(sDateOfBirth);
                             String newExDate=SideWork.formateExpDate(sExpiryDate);
                             String newCardType=SideWork.formateCardType(sCardType);
                             activityLog.setRESPONSE_MESSAGE(sMessage);
                             activityLog.setSTATUS(activityLog.SUCCESS);
                             activityLog.setRESPONSE_CODE("Y");
                            // String version =activityLog.getFLAG();
                             try{
                            String name = Base64.getEncoder().encodeToString(sName.getBytes());
                            String fHName = Base64.getEncoder().encodeToString(sFater_Husband_Name.getBytes());
                            String presAddress = Base64.getEncoder().encodeToString(sPresentAddress.getBytes());
                            String permAddress = Base64.getEncoder().encodeToString(sPermanantAddress.getBytes());
                            String dateOfBirth = Base64.getEncoder().encodeToString(newDOB.getBytes());
                            String birthPlace = Base64.getEncoder().encodeToString(sBirthPlace.getBytes());
                            String expDate = Base64.getEncoder().encodeToString(newExDate.getBytes());
                            String cardType = Base64.getEncoder().encodeToString(newCardType.getBytes());
                            InsertCustomerInfo(sUsrCNIC.toString(), name, fHName, presAddress, permAddress, dateOfBirth, birthPlace, expDate, cardType, newImg);
                             }catch(Exception ex){
                                 if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                                 System.out.println("Encoding Error"+ex);
                             }
                                 deleteFromSessionTempRecord(sUsrCNIC.toString());
                                 return "Y " + sMessage + "|" + sName + "|" + sFater_Husband_Name + "|" + sPresentAddress + "|" + sPermanantAddress + "|" + sDateOfBirth + "|" + sBirthPlace + "|" + sExpiryDate + "|" + sCardType + "|" + newImg + "|"+userID+"|";
                        }
                        conCh.close();
                        rsCh.close();

                    } catch (ClassNotFoundException ex) {
                        System.out.println(ex);
                        activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
                        activityLog.setSTATUS(activityLog.FAIL);
                        activityLog.setRESPONSE_CODE("NOO 19 3");
                        if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                        return "N " + "NOO 19 3 Unable to connect to database";
                    } catch (SQLException ex) {
                        System.out.println(ex);
                        activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
                        activityLog.setSTATUS(activityLog.FAIL);
                        activityLog.setRESPONSE_CODE("NOO 19 4");
                        if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                        return "N " + "NOO 19 4 Unable to connect to database";
                    }
//
////                    String result = addNadraSuccess(sCode, sMessage, sSession, sCitiznNum, sName, sFater_Husband_Name, sPresentAddress, sPermanantAddress, sDateOfBirth, sPhoto, sExpiryDate, sCardType, sBirthPlace, sIPAddr, oraConInfo, oraConName, oraConPass, log, file);
////                    if (result.contains("OK")) {
////                     String newImg = new String(sPhoto.getBytes(), StandardCharsets.UTF_8);
                     deleteFromSessionTempRecord(sUsrCNIC.toString());
                   return "Y1";//finger is verified but information is not being shown
////                    } else {
////                        return "N " + result;
////                    }
                } else {
                    if (sCode.equals("118")) {
                         activityLog.setRESPONSE_MESSAGE("Select an other finger to verify");
                         activityLog.setSTATUS(activityLog.FAIL);
                         activityLog.setRESPONSE_CODE("N");
                         if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                        return "N Select an other finger to verify|" + sSession + "/" + oldTranID; 
                        // return "N Select an other finger to verify"+ "|" + sSession+"/"+franchizeID + TransactionId;
                    }
                    else if(sCode.equals("110") ||sCode.equals("111")||sCode.equals("112")||sCode.equals("114")||sCode.equals("115")||sCode.equals("119")||sCode.equals("120")||sCode.equals("121")||sCode.equals("122")||sCode.equals("123")||sCode.equals("124")||sCode.equals("125")||sCode.equals("175")||sCode.equals("185")||sCode.equals("186")){
                    activityLog.setRESPONSE_MESSAGE(sMessage);
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("N");
                    if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                    //return "N " + sMessage + "|" + sSession ;//+ "/" + franchizeID + TransactionId;
                    return "N " + sMessage + "|" + sSession + "/" + oldTranID ;
                    }else{
                         activityLog.setRESPONSE_MESSAGE("Try Again Verification Failed");
                         activityLog.setSTATUS(activityLog.FAIL);
                         activityLog.setRESPONSE_CODE("N");
                         if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                        return "N Try Again Verification Failed|";
                    }
                }
            } catch (Exception x) {
                 //System.out.println("Here 1" + x);
                addNADRAAuditCUS(sCode, sMessage, sSession, sCitiznNum, template, fingerIndex, sIPAddr, oraConInfo, oraConName, oraConPass, log, file,activityLog);
                activityLog.setRESPONSE_MESSAGE(sMessage);
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("N");
                if (sessionFound) {
                            insertToSessionTempRecord(sUsrCNIC.toString(), sessionId, oldTranID, "");
                        }
                return "N " + sMessage + "|" + sSession;

            }
            // return "N|NOO 08 9 Invalied Input for NADRA request";
        } catch (Exception ex) {
            // System.out.println("Here 2");
            System.out.println("\n\n" + ex);
            activityLog.setRESPONSE_MESSAGE("Unable to connect with external biometric server");
            activityLog.setSTATUS(activityLog.FAIL);
            activityLog.setRESPONSE_CODE("N");
            return "N|Unable to connect with external biometric server";
            //return "Y 1";
        }
        // return "N|NOO 08 9 Invalied Input for NADRA request";
    }

    public static void addNADRAAuditCUS(String code, String Msg, String SessionID, String CNIC, byte[] finISO, String fingerIndex, String sIPAddr, String oraConInfo, String oraConName, String oraConPass, String log, File file,ActivityLog activityLog) {
        try {
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);
            String sql = "INSERT INTO CUS_NADRALOG(RESPONSECODE, RESPONSEMSG, SESSIONID, CNIC, FNO, TIME, BDATA,CLIENTIP) VALUES( '" + code + "','" + Msg + "','" + SessionID + "','" + CNIC + "'," + fingerIndex + ",CURRENT_TIMESTAMP" + ", ?,'" + sIPAddr + "')";
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

            // System.out.println("\n\n\n\n\n ok auditlog");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public static String addNadraSuccess(String code, String Msg, String Session_ID, String citizen_no, String cus_name, String father_husband_name, String presnt_address, String permanent_address, String Dob, String photo, String expiry_date, String card_type, String birth_place, String sIPAddr, String oraConInfo, String oraConName, String oraConPass, String log, File file,ActivityLog activityLog) {
        try {

            //  System.out.println(cus_name);
            //cus_name=String.
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object  
            Connection con = DriverManager.getConnection(oraConInfo, oraConName, oraConPass);
            String sql = "INSERT INTO CUS_SUCCESS_RES(CODE,MESSAGE,SESSINON_ID,CITIZEN_NO,CUS_NAME,FATHER_HUSBAND_NAME,PRESENT_ADDRESS,PERASANT_ADDRESS,DATE_OF_BIRTH,PHOTOGRAPH,EXPIRY_DATE,CARD_TYPE,BIRTH_PLACE) VALUES('" + code + "','" + Msg + "','" + Session_ID + "','" + citizen_no + "','" + cus_name + "','" + father_husband_name + "','" + presnt_address + "','" + permanent_address + "','" + Dob + "'," + " ?" + ",'" + expiry_date + "','" + card_type + "','" + birth_place + "')";
            // String sql = "INSERT INTO CUS_SUCCESS_RES(CODE,MESSAGE,SESSINON_ID,CITIZEN_NO,CUS_NAME,FATHER_HUSBAND_NAME,PRESENT_ADDRESS,PERASANT_ADDRESS,DATE_OF_BIRTH,PHOTOGRAPH,EXPIRY_DATE,CARD_TYPE,BIRTH_PLACE) VALUES( '" + code + "','" + Msg + "','" + Session_ID + "','" + citizen_no + "','" + " " + "','" + " " + "','" + " " + "','" + " " + "','" + " " + "','" + " " + "','" + " " + "','" + " " + "','" + " " + "')";
            byte[] photograph = null;
            photograph = photo.getBytes();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setBytes(1, photograph);
            pstmt.executeUpdate();
            con.commit();
            con.close();
            // System.out.println("\n\n\n\n\n ok successlog");

        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            activityLog.setRESPONSE_CODE("NOO 19 3");
            return "NOO 19 3 Unable to connect to database";
        } catch (SQLException e) {
            System.out.println(e);
            activityLog.setRESPONSE_MESSAGE("Unable to connect to database");
            activityLog.setSTATUS(activityLog.FAIL);
            activityLog.setRESPONSE_CODE("NOO 19 4");
            return "NOO 19 4 Unable to connect to database";
        }
        return "OK";
    }
public static String InsertCustomerInfo(String CNIC, String NAME,String FHNAME,String PRESADDRESS,String PERMADDRESS,String DATEOFBIRTH,String BIRTHPLACE,String EXPDATE,String CARDTYPE,String IMAGE)
    {
        
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(Config.getCUSINFO_CONDBINFO(), Config.getCUSINFO_CONDBUSERNAME(), Config.getCUSINFO_CONDBPASS());
           // Connection con = DriverManager.getConnection("jdbc:oracle:thin:@10.224.20.132:1526:bmtest", "t24fp", "Allied2016");
            Statement stmt = con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from CUS_INFO WHERE CNIC='"+CNIC+"'");
            if(rs.next()){
                 String query="declare varImg clob;BEGIN varImg:= '"+IMAGE+"';UPDATE CUS_INFO SET \"NAME\"='"+NAME+"',FHNAME='" + FHNAME + "',PRESENT_ADDRESS='" + PRESADDRESS + "',PERMANENT_ADDRESS='" + PERMADDRESS + "',DOB='" + DATEOFBIRTH + "',BIRTH_PLACE='" + BIRTHPLACE + "',EXP_DATE='" + EXPDATE + "',CARD_TYPE='" + CARDTYPE + "',IMAGE=varImg WHERE CNIC='" + CNIC + "';END;";
             int count = stmt.executeUpdate(query);
            }else{
                String query="declare varImg clob;BEGIN varImg:= '"+IMAGE+"';INSERT INTO CUS_INFO (CNIC,\"NAME\",FHNAME,PRESENT_ADDRESS,PERMANENT_ADDRESS,DOB,BIRTH_PLACE,EXP_DATE,CARD_TYPE,IMAGE) VALUES('" + CNIC + "','"+NAME+"','" + FHNAME + "','" + PRESADDRESS + "','" + PERMADDRESS + "','" + DATEOFBIRTH + "','" + BIRTHPLACE + "','" + EXPDATE + "','" + CARDTYPE + "',varImg);END;";
                int count = stmt.executeUpdate(query);
            }
            con.commit();
             con.close();
        }
        catch(ClassNotFoundException ex){
            System.err.println(ex+"Fail to save info"+CNIC);
            //return ex.toString();
        }
        catch(SQLException ex){
            System.err.println(ex+"Fail to save info"+CNIC);
            //return ex.toString();
        }
        
        
        return " OK";
    }
 public static String insertToSessionTempRecord(String CNIC, String sessionID, String transactionId, String flag) {

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(Config.getCUSINFO_CONDBINFO(), Config.getCUSINFO_CONDBUSERNAME(), Config.getCUSINFO_CONDBPASS());
            // Connection con = DriverManager.getConnection("jdbc:oracle:thin:@10.224.20.132:1526:bmtest", "t24fp", "Allied2016");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM SESSION_TEMP_LOGS WHERE CNIC='" + CNIC + "'");
            if (rs.next()) {
            } else {
                String query = "INSERT INTO SESSION_TEMP_LOGS (CNIC,SESS_ID,TRANS_ID,FLAG) VALUES('" + CNIC + "','" + sessionID + "','" + transactionId + "','" + flag + "')";
                int count = stmt.executeUpdate(query);
            }
            con.commit();
            con.close();
        } catch (ClassNotFoundException ex) {
            System.err.println(ex + "Fail to save sessionTemp_log" + CNIC);
            //return ex.toString();
        } catch (SQLException ex) {
            System.err.println(ex + "Fail to save sessionTemp_log" + CNIC);
            //return ex.toString();
        }

        return " OK";
    }
    public static String deleteFromSessionTempRecord(String CNIC) {

        try {
            System.err.println("Inside atmTemp_log");
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(Config.getCUSINFO_CONDBINFO(), Config.getCUSINFO_CONDBUSERNAME(), Config.getCUSINFO_CONDBPASS());
            // Connection con = DriverManager.getConnection("jdbc:oracle:thin:@10.224.20.132:1526:bmtest", "t24fp", "Allied2016");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("DELETE FROM SESSION_TEMP_LOGS WHERE CNIC='" + CNIC + "'");
            con.commit();
            con.close();
        } catch (ClassNotFoundException ex) {
            System.err.println(ex + "Fail to dlete sessionTemp_log" + CNIC);
            //return ex.toString();
        } catch (SQLException ex) {
            System.err.println(ex + "Fail to delete sessionTemp_log" + CNIC);
            //return ex.toString();te
        }

        return " OK";
    }
}

//CODE VARCHAR2(10),
//  MESSAGE VARCHAR2(250),
//  SESSINON_ID VARCHAR2(50),
//  CITIZEN_NO VARCHAR2(20),
//  CUS_NAME VARCHAR2(250),
//  FATHER_HUSBAND_NAME VARCHAR2(250),
//  PRESENT_ADDRESS VARCHAR2(750),
//  PERASANT_ADDRESS VARCHAR2(750),
//  DATE_OF_BIRTH VARCHAR2(300),
//  PHOTOGRAPH BLOB,
//  EXPIRY_DATE VARCHAR2(20),
//  CARD_TYPE VARCHAR2(30)
