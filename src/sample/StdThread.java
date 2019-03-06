package sample;

import java.io.BufferedReader;
import java.io.IOException;

public class StdThread extends Thread {
    private int ascii;
    private char c ;
    private static boolean inpState;
    private BufferedReader br;
    private String out;

    public StdThread(BufferedReader br , String out){
        this.br = br;
        this.out = out;
    }

    public void run() {
        try {
            //std();
            while (true) {
                ascii = br.read();
                c = (char) ascii;
                System.out.print(c);
                out += c;
                inpState = br.ready();
                // if (inpState == false) {
                //   break;
                if(isInterrupted())
                    break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void std() throws Exception {
//        while (true) {
//            ascii = br.read();
//            c = (char) ascii;
//            System.out.print(c);
//            out += c;
//            inpState = br.ready();
//            // if (inpState == false) {
//            //   break;
//            //}
//        }
    }
}
