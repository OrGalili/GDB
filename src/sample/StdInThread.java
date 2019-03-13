package sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;


public class StdInThread extends Thread{

    private BufferedWriter bufferedWriter;
    private StdOutThread th_readGDB;
    private Process subProcess;

    public StdInThread(BufferedWriter bufferedWriter , StdOutThread th_readGDB , Process subProcess){
        this.bufferedWriter = bufferedWriter;
        this.th_readGDB = th_readGDB;
        this.subProcess = subProcess;
    }

    public void run() {
        String userInput = "";
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (!userInput.toLowerCase().equals("quit")) {
                userInput = bufferRead.readLine();
                if (userInput.toLowerCase().equals("quit"))
                    th_readGDB.interrupt();

                bufferedWriter.write(userInput + "\n");
                bufferedWriter.flush();
            }
            subProcess.waitFor();
            subProcess.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
