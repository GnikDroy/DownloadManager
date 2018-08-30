package downloadmanager;

import java.net.HttpURLConnection;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gnik
 */


public class Download implements Runnable{
    private final DownloadMetadata metadata;
    private final List<DownloadThread> downloadThreads=new ArrayList<>();
    private ConcurrentLinkedQueue queueCommand;
    private ConcurrentLinkedQueue queueResponse;
    
    public Download(DownloadMetadata metadata,ConcurrentLinkedQueue queueCommand,ConcurrentLinkedQueue queueResponse){
        this.metadata=metadata;
        this.queueCommand=queueCommand;
        this.queueResponse=queueResponse;
    }

    public Download(DownloadMetadata metadata,List<DownloadPartMetadata> downloadPartMetadatas,ConcurrentLinkedQueue queueCommand,ConcurrentLinkedQueue queueResponse){
        this.metadata=metadata;
        this.queueCommand=queueCommand;
        this.queueResponse=queueResponse;
        load(downloadPartMetadatas);
    }    
    
    @Override
    public String toString(){
        return "DownloadID:"+metadata.downloadID;
    }
    
    public void get_headers() throws IOException {
          HttpURLConnection conn = null;
          conn = (HttpURLConnection) metadata.url.openConnection();
          conn.setRequestMethod("HEAD");
          metadata.size=conn.getContentLengthLong();
          String ranges=conn.getHeaderField("Accept-Ranges");
          if (!ranges.equals("none")){
              metadata.accelerated=true;
          }
          metadata.status=DownloadStatus.STARTING;
    }
    public void load(List<DownloadPartMetadata> downloadPartMetadatas){
        for (DownloadPartMetadata downloadPartMetadata:downloadPartMetadatas){
            ConcurrentLinkedQueue queueCom=new ConcurrentLinkedQueue();
            ConcurrentLinkedQueue queueRes=new ConcurrentLinkedQueue();
            DownloadPart downloadPart=new DownloadPart(downloadPartMetadata,queueCom,queueRes);
            downloadThreads.add(new DownloadThread(downloadPart,downloadPartMetadata,queueCom,queueRes));
        }
    }
    public void initialize() {
        int partID=0;
        for (Part part:divide_download())
        {
            DownloadPartMetadata part_metadata=new DownloadPartMetadata(metadata,partID,part);
            ConcurrentLinkedQueue queueCom=new ConcurrentLinkedQueue();
            ConcurrentLinkedQueue queueRes=new ConcurrentLinkedQueue();
            DownloadPart downloadPart=new DownloadPart(part_metadata,queueCom,queueRes);
            downloadThreads.add(new DownloadThread(downloadPart,part_metadata,queueCom,queueRes));
            partID++;
        }

    }
    
    private List<Part> divide_download(){
        List<Part> parts=new ArrayList<>();
        long start=0;
        double size=(double) metadata.size/metadata.parts;
        for (int cnt=0;cnt<metadata.parts;cnt++){
            Part part=new Part(start,(int)Math.round(size*(cnt+1)));
            parts.add(part);
            start=(int)Math.round(size*(cnt+1))+1;
            
        }
        return parts;
    }
    
    public DownloadStatus getStatus(){
        return metadata.status;
    }
    
    
    
    public boolean has_downloaded(){
        for(DownloadThread downloadThread:downloadThreads){
            if (downloadThread.downloadPart.getStatus()!=DownloadStatus.COMPLETED){
                return false;
            }
        }
        return true;
    }
    
