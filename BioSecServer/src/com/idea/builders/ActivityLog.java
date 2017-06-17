
package com.idea.builders;

public class ActivityLog {
    public final String SUCCESS="successfull";
    public final String FAIL="fail";
    private String USER_ID=null;
    private String CNIC=null;
     private String ClientIP=null;
      private String COMMAND=null;
      private String SUPERVISION=null;
       private String RESPONSE_CODE=null;
        private String RESPONSE_MESSAGE=null;
        private  String customerSessionId="";
        public Boolean attendCall=false;

    public Boolean isAttendCall() {
        return attendCall;
    }

    public void setAttendCall(Boolean attendCall) {
        this.attendCall = attendCall;
    }

    public String getCustomerSessionId() {
        return customerSessionId;
    }

    public void setCustomerSessionId(String customerSessionId) {
        this.customerSessionId = customerSessionId;
    }
       private String STATUS=null;
        private String FLAG=null;
      public  String LOGDATA="";
public Boolean isCMDVerify=false;
    public Boolean isIsCMDVerify() {
        return isCMDVerify;
    }
    public void setIsCMDVerify(Boolean isCMDVerify) {
        this.isCMDVerify = isCMDVerify;
    }
      
    public String getCOMMAND() {
        return COMMAND;
    }

    

    public void setCOMMAND(String COMMAND) {
        this.COMMAND = COMMAND;
    }
      

    public String getUSER_ID() {
        return USER_ID;
    }

    public void setUSER_ID(String USER_ID) {
        this.USER_ID = USER_ID;
    }

    public String getSUPERVISION() {
        return SUPERVISION;
    }

    public void setSUPERVISION(String SUPERVISION) {
        this.SUPERVISION = SUPERVISION;
    }

    public String getRESPONSE_CODE() {
        return RESPONSE_CODE;
    }

    public void setRESPONSE_CODE(String RESPONSE_CODE) {
        this.RESPONSE_CODE = RESPONSE_CODE;
    }

    public String getFLAG() {
        return FLAG;
    }

    public void setFLAG(String FLAG) {
        this.FLAG = FLAG;
    }
      
   

    public String getRESPONSE_MESSAGE() {
        return RESPONSE_MESSAGE;
    }

    public void setRESPONSE_MESSAGE(String RESPONSE_MESSAGE) {
        this.RESPONSE_MESSAGE = RESPONSE_MESSAGE;
    }

    public String getClientIP() {
        return ClientIP;
    }

    public void setClientIP(String ClientIP) {
        this.ClientIP = ClientIP;
    }

    public String getCNIC() {
        return CNIC;
    }

    public void setCNIC(String CNIC) {
        this.CNIC = CNIC;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }
    
    
    
}
