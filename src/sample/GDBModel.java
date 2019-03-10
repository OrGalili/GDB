package sample;

import javafx.concurrent.Task;

import java.io.*;
import java.util.ArrayList;

public class GDBModel {
    private ProcessBuilder processBuilder;
    private Process subProcess;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private InputStream outStr= null;
    private OutputStream inpStr = null;
    private boolean inpState = false;
    private StringBuilder output;

    public Process getProcess() {
        return subProcess;
    }

    public ProcessBuilder getProcessBuilder() {
        return processBuilder;
    }

    public GDBModel(){
        processBuilder = new ProcessBuilder("gdb");
        processBuilder.redirectErrorStream(true);
        output = new StringBuilder();
    }

    public void GDBStart(){
        try {
            if (subProcess == null || !subProcess.isAlive())
                startGDB();
        }
        catch (IOException e){

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startGDB() throws IOException, InterruptedException {
        subProcess = processBuilder.start();
        inpStr = subProcess.getOutputStream();  /* Handle to the stdin of process */
        outStr = subProcess.getInputStream();  /* Handle to the stdout of process */
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(inpStr));
        bufferedReader = new BufferedReader(new InputStreamReader(outStr));
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        StdThread readOutPut = new StdThread(bufferedReader,output);

        readOutPut.start();
        String userInput="";

        while(!userInput.toLowerCase().equals("quit")) {
            userInput = bufferRead.readLine();
            if(userInput.toLowerCase().equals("quit"))
                readOutPut.interrupt();


            output.setLength(0);

            bufferedWriter.write(userInput + "\n");
            bufferedWriter.flush();
        }
        subProcess.waitFor();
        subProcess.destroy();
    }

    public void stopGDB(){

    }
}
