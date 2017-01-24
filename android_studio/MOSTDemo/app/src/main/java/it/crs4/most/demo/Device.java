package it.crs4.most.demo;


import android.os.Build;

public class Device {
    public static String getModel(){
        return "";
    }

    public static boolean isEyeWear(){
        return((Build.MANUFACTURER.equals("EPSON") && Build.MODEL.equals("embt2")));
    }
}
