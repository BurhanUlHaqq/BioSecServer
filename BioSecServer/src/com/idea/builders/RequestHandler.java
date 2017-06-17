package com.idea.builders;

import static com.src.server.actions.CommonActions.checkUsr;
import static com.src.server.actions.CommonActions.getCustomerFingers;
import static com.src.server.actions.CommonActions.getEmpFingers;
import static com.src.server.actions.CommonActions.getEmpnoFromUid;
import com.src.server.actions.RegistrationProcess;
import static com.src.server.actions.ServerLog.addServerLog;
import static com.src.server.actions.SideWork.extractEIN;
import static com.src.server.actions.VerificationProcess.verifyFP;
import static com.src.server.actions.AttendanceProcess.attendFP;
import static com.src.server.actions.VerificationProcess.verifySupervisor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.xml.bind.DatatypeConverter;

public class RequestHandler extends SimpleChannelInboundHandler<byte[]> {

    private ByteBuf buffer;
    private int i = 0;
    private final int Max_Itration = 8;
    public static Boolean exceptionOccure = false;
    static File filef = null;
    ActivityLog activityLog = null;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        if (!ctx.isRemoved()) {
            if (activityLog.attendCall == true) {
                System.out.print("Invalid Location");
                responce(ctx, "NOO 10 0 Invalid Location", activityLog);
            } else {
                System.out.print("|ExceptionCaught" + cause);
                exceptionOccure = true;
                System.err.println("in ExceptionCaught");
                responce(ctx, "NOO 02 0 Timeout Disconnected", activityLog);
            }
        }
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        //initializ butter
        buffer = ctx.alloc().buffer(Max_Itration * 4);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        activityLog = new ActivityLog();
        if (i < Max_Itration) {//check if data received is less then max reques size
            buffer.writeBytes(msg);
            i++;
        } else {
            activityLog.setRESPONSE_MESSAGE("Invalied Input");
            activityLog.setSTATUS(activityLog.FAIL);
            activityLog.setRESPONSE_CODE("NOO 01 0");
            responce(ctx, "NOO 01 0 Invalied Input", activityLog);

        }
        oprationSelector(ctx, activityLog);
    }

    protected void oprationSelector(ChannelHandlerContext ctx, ActivityLog activityLog) throws InterruptedException, ClassNotFoundException {
        //check request size it is completed or not
        if (buffer.readableBytes() == 1006 || buffer.readableBytes() == 1806) {
            switch (buffer.readableBytes()) {
                case 1006: {
                    Config.setOPERATION_ID(0);//0 means working with 1006 bytes
                    break;
                }
                case 1806: {
                    Config.setOPERATION_ID(1);//1 means working with 1806 bytes
                    break;
                }
            }
            String log = null;
            String timeStamp = new SimpleDateFormat("SSS:ss:mm:hh-dd-MM-yyyy").format(Calendar.getInstance().getTime());
            System.out.print(timeStamp);
            System.out.print("|connected");
            activityLog.LOGDATA = activityLog.LOGDATA + timeStamp + "|connected";
            log = log + timeStamp + "|connected";
            int readloc = 0;
            ByteBuffer buff = ByteBuffer.allocate(1807);
            while (buffer.isReadable()) {
                buff.put(readloc, buffer.readByte());
                readloc++;
            }
            buffer.release();// data is stored in buff
            Channel ch = ctx.channel();
            String RemoteIP = ch.remoteAddress() + "";
            System.out.print("|" + RemoteIP);
            System.out.print("|readBytes=" + readloc);
            activityLog.LOGDATA = activityLog.LOGDATA + RemoteIP + "|readBytes=" + readloc;
            log = log + RemoteIP + "|readBytes=";
            if (readloc == 1006 || readloc == 1806) {//extra check to ensure the complete request is coppied or not
                responce(ctx, oprations(RemoteIP, buff, log, filef, activityLog), activityLog);

            } else {
                activityLog.setRESPONSE_MESSAGE("Invalid Input");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 11 8");
                responce(ctx, "NOO 11 8 Invalid Input", activityLog);
            }
        } else if (i >= Max_Itration) {
            System.out.print("|Taking much time than usual!");
        }
    }

    //Response to client and distroy the connection
    public static synchronized void responce(final ChannelHandlerContext ctx, String s, ActivityLog activityLog) {
        System.out.print("|Response=" + s);
        String userID = activityLog.getUSER_ID();
        String CNIC = activityLog.getCNIC();
        String clientIP = activityLog.getClientIP();
        String cmd = activityLog.getCOMMAND();
        String serverID = Config.SERVERID;
        String supervise = activityLog.getSUPERVISION();
        String resCode = null;
        String resMsg = null;
        String reqStatus = null;
        if (activityLog.attendCall == true) {
            activityLog.setAttendCall(false);
            resCode = "NOO 10 0";
            resMsg = "Invalid Location";
            reqStatus = activityLog.FAIL;
        } else if (exceptionOccure) {
            exceptionOccure = false;
            resCode = "NOO 02 0";
            resMsg = "Timeout Disconnected";
            reqStatus = activityLog.FAIL;
        } else {
            resCode = activityLog.getRESPONSE_CODE();
            resMsg = activityLog.getRESPONSE_MESSAGE();
            reqStatus = activityLog.getSTATUS();
        }

        String flag = activityLog.getFLAG();

        String oraConInfo = Config.getDB_Server_Info();
        String oraConName = Config.getDB_Server_UserName();
        String oraConPass = Config.getDB_Server_Password();
        // adding data into ServerLog by using addServerLog method
        activityLog.LOGDATA = activityLog.LOGDATA + " |ResponeCode:" + resCode + " |ResponseMessage:" + resMsg + "|Flag:" + flag + "|Disconnected\n";
        // LogCreation.writeLog(activityLog.LOGDATA, filef);
        addServerLog(serverID, userID, CNIC, clientIP, cmd, supervise, resCode, resMsg, reqStatus, flag, oraConInfo, oraConName, oraConPass);
        final ByteBuf time = ctx.alloc().buffer(4);
        time.writeBytes(s.getBytes());
        final ChannelFuture f = ctx.writeAndFlush(time);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                assert f == future;
                ctx.close();
                System.err.println("|Disconnected\n");
            }
        });
    }
    
    //Processing is started and proper response will be sent from here

    private String oprations(String RemoteIP, ByteBuffer buff, String log, File file, ActivityLog activityLog) throws InterruptedException, ClassNotFoundException {
        String oraConInfo = Config.getDB_Server_Info();
        String oraConName = Config.getDB_Server_UserName();
        String oraConPass = Config.getDB_Server_Password();
        //String writeFileServerIp = serverInfo[5];
        //int writeFilePort = Integer.parseInt(serverInfo[6]);
        //String LogMode = serverInfo[7];
        String sCmd;
        String sUid;
        String extractedEIN;
        String sEmpno;
        String appValue;
        String sCnic;
        String sFNo;
        String sSuper;
        String sfinData;
        String sEmpName;
        String customerContactNo;
        byte[] finISO = new byte[800];
        byte[] finISOLocal = new byte[800];
        byte[] bytebuf = null;
        if (Config.getOPERATION_ID() == 0) {
            bytebuf = new byte[1007];
            for (int i = 0; i < 1007; i++) {
                bytebuf[i] = buff.get(i);
            }
        } else if (Config.getOPERATION_ID() == 1) {
            bytebuf = new byte[1807];
            for (int i = 0; i < 1807; i++) {
                bytebuf[i] = buff.get(i);
            }
        }
        byte[] byteCmd = new byte[200];
        String resIP = RemoteIP;
        //System.out.println(resIP);
        String[] sSplitIP = resIP.split("/");
        String clientIp = sSplitIP[1];
        String[] sSplit2IP = clientIp.split(":");
        clientIp = sSplit2IP[0];
        // -------------------------
        // 1. convert first 200 bytes of byte array to string.
        // 2. convert 201 to 800 bytes to another byte array.
        // 3. use string tokens. get fingers should be disabled.
        // 4. 
        System.arraycopy(bytebuf, 0, byteCmd, 0, 200);//bytebuff 0 to 199 cmd 

        System.arraycopy(bytebuf, 200, finISO, 0, 800);//bytebuff 200 to 1000 fingerprit

        String ISOImgLocal = null;
        if (Config.getOPERATION_ID() == 1) {
            System.arraycopy(bytebuf, 1000, finISOLocal, 0, 800);//bytebuff 1006 to 1806 fingerprit
            ISOImgLocal = new String(DatatypeConverter.printBase64Binary(finISOLocal)); //template1 is real fingerprint
        }

        String ISOImg = new String(DatatypeConverter.printBase64Binary(finISO)); //template1 is real fingerprint

        String sCmdL = new String(byteCmd);

        String[] tokens = sCmdL.split("\n");
        String[] cmdLabels = {"Cmd", "Uid", "cnic", "fno", "super", "finData"};

        sCmd = "";
        sUid = "";
        extractedEIN = "";
        sEmpno = "";
        sCnic = "";
        appValue = "";
        sFNo = "";
        sSuper = "";
        sfinData = "";
        sEmpName = "";
        customerContactNo = "";
        for (int i = 0; (i < tokens.length) && (i < 6); i++) {
            // System.out.println(cmdLabels[i] + i + " > " + tokens[i] + "<");
            if (i == 0) {
                sCmd = tokens[i].trim();
            }
            if (i == 1) {
                sUid = tokens[i].trim();
                appValue = sUid;
            }
            if (i == 2) {
                sCnic = tokens[i].trim();
            }
            if (i == 3) {
                sFNo = tokens[i].trim();
            }
            if (i == 4) {
                sSuper = tokens[i].trim();
            }
            if (i == 5) {
                sfinData = tokens[i].trim();
            }
        }
        System.out.print("|Data=(cmd:\"" + sCmd + "\" uid:\"" + sUid + "\"cnic:\"" + sCnic + "\"Fno:\"" + sFNo + "\"Sup:\"" + sSuper + "\"sfin:\"" + sfinData + "\" )");
        activityLog.LOGDATA = activityLog.LOGDATA + "|ServerID=" + Config.SERVERID + "|Data=(cmd:\"" + sCmd + "\" uid:\"" + sUid + "\"cnic:\"" + sCnic + "\"Sup:\"" + sSuper + "\"sfin:\"" + sfinData + "\"  )";
        log = log + "|Data=(cmd:\"" + sCmd + "\" uid:\"" + sUid + "\"cnic:\"" + sCnic + "\"Fno:\"" + sFNo + "\"Sup:\"" + sSuper + "\"sfin:\"" + sfinData + "\" )";
        activityLog.setUSER_ID(sUid);
        activityLog.setCNIC(sCnic);
        activityLog.setClientIP(clientIp);
        activityLog.setCOMMAND(sCmd);
        activityLog.setSUPERVISION(sSuper);

// Y for Registration Supervisor verify
        //activityLog.setCNIC("89793847297429");
        if (!sCmd.equalsIgnoreCase("CMD:ATTEND")) {
            sUid = sUid.replaceFirst("^0+(?!$)", "");
            // System.out.println("\n\n\n\n"+sUid+"\n");
        }

        if ((sSuper.equalsIgnoreCase("Y")) || (sSuper.equalsIgnoreCase("V"))) {

            return verifySupervisor(sUid, finISO, clientIp, oraConInfo, oraConName, oraConPass, log, file, activityLog);
        } else {
            if (!sCmd.equalsIgnoreCase("CMD:ATTEND") && !sCmd.equalsIgnoreCase("CMD:GETFINGERS")) {
                // System.out.println("suid= " + sUid);
                String res = null;
                if (sCmd.equalsIgnoreCase("CMD:VERIFY")) {
                    sEmpno = sUid;
                    extractedEIN = extractEIN(sUid);//extract EIN:123 From sUid:Ali123
                    //System.out.println(extractedEIN+"Extracted");
                    res = "";
                    // login via local table start here below 4 lines
//                    activityLog.setIsCMDVerify(true);
//                   String uidWDot = addDot(sUid);//add dot in UID: BUR123 to BUR.123
//                    res = getEmpnoFromUid(uidWDot, oraConInfo, oraConName, oraConPass, log, file,activityLog); 
//                    activityLog.setIsCMDVerify(false);
                } else {
                    //System.out.println("User ID"+sUid);
                    res = getEmpnoFromUid(sUid, oraConInfo, oraConName, oraConPass, log, file, activityLog);
                    if (res.contains("NOO 0")) {
                        return res;
                    } else {
//                System.out.println(res);
                        String[] sSplit = res.split("\n");//res returns 12345\nBurhanUl Haqq Zahir\n BURHAN.12345
                        sUid = sSplit[0];
                        sEmpno = sSplit[1];
                        sEmpName = sSplit[2];
                    }
                }

            }
        }
        if (sCmd.equalsIgnoreCase("CMD:VERIFY")) {//123ver

            //extractedUID=sUid;
            return verifyFP(extractedEIN, sEmpno, finISO, clientIp, oraConInfo, oraConName, oraConPass, log, file, activityLog);
            // verify

        } else if (sCmd.equalsIgnoreCase("CMD:ATTEND")) {//123att  "YOO 00 0 Welcome|" + EmpID + "|" + sEmpName + "|Time: " + AttendTime;
            // TimeUnit.SECONDS.sleep(15);
//             activityLog.setRESPONSE_MESSAGE("Attendance disabled it will be available soon Thank You");
//               activityLog.setSTATUS(activityLog.FAIL);
//               activityLog.setRESPONSE_CODE("NOO 00 0");
//            return "NOO 00 0 Attendance disabled it will be available soon Thank You";

            return attendFP(finISO, clientIp, oraConInfo, oraConName, oraConPass, log, file, activityLog);

        } else if (sCmd.equalsIgnoreCase("CMD:REGISTER")) {//123reg  
            // return "NOO 00 0 Registration is off due to maintenance";
            return RegistrationProcess.registerFP(sUid, sEmpName, sCnic, sFNo, finISO, sSuper, sfinData, clientIp, oraConInfo, oraConName, oraConPass, log, file, activityLog);

        } else if (sCmd.equalsIgnoreCase("CMD:GETFINGERS")) {//123reg
            sEmpName = checkUsr(sUid, sCnic, oraConInfo, oraConName, oraConPass, log, file, activityLog);

            // System.out.println("sUsrId= " + sUid + " and usr name: \"" + sEmpName + "\"");
            if (sEmpName == null) //if employee not found
            {
                //writeLog("|NOO 04 6 Invalid EIN / CNIC", bufferedwriter);
                log = log + "|NOO 04 6 Invalid EIN / CNIC";
                activityLog.setRESPONSE_MESSAGE("Invalid EIN / CNIC");
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 04 6");
                return "NOO 04 6 Invalid EIN / CNIC";
            } else if (sEmpName.contains("NOO 0"))//if database error
            {
                activityLog.setRESPONSE_MESSAGE(sEmpName);
                activityLog.setSTATUS(activityLog.FAIL);
                activityLog.setRESPONSE_CODE("NOO 0");
                return sEmpName;
            } else {
                String result = getEmpFingers(sUid, sEmpName, oraConInfo, oraConName, oraConPass, log, file, activityLog);
                if (result.contains("NOO 0"))//if database error
                {
                    activityLog.setRESPONSE_MESSAGE(result);
                    activityLog.setSTATUS(activityLog.FAIL);
                    activityLog.setRESPONSE_CODE("NOO 0");
                    return result;
                } else {
                    activityLog.setRESPONSE_MESSAGE(result + "|" + sEmpName);
                    activityLog.setSTATUS(activityLog.SUCCESS);
                    activityLog.setRESPONSE_CODE("YOO 0");
                    return result + "|" + sEmpName;
                }
            }
        } else if (sCmd.equalsIgnoreCase("CMD:VERIFYCUSTOMER")) {
            // return verifyCustommer(sCnic, finISO, oraConInfo, oraConName, oraConPass, log,file,activityLog);
        } else if (sCmd.equalsIgnoreCase("CMD:REGISTERCUSTOMER")) {
//             activityLog.setRESPONSE_MESSAGE("Service is temporarily unavailable due to maintenance");
//             activityLog.setSTATUS(activityLog.FAIL);
//              activityLog.setRESPONSE_CODE("NOO 00 0");
//              return "NOO 00 0 Service is temporarily unavailable due to maintenance";
            return RegistrationProcess.registerCustomer(sCnic, sfinData, finISO, finISOLocal, sFNo, clientIp, oraConInfo, oraConName, oraConPass, log, file, activityLog, customerContactNo);
        } else if (sCmd.equalsIgnoreCase("CMD:GETCUSTOMMERFINGER")) {
            if (!("CNICVERIFICATION".equalsIgnoreCase(appValue))) {
                activityLog.setRESPONSE_CODE("NOO 00 0");
                activityLog.setRESPONSE_MESSAGE("Invalid Client Version");
                activityLog.setSTATUS(activityLog.FAIL);
                return "NOO 00 0 Invalid Client Version";
            }
            return getCustomerFingers(sCnic, oraConInfo, oraConName, oraConPass, log, file, activityLog);
        } else if (sCmd.equalsIgnoreCase("CMD:GETSERVERINFO")) {

            return Config.SERVERID + "|" + Config.DESCRIPTION;
        }
        activityLog.setRESPONSE_MESSAGE("Invalid input");
        activityLog.setSTATUS(activityLog.FAIL);
        activityLog.setRESPONSE_CODE("NOO 04 9");
        return "NOO 04 9 Invalid input";
    }
}
