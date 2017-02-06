package it.crs4.most.demo.spec;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TXTKeyboardCoordinatesParser implements VirtualKeyboard.KeyboardCoordinatesParser {
    private static final String TAG = "TXTKCParser";
    private InputStream istream;
    private Map<String, float[]> keymap;
    private static final Pattern pattern = Pattern.compile(
            "(\\w+)\\s+(\\d+(?:.*\\d+)?)\\s+(\\d+(?:.*\\d+)?)\\s+(\\d+(?:.*\\d+)?)"
    );
    public TXTKeyboardCoordinatesParser(InputStream istream) {
        this.istream = istream;


    }

    public InputStream getIstream() {
        return istream;
    }

    public void setIstream(InputStream istream) {
        this.istream = istream;
    }

    @Override
    public Map<String, float[]> parse() {
        if (keymap != null) {
            return keymap;
        }
        keymap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(istream));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(strLine);
                if (matcher.matches()) {
                    String key = matcher.group(1);
                    float x = Float.parseFloat(matcher.group(2));
                    float y = Float.parseFloat(matcher.group(3));
                    float z = Float.parseFloat(matcher.group(4));
                    keymap.put(key, new float[] {x, y, z});
                }
                else {
                    Log.e(TAG, String.format("skipping line %s, invalid format", strLine));
                }

            }


        } catch (Exception e) {
            e.printStackTrace();

        }
    return keymap   ;
    }
}