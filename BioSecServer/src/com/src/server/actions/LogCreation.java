
package com.src.server.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogCreation {
    /*16*/ public static synchronized void writeLog(String str, File file) {
        try {
            //System.err.println("in write log");
            FileWriter fileWritter = new FileWriter(file, true);
               BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(str);
            bufferWritter.flush();
            closeLog(bufferWritter);
        } catch (IOException ex) {
           // Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
        }

    }
   
    
    /*17*/ private static synchronized void closeLog(BufferedWriter bufferedwriter) {
        try {
            bufferedwriter.close();
        } catch (IOException ex) {
          //  Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
        }

    }

}
