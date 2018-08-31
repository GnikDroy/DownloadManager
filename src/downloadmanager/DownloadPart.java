/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.io.IOException;
import java.net.URLConnection;
import java.io.*; 
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gnik
 */


public class DownloadPart implements Runnable{
    private final DownloadPartMetadata metadata;
    private ConcurrentLinkedQueue queueCommand;
    private ConcurrentLinkedQueue queueResponse;

    public DownloadPart(DownloadPartMetadata metadata,ConcurrentLinkedQueue queueCommand,ConcurrentLinkedQueue queueResponse){
        this.queueCommand=queueCommand;
        this.queueResponse=queueResponse;
        this.metadata=metadata;
        
    }
    public String toString(){
        return "DownloadPartID:"+metadata.partID;
    }
    public DownloadStatus getStatus() {
        return metadata.status;
    }

    public void pause(){
        metadata.status=DownloadStatus.PAUSED;
    }
    public void resume(){
        metadata.status=DownloadStatus.DOWNLOADING;
    }
    public void stop(){
        metadata.status=DownloadStatus.STOPPED;
    }
    public String getFilename() {
        return metadata.filename;
    }
    
    
    public boolean is_complete(){
        return ((metadata.completedBytes+metadata.part.getStartByte())==metadata.part.getEndByte());
    }
    public void download() throws IOException,SocketTimeoutException{
        metadata.status=DownloadStatus.DOWNLOADING;
        URLConnection connection=metadata.download.url.openConnection();
        connection.setRequestProperty( "Range", "bytes="+String.valueOf(metadata.part.getStartByte()+metadata.completedBytes)+"-"+String.valueOf(metadata.part.getEndByte()) );
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(metadata.download.timeout);
        connection.connect();
        
        BufferedInputStream inputStream=new BufferedInputStream(connection.getInputStream());
        boolean append=(metadata.completedBytes!=0);
        
            BufferedOutputStream fileStream = new BufferedOutputStream(new FileOutputStream(metadata.filename,append));
            int byt;
            long completedBytes=metadata.completedBytes;
            while( ( byt=inputStream.read() ) != -1)
            {
                fileStream.write(byt);
                completedBytes++;
                metadata.setCompletedBytes(completedBytes);
                
                if(!queueCommand.isEmpty()){
                    if (queueCommand.poll().equals("pause")){
                        fileStream.close();
                        pause();
                        queueResponse.add("paused");
                        return;
                    }
                }
            }
        
        
        metadata.status=DownloadStatus.COMPLETED;
    }

    public void tryDownload(){
        try {
            download();
        } catch (IOException ex) {
            metadata.status=DownloadStatus.ERROR;
            metadata.retries++;
            Logger.getLogger(DownloadPart.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    @Override
    public void run() {
        if(DownloadStatus.COMPLETED==metadata.status){return;}
        tryDownload();
        //Infinite loop until the downloadstatus is completed 
        while (metadata.status!=DownloadStatus.COMPLETED){
            //Retry if there is any errors retry.
            if (metadata.status==DownloadStatus.ERROR){
                tryDownload();
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloadPart.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(!queueCommand.isEmpty()){
                String command=(String)queueCommand.poll();
                if(command.equals("stop")){
                    stop();
                    queueResponse.add("stopped");
                    return;
                
                }
                else if(command.equals("resume")){
                    resume();
                    queueResponse.add("resumed");
                    tryDownload();
                }
            }
        }
    }
}


