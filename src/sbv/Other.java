package sbv;

import java.text.SimpleDateFormat;

public class Other {

    public static String ToNormal(String timestamp) {
        try {
            String s;
            long stuff = Long.parseLong(timestamp);
            java.util.Date time = new java.util.Date((long) stuff * 1000);
            s = new SimpleDateFormat("MM/dd/yyyy").format(time);
            return s;
        } catch (Exception e) {
            System.out.println(e + " => date");
        }
        return timestamp;
    }
    
    /* unused
    
    public static String addToLength(String in, int length, boolean davor, int spaceBehind) {
        do {
            if (davor) {
                in = " ".concat(in); //ERROR bei random Barcode (vielleicht zu lang)
            } else {
                in = in.concat(" ");
            }
        } while (in.length() < length);

        for (int i = 0; i < spaceBehind; i++) {
            in = in.concat(" ");
        }

        return in;
    }*/

}
