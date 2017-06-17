package com.idea.builders;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Server {
    //Basic Configration based on resources
    private static int port;
    private final int BOSS_THREADS = 250;
    private final int MAX_WORKER_THREADS = 500;
    private final int MAX_PROCESSES_ON_ONE_PROCESSOR = 100;

    public Server(int p) {
        port = p;
    }

    private int calculateThreadCount() {
        int threadCount;
        if ((threadCount = SystemPropertyUtil.getInt("io.netty.eventLoopThreads", 0)) > 0) {
            return threadCount;
        } else {
            threadCount = Runtime.getRuntime().availableProcessors() * MAX_PROCESSES_ON_ONE_PROCESSOR;
            return threadCount > MAX_WORKER_THREADS ? MAX_WORKER_THREADS : threadCount;
        }
    }
    //Programm strt point
    public void run() throws Exception {

        EventLoopGroup bossPool = new NioEventLoopGroup(BOSS_THREADS);
        EventLoopGroup workerPool = new NioEventLoopGroup(MAX_WORKER_THREADS);

        try {
            ServerBootstrap boot = new ServerBootstrap();
            boot.group(bossPool, workerPool);
            boot.channel(NioServerSocketChannel.class);
            boot.childHandler(new Pipeline());
            boot.option(ChannelOption.TCP_NODELAY, true);
            boot.option(ChannelOption.SO_KEEPALIVE, true);
            boot.option(ChannelOption.SO_REUSEADDR, true);
            boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000);
            boot.option(ChannelOption.SO_TIMEOUT, 6);
            boot.option(ChannelOption.SO_BACKLOG, 2048);
            boot.bind(port).sync().channel().closeFuture().sync();

        } catch (InterruptedException e) {
        } finally {
            workerPool.shutdownGracefully();
            bossPool.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        String[] serverInfo = new String[7];
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////Reading Config File/////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        try (BufferedReader br = new BufferedReader(new FileReader("config.dat"))) {
            int i = 0;
            String line;
            while ((line = br.readLine()) != null && i < 5) {
                serverInfo[i] = decrypt(line);
                i++;
            }
            br.close();
        }
        System.out.println("Wait Server Loading Configrations.....");
        
        Config.setDB_Server_Info(serverInfo[1]);
        Config.setDB_Server_UserName(serverInfo[2]);
        Config.setDB_Server_Password(serverInfo[3]);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////Retreving Necessery Server Info////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(Config.getDB_Server_Info(), Config.getDB_Server_UserName(), Config.getDB_Server_Password());

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ITEM, \"VALUE\" FROM APPCONFIG");
            while (rs.next()) {
                String Item = rs.getString("ITEM");
                switch (Item) {
                    case "FINGERLIST": {
                        Config.setFinger_List((rs.getString("VALUE").trim()));
                        break;
                    }
                    case "NADRA_STAFF": {
                        String Value = rs.getString("VALUE");
                        String[] Values = Value.split(Pattern.quote("|\\."));//0, 1 and 2 contains respectivly Franchise ID, User Name and Password
                       // System.out.println(Value);
                        Config.setNADRA_Staff_FranchiseID(Values[0].trim());
                        Config.setNADRA_Staff_UserName(Values[1].trim());
                        Config.setNADRA_Staff_Password(Values[2].trim());
                        break;
                    }
                    case "NADRA_CUSTOMER": {
                        String Value = rs.getString("VALUE");
                        String[] Values = Value.split(Pattern.quote("|\\."));//0, 1 and 2 contains respectivly Franchise ID, User Name and Password
                       // System.out.println(Value);
                        Config.setNADRA_Customer_FranchiseID(Values[0].trim());
                        Config.setNADRA_Customer_UserName(Values[1].trim());
                        Config.setNADRA_Customer_Password(Values[2].trim());
                        break;
                    }
                    case "CUSTOMER_INFO_DB":{
                        String Value=rs.getString("VALUE");
                           String[] Values = Value.split(Pattern.quote("|"));
                           Config.setCUSINFO_CONDBINFO(Values[0].trim());
                           Config.setCUSINFO_CONDBUSERNAME(Values[1].trim());
                           Config.setCUSINFO_CONDBPASS(Values[2].trim());
                           
                    }
                }
            }
            con.close();

        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(ex);
            return;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////Retreving Customer Verify Write Info/////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(Config.getDB_Server_Info(), Config.getDB_Server_UserName(), Config.getDB_Server_Password());
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SNAME, SPORT FROM CUS_TSSERVERS");
            String[] IPs = new String[rs.getFetchSize()];
            int[] Ports = new int[rs.getFetchSize()];
            for (int cnt = 0; rs.next(); cnt++) {
                IPs[cnt] = rs.getString("SNAME").trim();
                Ports[cnt] = rs.getInt("SPORT");
            }
            con.close();
            Config.setCustomer_Verify_Write_IP(IPs);
            Config.setCustomer_Verify_Write_Port(Ports);
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(ex);
            return;
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////Retreving Staff Verify Write Info////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(Config.getDB_Server_Info(), Config.getDB_Server_UserName(), Config.getDB_Server_Password());
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SNAME, SPORT FROM TSERVERS");
            String[] IPs = new String[rs.getFetchSize()];
            int[] Ports = new int[rs.getFetchSize()];
            for (int cnt = 0; rs.next(); cnt++) {
                IPs[cnt] = rs.getString("SNAME").trim();
                Ports[cnt] = rs.getInt("SPORT");
            }
            con.close();
            Config.setStaff_Verify_Write_IP(IPs);
            Config.setStaff_Verify_Write_Port(Ports);
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(ex);
            return;
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////Displaying Server Info////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        System.out.println("Server Showing Configration Information");
//        
//        System.out.println("DB Info : " + Config.getDB_Server_Info());
//        System.out.println("DB UserName : " + Config.getDB_Server_UserName());
//        System.out.println("DB Password : " + Config.getDB_Server_Password());
//        
//        System.out.println("NADRA Staff FranchiseID : " + Config.getNADRA_Staff_FranchiseID());
//        System.out.println("NADRA Staff UserName : " + Config.getNADRA_Staff_UserName());
//        System.out.println("NADRA Staff PassWord : " + Config.getNADRA_Staff_Password());
//        
//        System.out.println("NADRA Customer FranchiseID : " + Config.getNADRA_Customer_FranchiseID());
//        System.out.println("NADRA Customer UserName : " + Config.getNADRA_Customer_UserName());
//        System.out.println("NADRA Customer Password : " + Config.getNADRA_Customer_Password());
//        
//        System.out.println("No Staff File Write IPs: " + Config.getStaff_Verify_Write_IP().length);
//        System.out.println("No Staff File Write Ports : " + Config.getStaff_Verify_Write_Port().length);
//        System.out.println("No Customer File Write IPs: " + Config.getCustomer_Verify_Write_IP().length);
//        System.out.println("No Customer File Write Ports : " + Config.getCustomer_Verify_Write_Port().length);

        System.out.println("Server started listening at port: "+serverInfo[0]);
        port = Integer.parseInt(serverInfo[0]);
        new Server(port).run();
    }
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////Comvert data in actual form//////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("restriction")
    public static String decrypt(String encryptedData) throws Exception {
        Key key = new SecretKeySpec("Ib2AbL*&*9w0w78w".getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////This method is here only for back up it is beeing used by BioSecPassGen//////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String encrypt(String data) throws Exception {
        try {
            String Data = data;
            String secKey = "Ib2AbL*&*9w0w78w";
            Key key = new SecretKeySpec(secKey.getBytes(), "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(Data.getBytes());
            String encryptedValue = new BASE64Encoder().encode(encVal);
            return encryptedValue;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            return "";
        }
    }
}
