
package com.idea.builders;
public class Config {
    //Configration for Paths
    private static String CUSINFO_CONDBINFO=null;
    private static String CUSINFO_CONDBUSERNAME=null;
    private static String CUSINFO_CONDBPASS=null;
    public static final String MATCHING_API_PATH="/usr/local/bin/biosecfpm";
    public static final String SERVER_LOG_PATH="/usr/ServerLog/";
    public  static  final String MATCHING_FILE_PATH="/usr/tmp/";
    public static final String SERVERID="BMS-20170306-2";
    public static final String DESCRIPTION=""
            + "Build On: 06-03-2017|"
            + "Company: Idea Builders|"
            +"Reason:Save customer Info"
            + "Features: Login Via Local DB And Store Record Of BM Verified Employees Into DB for Login,Contains Customer Contact Number, Mark Attandence for Only Active Branches|"
            + "New Features: DB base Session handling & Banner configuration|"
            + "Builed By: Ali Raza|"
            + "Emergency Contact: Burhan-Ul-Haq 03004070110, Ali Raza 03085507943|"
            + "license Type: Unlimmited User License For ABL"
            + "Developed By: Burhan-Ul-Haq, Ali Raza|";

    public static String getCUSINFO_CONDBINFO() {
        return CUSINFO_CONDBINFO;
    }

    public static void setCUSINFO_CONDBINFO(String CUSINFO_CONDBINFO) {
        Config.CUSINFO_CONDBINFO = CUSINFO_CONDBINFO;
    }

    public static String getCUSINFO_CONDBUSERNAME() {
        return CUSINFO_CONDBUSERNAME;
    }

    public static void setCUSINFO_CONDBUSERNAME(String CUSINFO_CONDBUSERNAME) {
        Config.CUSINFO_CONDBUSERNAME = CUSINFO_CONDBUSERNAME;
    }

    public static String getCUSINFO_CONDBPASS() {
        return CUSINFO_CONDBPASS;
    }

    public static void setCUSINFO_CONDBPASS(String CUSINFO_CONDBPASS) {
        Config.CUSINFO_CONDBPASS = CUSINFO_CONDBPASS;
    }
    

    

    public static String getSERVER_LOG_PATH() {
        return SERVER_LOG_PATH;
    }
    
    //Configration for Server Cheshe
    private static int OPERATION_ID;

    public static int getOPERATION_ID() {
        return OPERATION_ID;
    }

    public static void setOPERATION_ID(int OPERATION_ID) {
        Config.OPERATION_ID = OPERATION_ID;
    }
    
    //Configration for Batabase Server
    private static String DB_Server_Info;
    private static String DB_Server_UserName;
    private static String DB_Server_Password;
    
    //Configration for Batabase Server
    private static String Log_Mode;
    
    //Configration for NADRA Staff Verification Service
    private static String NADRA_Staff_FranchiseID;
    private static String NADRA_Staff_UserName;
    private static String NADRA_Staff_Password;
    
    //Configration for NADRA Customer Verification Service
    private static String NADRA_Customer_FranchiseID;
    private static String NADRA_Customer_UserName;
    private static String NADRA_Customer_Password;

    //Configration for FingerList (How many fingers to show client side)
    private static String Finger_List;
    
    ////Configration for Staff Verification File Write Server
    private static String[] Staff_Verify_Write_IP;
    private static int[] Staff_Verify_Write_Port;
    
    ////Configration for Staff Verification File Write Server
    private static String[] Customer_Verify_Write_IP;
    private static int[] Customer_Verify_Write_Port;
    
    
    
    ///Getter and Setters
    public static String getDB_Server_Info() {
        return DB_Server_Info;
    }

    public static void setDB_Server_Info(String aDB_Server_Info) {
        DB_Server_Info = aDB_Server_Info;
    }

    public static String getDB_Server_UserName() {
        return DB_Server_UserName;
    }

    public static void setDB_Server_UserName(String aDB_Server_UserName) {
        DB_Server_UserName = aDB_Server_UserName;
    }

    public static String getDB_Server_Password() {
        return DB_Server_Password;
    }

    public static void setDB_Server_Password(String aDB_Serve_Password) {
        DB_Server_Password = aDB_Serve_Password;
    }
    
    public static String getLog_Mode() {
        return Log_Mode;
    }

    public static void setLog_Mode(String aLog_Mode) {
        Log_Mode = aLog_Mode;
    }

    public static String getNADRA_Staff_FranchiseID() {
        return NADRA_Staff_FranchiseID;
    }

    public static void setNADRA_Staff_FranchiseID(String aNADRA_Staff_FranchiseID) {
        NADRA_Staff_FranchiseID = aNADRA_Staff_FranchiseID;
    }

    public static String getNADRA_Staff_UserName() {
        return NADRA_Staff_UserName;
    }

    public static void setNADRA_Staff_UserName(String aNADRA_Staff_UserName) {
        NADRA_Staff_UserName = aNADRA_Staff_UserName;
    }

    public static String getNADRA_Staff_Password() {
        return NADRA_Staff_Password;
    }

    public static void setNADRA_Staff_Password(String aNADRA_Staff_Password) {
        NADRA_Staff_Password = aNADRA_Staff_Password;
    }

    public static String getNADRA_Customer_FranchiseID() {
        return NADRA_Customer_FranchiseID;
    }

    public static void setNADRA_Customer_FranchiseID(String aNADRA_Customer_FranchiseID) {
        NADRA_Customer_FranchiseID = aNADRA_Customer_FranchiseID;
    }

    public static String getNADRA_Customer_UserName() {
        return NADRA_Customer_UserName;
    }

    public static void setNADRA_Customer_UserName(String aNADRA_Customer_UserName) {
        NADRA_Customer_UserName = aNADRA_Customer_UserName;
    }

    public static String getNADRA_Customer_Password() {
        return NADRA_Customer_Password;
    }

    public static void setNADRA_Customer_Password(String aNADRA_Customer_Password) {
        NADRA_Customer_Password = aNADRA_Customer_Password;
    }

    public static String getFinger_List() {
        return Finger_List;
    }

    public static void setFinger_List(String aFinger_List) {
        Finger_List = aFinger_List;
    }

    public static String[] getStaff_Verify_Write_IP() {
        return Staff_Verify_Write_IP;
    }

    public static void setStaff_Verify_Write_IP(String[] aStaff_Verify_Write_IP) {
        Staff_Verify_Write_IP = aStaff_Verify_Write_IP;
    }

    public static int[] getStaff_Verify_Write_Port() {
        return Staff_Verify_Write_Port;
    }

    public static void setStaff_Verify_Write_Port(int[] aStaff_Verify_Write_Port) {
        Staff_Verify_Write_Port = aStaff_Verify_Write_Port;
    }

    public static String[] getCustomer_Verify_Write_IP() {
        return Customer_Verify_Write_IP;
    }

    public static void setCustomer_Verify_Write_IP(String[] aCustomer_Verify_Write_IP) {
        Customer_Verify_Write_IP = aCustomer_Verify_Write_IP;
    }

    public static int[] getCustomer_Verify_Write_Port() {
        return Customer_Verify_Write_Port;
    }

    public static void setCustomer_Verify_Write_Port(int[] aCustomer_Verify_Write_Port) {
        Customer_Verify_Write_Port = aCustomer_Verify_Write_Port;
    }
}