    public boolean join_threads(){
            boolean alljoined=true;
            for(DownloadThread downloadThread: downloadThreads){
                Thread th=downloadThread.thread;
                if(!th.isAlive()){
                    try {
                    th.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                }
                else{
                    alljoined=false;
                }
            }
        return alljoined;
    }
    
    public void pause(){
        if(metadata.status!=DownloadStatus.DOWNLOADING){return;}
        for (DownloadThread dthread:downloadThreads){
            if(!dthread.thread.isAlive()){return;}
            dthread.queueCommand.add("pause");
            while(true){
                if (!dthread.queueResponse.isEmpty() && dthread.queueResponse.peek().equals("paused")){
                    dthread.queueResponse.poll();
                    break;
                }
            }
        }
        metadata.status=DownloadStatus.PAUSED;
        
    }
    public void resume(){
        if(metadata.status!=DownloadStatus.PAUSED){return;}
        for(DownloadThread dthread:downloadThreads){
            if(!dthread.thread.isAlive()){return;}
            dthread.queueCommand.add("resume");
            while(true){
                if (!dthread.queueResponse.isEmpty() && dthread.queueResponse.peek().equals("resumed")){
                    dthread.queueResponse.poll();
                    break;
                }
            }
        }
        metadata.status=DownloadStatus.DOWNLOADING;
    }
    
    public void stop(){
        if(metadata.status!=DownloadStatus.PAUSED){return;}
        for (DownloadThread dthread:downloadThreads){
            if(!dthread.thread.isAlive()){return;}
            dthread.queueCommand.add("stop");
            while(true){
                if (!dthread.queueResponse.isEmpty() && dthread.queueResponse.peek().equals("stopped")){
                    dthread.queueResponse.poll();
                    break;
                }
            }
        }
        metadata.status=DownloadStatus.STOPPED;    
    }
    
    public void accelerated_download() {
        if (!metadata.accelerated){return;}
        if(downloadThreads.isEmpty()){
            initialize();
        }
        metadata.status=DownloadStatus.DOWNLOADING;
        for (DownloadThread downloadThread : downloadThreads){
            Thread thread=new Thread(downloadThread.downloadPart);
            thread.setName(this.toString()+" "+downloadThread.downloadPart.toString());
            downloadThread.thread=thread;
            thread.start();    
        }
    }
    
    public void join_parts(){
        if(!has_downloaded()){return;}
        metadata.status=DownloadStatus.JOINING;
        BufferedOutputStream outFile=null;
        try{
            outFile=new BufferedOutputStream( new FileOutputStream(metadata.filename) );
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            for (DownloadThread downloadThread:downloadThreads){
                DownloadPart downloadPart=downloadThread.downloadPart;
                BufferedInputStream inFile=new BufferedInputStream(new FileInputStream(downloadPart.getFilename()));
                int byt;
                while(( byt=inFile.read() )!=-1 && outFile!=null){
                    outFile.write(byt);
                }
                inFile.close();
            }
            if(outFile!=null){
                outFile.close();
            }
            for (DownloadThread downloadThread: downloadThreads){
                DownloadPart downloadPart=downloadThread.downloadPart;
                Files.deleteIfExists(Paths.get(downloadPart.getFilename()));
            }
            metadata.status=DownloadStatus.COMPLETED;
                
        } catch (FileNotFoundException ex) {
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }

    @Override
    public void run() {
        try {
            this.get_headers();
        } catch (IOException ex) {
            Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
            this.metadata.status=DownloadStatus.ERROR;
        }
        this.accelerated_download();
        while (!has_downloaded()){
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                this.metadata.status=DownloadStatus.ERROR;
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(!this.queueCommand.isEmpty()){
                String command=(String)this.queueCommand.poll();
                switch (command) {
                    case "pause":
                        this.pause();
                        this.queueResponse.add("paused");
                        break;
                    case "stop":
                        this.stop();
                        this.join_threads();
                        this.queueResponse.add("stopped");
                        return;
                    case "resume":
                        this.resume();
                        this.queueResponse.add("resumed");
                        break;
                    default:
                        break;
                }
            }
        }
        this.join_threads();
        this.join_parts();
    }

}


class DownloadThread{
    public Thread thread;
    public DownloadPart downloadPart;
    public ConcurrentLinkedQueue queueCommand;
    public ConcurrentLinkedQueue queueResponse;
    public DownloadPartMetadata downloadPartMetadata;
    public DownloadThread(DownloadPart downloadPart,DownloadPartMetadata downloadPartMetadata,Thread thread,ConcurrentLinkedQueue queueCommand,ConcurrentLinkedQueue queueResponse){
        this.thread=thread;
        this.downloadPart=downloadPart;
        this.downloadPartMetadata=downloadPartMetadata;
        this.queueCommand=queueCommand;
        this.queueResponse=queueResponse;
    }
    public DownloadThread(DownloadPart downloadPart,DownloadPartMetadata downloadPartMetadata,ConcurrentLinkedQueue queueCommand,ConcurrentLinkedQueue queueResponse){
        this.downloadPart=downloadPart;
        this.downloadPartMetadata=downloadPartMetadata;
        this.queueCommand=queueCommand;
        this.queueResponse=queueResponse;
    }
}
