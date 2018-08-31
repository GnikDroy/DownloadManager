/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
/**
 *
 * @author gnik
 */
public class DownloadPool {
    private final ObservableList<DownloadThread> downloadThreads=FXCollections.observableArrayList();
 
    public DownloadPool(){
        
    }
    
    public DownloadPool(DownloadsSaves saves){
        load(saves);
    }
    
    public void load(DownloadsSaves saves){
        for(Map<DownloadMetadata,List<DownloadPartMetadata>> save:saves.downloads)
        {
            for(DownloadMetadata downloadMetadata:save.keySet()){
                ConcurrentLinkedQueue queueCommand=new ConcurrentLinkedQueue();
                ConcurrentLinkedQueue queueResponse=new ConcurrentLinkedQueue();
                Download download=new Download(downloadMetadata,queueCommand,queueResponse);
                download.load(save.get(downloadMetadata));
                Thread thread=new Thread(download);
                DownloadThread downloadThread=new DownloadThread(downloadMetadata,download,thread,queueCommand,queueResponse);
                downloadThreads.add(downloadThread);
                thread.start();
        
            }
        }
    }
    
    public boolean isValidUrl(String url){
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException ex) {
           return false;
        }
    }
    
    public ObservableList<DownloadThread> getDownloadThreads(){
        return downloadThreads;
    }
    

    public DownloadThread getDownloadThreadObj(int downloadID){
        for(DownloadThread dthread:downloadThreads){
            if (dthread.download.getValue().get_metadata().getDownloadID()==downloadID){
                return dthread;
            }
        }
        return null;
    }
    
    public void stopDownload(DownloadThread downloadThread){
        if (!downloadThread.thread.isAlive()){return;}
        downloadThread.queueCommand.add("stop");
        while(downloadThread.queueResponse.peek()!="stopped"){
        }
        downloadThread.queueResponse.poll();
    }
    
    public void resumeDownload(DownloadThread downloadThread){
        if (!downloadThread.thread.isAlive()){return;}
        downloadThread.queueCommand.add("resume");
        while(downloadThread.queueResponse.peek()!="resumed"){
        }
        downloadThread.queueResponse.poll();
    }
    
    public void pauseDownload(DownloadThread downloadThread){
        if (!downloadThread.thread.isAlive()){return;}
        downloadThread.queueCommand.add("pause");
        while(downloadThread.queueResponse.peek()!="paused"){
        }
        downloadThread.queueResponse.poll();
    }
 
    public void pauseAll(){
        for(DownloadThread downloadThread:downloadThreads){
            pauseDownload(downloadThread);
        }
    }

    public void resumeAll(){
        for(DownloadThread downloadThread:downloadThreads){
            resumeDownload(downloadThread);
        }
    }
    
    public void stopAll(){
        for(DownloadThread downloadThread:downloadThreads){
            stopDownload(downloadThread);
        }
    }
    public void joinThreads(){
        for(DownloadThread downloadThread:downloadThreads){
            try {
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
        thread.start();
        
    } 
  
    
}

class DownloadThread{
    public SimpleObjectProperty<DownloadMetadata> downloadMetadata;
    public SimpleObjectProperty<Download> download;
    public Thread thread;
    public ConcurrentLinkedQueue queueCommand;
    public ConcurrentLinkedQueue queueResponse;

    public DownloadThread(DownloadMetadata downloadMetadata, Download download, Thread thread, ConcurrentLinkedQueue queueCommand, ConcurrentLinkedQueue queueResponse) {
        this.downloadMetadata = new SimpleObjectProperty<>(downloadMetadata);
        this.download = new SimpleObjectProperty<>(download);
        this.thread = thread;
        this.queueCommand = queueCommand;
        this.queueResponse = queueResponse;
    }
    public DownloadMetadata getDownloadMetadata(){
        return downloadMetadata.getValue();
    }
}