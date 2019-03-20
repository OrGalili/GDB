package sample;

import java.io.BufferedReader;
import java.io.IOException;

public class StdOutThread extends Thread {
    private int ascii;
    private char c;
    private static boolean inpState;
    private BufferedReader bufferedReader;
    private StringBuilder GDBOutput;

    public StdOutThread(BufferedReader br, StringBuilder GDBOutput) {
        bufferedReader = br;
        this.GDBOutput = GDBOutput;
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
                    GDBOutput.append(c);
                    inpState = bufferedReader.ready();
                    if (inpState == false)
                        break;
                }
                if (isInterrupted())
                    break;
                if(GDBOutput.indexOf("(gdb)")!=-1) {
                    getOutPut();
                    GDBOutput.setLength(0);
                    //Pseudo code
                    //endOut = out
                    //notify end of specific output
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOutPut(){
        return GDBOutput.toString();
    }
}
