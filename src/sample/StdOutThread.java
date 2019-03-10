package sample;

import java.io.BufferedReader;
import java.io.IOException;

public class StdOutThread extends Thread {
    private int ascii;
    private char c;
    private static boolean inpState;
    private BufferedReader bufferedReader;
    private StringBuilder out;

    public StdOutThread(BufferedReader br, StringBuilder out) {
        bufferedReader = br;
        this.out = out;
    }

    public void run() {
        try {
            while (true) {
                while (true) {
                    ascii = bufferedReader.read();
                    if (isInterrupted())
                        break;
                    c = (char) ascii;
                    System.out.print(c);
                    out.append(c);
                    inpState = bufferedReader.ready();
                    if (inpState == false)
                        break;
                }
                if (isInterrupted())
                    break;
                //if(out.contains("(gdb)")) {
                    //Pseudo code
                    //endOut = out
                    //notify end of specific output
                //}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
