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

    public GDBModel(){
        processBuilder = new ProcessBuilder("gdb");
        processBuilder.redirectErrorStream(true);
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
        outStr = subProcess.getInputStream();   /* Handle to the stdout of process*/
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(inpStr));
        bufferedReader = new BufferedReader(new InputStreamReader(outStr));
        getOutPut();
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

    public String getOutPut() {
        int ascii;
        char c;
        String GDBOutput = "";
        try {
            Thread.sleep(15);//15
            while (true) {
                //
               /* inpState = bufferedReader.ready();
                if (inpState == false)
                    break;*/
                //
                ascii = bufferedReader.read();
                c = (char) ascii;
                System.out.print(c);
                GDBOutput += c;
                inpState = bufferedReader.ready();
//              if (inpState == false && GDBOutput.endsWith("(gdb) "))
                if (inpState == false)
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return GDBOutput;
    }
}
