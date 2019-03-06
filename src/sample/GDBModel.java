package sample;

import javafx.concurrent.Task;

import java.io.*;
import java.util.ArrayList;

public class GDBModel {
    private ProcessBuilder pb;
    private Process p;
    private BufferedWriter bw;
    private BufferedReader br;
    private InputStream inpStr= null;
    private OutputStream outStr = null;
    private boolean inpState = false;
    private String output ="";

    public Process getProcess() {
        return p;
    }

    public ProcessBuilder getProcessBuilder() {
        return pb;
    }



    public GDBModel(){
        pb = new ProcessBuilder("gdb");
        pb.redirectErrorStream(true);
    }

    public void GDBStart(){
        try {
            if (p == null || !p.isAlive())
                startGDB();
        }
        catch (IOException e){

        }
    }

    private void startGDB() throws IOException{
        p = pb.start();
        outStr = p.getOutputStream();  /* Handle to the stdin of process */
        inpStr = p.getInputStream();  /* Handle to the stdout of process */
        bw = new BufferedWriter(new OutputStreamWriter(outStr));
        br = new BufferedReader(new InputStreamReader(inpStr));
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        StdThread readOutPut = new StdThread(br,output);
        readOutPut.start();

        //int ascii;
        //char c;
        String userInput="";

        while(!userInput.toLowerCase().equals("quit")) {

            /*while (true) {
                ascii = br.read();
                c = (char) ascii;
                System.out.print(c);
                inpState = br.ready();
                if (inpState == false) {
                    break;
                }
            }*/


            userInput = bufferRead.readLine();
            if(userInput.toLowerCase().equals("quit")){
                readOutPut.interrupt();
                //p.destroy();
            }
            bw.write(userInput + "\n");
            //userInput.clear();
            bw.flush();
        }

    }

    public void stopGDB(){

    }
}
