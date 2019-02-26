package sample;

import java.io.*;
import java.util.ArrayList;


public class TestGDB {
    private InputStream inpStr= null;
    private OutputStream outStr = null;
    private InputStream errStr = null;
    private boolean errorState = false;
    private boolean inpState = false;

    public static void main(String[] args) {
        TestGDB gdb = new TestGDB();
        gdb.test(null);
    }

    public void test(String[] args) {
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("gdb");

        ProcessBuilder pb = new ProcessBuilder(cmd);
        try {
            Process p = pb.start();
            outStr = p.getOutputStream();  /* Handle to the stdin of process */
            inpStr = p.getInputStream();  /* Handle to the stdout of process */
            errStr = p.getErrorStream();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outStr));
            BufferedReader br = new BufferedReader(new InputStreamReader(inpStr));
            BufferedReader brErr = new BufferedReader(new InputStreamReader(errStr));
            int ascii;
            char c;
            String userInput="";

            while(!userInput.toLowerCase().equals("quit")) {
                /*while (true) {
                    ascii = brErr.read();
                    c = (char) ascii;
                    System.out.print(c);
                    errorState = brErr.ready();
                    if (errorState == false) {
                        break;
                    }
                }*/

                while (true) {
                    ascii = br.read();
                    c = (char) ascii;
                    System.out.print(c);
                    inpState = br.ready();
                    if (inpState == false) {
                        break;
                    }
                }

                BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                userInput = bufferRead.readLine();
                bw.write(userInput + "\n");
                bw.flush();
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}