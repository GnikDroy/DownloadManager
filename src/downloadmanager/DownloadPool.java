/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author gnik
 */
public class DownloadPool {
    private List<DownloadThread> downloadThreads=new ArrayList<>();
 
    public boolean isValidUrl(String url){
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException ex) {
           return false;
        }
    }
    public void joinThreads(){
        for(DownloadThread downloadThread:downloadThreads){
            try {
                System.out.println("threadjoined");
                downloadThread.thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloadPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void newDownload(String url){
        DownloadMetadata downloadMetadata;
        try {
            downloadMetadata = new DownloadMetadata(url,downloadThreads.size());
        } catch (MalformedURLException ex) {
            Logger.getLogger(DownloadManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        ConcurrentLinkedQueue queueCommand=new ConcurrentLinkedQueue();
        ConcurrentLinkedQueue queueResponse=new ConcurrentLinkedQueue();
        Download download=new Download(downloadMetadata,queueCommand,queueResponse);
        Thread thread=new Thread(download);
        DownloadThread downloadThread=new DownloadThread(downloadMetadata,download,thread,queueCommand,queueResponse);
        downloadThreads.add(downloadThread);
        thread.run();
        System.out.println("thread run");
        
    } 
  
    
}

class DownloadThread{
    public DownloadMetadata downloadMetadata;
    public Download download;
    public Thread thread;
    public ConcurrentLinkedQueue queueCommand;
    public ConcurrentLinkedQueue queueResponse;

    public DownloadThread(DownloadMetadata downloadMetadata, Download download, Thread thread, ConcurrentLinkedQueue queueCommand, ConcurrentLinkedQueue queueResponse) {
        this.downloadMetadata = downloadMetadata;
        this.download = download;
        this.thread = thread;
        this.queueCommand = queueCommand;
        this.queueResponse = queueResponse;
    }



}