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
    private StringBuilder GDBOutput;

    public Process getProcess() {
        return subProcess;
    }

    public ProcessBuilder getProcessBuilder() {
        return processBuilder;
    }

    public GDBModel(){
        processBuilder = new ProcessBuilder("gdb");
        processBuilder.redirectErrorStream(true);
        GDBOutput = new StringBuilder();
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

        StdOutThread th_readGDB = new StdOutThread(bufferedReader,GDBOutput);
        th_readGDB.start();

        StdInThread th_writeGDB = new StdInThread(bufferedWriter,th_readGDB,subProcess);
        th_writeGDB.start();
    }

    public void stopGDB(){

    }
}
