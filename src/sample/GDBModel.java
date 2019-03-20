package sample;

import java.io.*;

public class GDBModel {
    private ProcessBuilder processBuilder;
    private Process subProcess;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private InputStream outStr= null;
    private OutputStream inpStr = null;
    private boolean inpState = false;
    //private StringBuilder GDBOutput;

    public StdOutThread th_readGDB;
    public StdInThread th_writeGDB;

    public Process getProcess() {
        return subProcess;
    }

    public ProcessBuilder getProcessBuilder() {
        return processBuilder;
    }

    public GDBModel(){
        processBuilder = new ProcessBuilder("gdb");
        processBuilder.redirectErrorStream(true);

        //GDBOutput = new StringBuilder();
    }

    public void GDBStart(){
        try {
            if (subProcess == null || !subProcess.isAlive())
                startGDB();
        }
        catch (IOException e){ }
    }

    private void startGDB() throws IOException {
        subProcess = processBuilder.start();
        inpStr = subProcess.getOutputStream();  /* Handle to the stdin of process */
        outStr = subProcess.getInputStream();   /* Handle to the stdout of process */
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(inpStr));
        bufferedReader = new BufferedReader(new InputStreamReader(outStr));
        getOutPut();

        //th_readGDB = new StdOutThread(bufferedReader,GDBOutput);
        //th_readGDB.start();

        //th_writeGDB = new StdInThread(bufferedWriter,th_readGDB,subProcess);
        //th_writeGDB.start();
    }

    public void stopGDB(){

    }

    public void setInput(String in){
        try {
            bufferedWriter.write(in + "\n");
            bufferedWriter.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOutPut(){
        int ascii;
        char c;
        String GDBOutput = "";
        try {
           // while (true) {
                while (true) {
                    ascii = bufferedReader.read();
                    //if (isInterrupted())
                    //    break;
                    c = (char) ascii;
                    System.out.print(c);
                    GDBOutput+=c;
                    inpState = bufferedReader.ready();
                    if (inpState == false && GDBOutput.indexOf("(gdb)")!=-1)
                        break;
                }
                //if (isInterrupted())
                  //  break;
                //if(GDBOutput.indexOf("(gdb)")!=-1) {
                //    break;
                    //Pseudo code
                    //endOut = out
                    //notify end of specific output
                //}
            //}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return GDBOutput;
    }



}
