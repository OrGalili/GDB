package sample;

import java.io.*;
import java.util.ArrayList;


public class TestGDB {
    private InputStream inpStr= null;
    private OutputStream outStr = null;
    private boolean inpState = false;

    public void test() {
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("gdb");


        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            outStr = p.getOutputStream();  /* Handle to the stdin of process */
            inpStr = p.getInputStream();  /* Handle to the stdout of process */

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outStr));
            BufferedReader br = new BufferedReader(new InputStreamReader(inpStr));
            int ascii;
            char c;
            String userInput="";
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

            while(!userInput.toLowerCase().equals("quit")) {
                while (true) {
                    ascii = br.read();
                    c = (char) ascii;
                    System.out.print(c);
                    inpState = br.ready();
                    if (inpState == false) {
                        break;
                    }
                }

                userInput = bufferRead.readLine();
                bw.write(userInput + "\n");
                bw.flush();
            }
            System.exit(0);

        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

