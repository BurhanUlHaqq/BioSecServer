package com.src.server.actions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class SideWork {
    /*15*/ public static synchronized String addDot(String uid) {
        try{
        String suid = null;
        // System.out.println("\n\ncoming uid: " + uid);
        int i = 0;
        while (!Character.isDigit(uid.charAt(i)) && (i < (uid.length() - 1))) {
            i++;
        }
        //   System.out.println(i);
        suid = uid.substring(0, i) + "." + uid.substring(i, uid.length());
        //   System.out.println("\n\nreturning uid: " + suid);
        return suid;
        }catch(Exception ex)
        {
            System.out.println("addDot: "+ex);
            return "";
        }
    }
public static String extractEIN(String uid)
{
   try{
        String sUID = null;
        // System.out.println("\n\ncoming uid: " + uid);
        int i = 0;
        while (!Character.isDigit(uid.charAt(i)) && (i < (uid.length() - 1))) {
            i++;
        }
        //   System.out.println(i);
        sUID =uid.substring(i, uid.length());
        //   System.out.println("\n\nreturning uid: " + suid);
        return sUID;
        }catch(Exception ex)
        {
            System.out.println("extractEIN: "+ex);
            return "";
        } 
}
    public static long getDiffDays(Date first) {
           Date last = new Date();
      Calendar calendar1 = Calendar.getInstance();
      Calendar calendar2 = Calendar.getInstance();
      calendar1.setTime(first);
      calendar2.setTime(last);
      long milsecs1= calendar1.getTimeInMillis();
      long milsecs2 = calendar2.getTimeInMillis();
      long diff = milsecs2 - milsecs1;
      long ddays = diff / (24 * 60 * 60 * 1000);
         // System.out.println("Your Day Difference="+ddays);
      return ddays;
    }
    
 public static String addLeadingZero(String Ip)
    {
        String[] sIP=new String[4];
        sIP=Ip.split(Pattern.quote("."));
        int no[] = new int[4];
        no[0]=Integer.parseInt(sIP[0]);
        no[1]=Integer.parseInt(sIP[1]);
        no[2]=Integer.parseInt(sIP[2]);
        no[3]=Integer.parseInt(sIP[3]);
        sIP[0]=String.format("%03d", no[0]);
        sIP[1]=String.format("%03d", no[1]);
        sIP[2]=String.format("%03d", no[2]);
        sIP[3]=String.format("%03d", no[3]);
        return sIP[0]+"."+sIP[1]+"."+sIP[2]+"."+sIP[3];
    }
 public static String addLeadingZeroToBCode(String code)
    {
        
        
       int intCode=Integer.parseInt(code);
        String modifiedCode=String.format("%04d", intCode);
        return modifiedCode;
    }
 public static String formateDOB(String birthDate)
{
                String DOB="";
                Calendar calendar = Calendar.getInstance();
                String BirthDate[]=birthDate.split(Pattern.quote("-"));
                if(BirthDate[0].equalsIgnoreCase("0") && BirthDate[1].equalsIgnoreCase("0")){
                    DOB=BirthDate[2];
                }
                else{
                calendar.set(Calendar.YEAR, Integer.parseInt(BirthDate[2]));
                calendar.set(Calendar.MONTH,  Integer.parseInt(BirthDate[1])-1);
                calendar.set(Calendar.DATE,  Integer.parseInt(BirthDate[0]));
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                 DOB = df.format(calendar.getTime());
                }
               return DOB;
}
public static String formateExpDate(String expDate){
                String ExpiryDate="";
                Calendar calendar = Calendar.getInstance();
                if(expDate.equalsIgnoreCase("Lifetime")){
                calendar.set(Calendar.YEAR, Integer.parseInt("2099"));
                calendar.set(Calendar.MONTH,  Integer.parseInt("12")-1);
                calendar.set(Calendar.DATE,  Integer.parseInt("31"));
                SimpleDateFormat df1 = new SimpleDateFormat("dd-MMM-yyyy");
                 ExpiryDate = df1.format(calendar.getTime());
                }
                else{
                String EXpDate[]=expDate.split(Pattern.quote("-"));
                calendar.set(Calendar.YEAR, Integer.parseInt(EXpDate[0]));
                calendar.set(Calendar.MONTH,  Integer.parseInt(EXpDate[1])-1);
                calendar.set(Calendar.DATE,  Integer.parseInt(EXpDate[2]));
                SimpleDateFormat df1 = new SimpleDateFormat("dd-MMM-yyyy");
                 ExpiryDate = df1.format(calendar.getTime());
            }
                return ExpiryDate;
}
public static String formateCardType(String cardType){
                   String type="";
                if (cardType.equalsIgnoreCase("idcard"))
                   type="CNIC";
                else if (  cardType.equalsIgnoreCase("smartid"))
                  type="SMART NIC";
                else if (cardType.equalsIgnoreCase("nicop"))
                   type="NICOP";
                else if (  cardType.equalsIgnoreCase("poc"))
                    type="PAKISTAN ORIGIN CARD";
                else
                    type=cardType;
                 return type;
} 
}
