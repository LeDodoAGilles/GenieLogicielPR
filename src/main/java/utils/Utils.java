package utils;

public class Utils {

    public static String[] stringSplitter(String s,int nb){
        int sl = s.length()/nb;
        int remain = s.length() % nb;
        String[] r = new String[nb];
        int deb=0,fin;
        for (int k=0;k<nb;k++){
            if (remain>0) {
                remain--;
                fin = deb+sl+1;
            }
            else
                fin = deb+sl;
            r[k]=s.substring(deb,Math.min(fin+((k==nb-1)?remain:0),s.length()));

            deb=fin;
        }
        return r;
    }
}
