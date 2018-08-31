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
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author gnik
 */


public class DownloadPart implements Runnable{
    private final SimpleObjectProperty<DownloadPartMetadata> metadata;
    private final ConcurrentLinkedQueue queueCommand;
    private final ConcurrentLinkedQueue queueResponse;

    public DownloadPart(DownloadPartMetadata metadata,ConcurrentLinkedQueue queueCommand,ConcurrentLinkedQueue queueResponse){
        this.queueCommand=queueCommand;
        this.queueResponse=queueResponse;
        this.metadata=new SimpleObjectProperty<>(metadata);
        
    }
    
    public DownloadPartMetadata getMetadata(){
        return metadata.getValue();
    }
    
    public String toString(){
        return "DownloadPartID:"+getMetadata().partID;
    }
    public DownloadStatus getStatus() {
        return getMetadata().getStatus();
    }

    public void pause(){
        getMetadata().setStatus(DownloadStatus.PAUSED);
    }
    public void resume(){
        getMetadata().setStatus(DownloadStatus.DOWNLOADING);
    }
    public void stop(){
        getMetadata().setStatus(DownloadStatus.STOPPED);
    }
    public String getFilename() {
        return getMetadata().filename;
    }
    
    
    public boolean is_complete(){
        return ((getMetadata().getCompletedBytes()+getMetadata().getPart().getStartByte())==getMetadata().getPart().getEndByte());
    }
    public void download() throws IOException,SocketTimeoutException{
        getMetadata().setStatus(DownloadStatus.DOWNLOADING);
        URLConnection connection=getMetadata().download.getUrl().openConnection();
        connection.setRequestProperty( "Range", "bytes="+String.valueOf(getMetadata().getPart().getStartByte()+getMetadata().getCompletedBytes())+"-"+String.valueOf(getMetadata().getPart().getEndByte()) );
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(getMetadata().download.timeout);
        connection.connect();
        
        BufferedInputStream inputStream=new BufferedInputStream(connection.getInputStream());
        boolean append=(getMetadata().getCompletedBytes()!=0);
        
            BufferedOutputStream fileStream = new BufferedOutputStream(new FileOutputStream(getMetadata().filename,append));
            int byt;
            long completedBytes=getMetadata().getCompletedBytes();
            while( ( byt=inputStream.read() ) != -1)
            {
                fileStream.write(byt);
                completedBytes++;
                getMetadata().setCompletedBytes(completedBytes);
                
                if(!queueCommand.isEmpty()){
                    if (queueCommand.poll().equals("pause")){
                        fileStream.close();
                        pause();
                        queueResponse.add("paused");
                        return;
                    }
                }
            }
        
        
        getMetadata().setStatus(DownloadStatus.COMPLETED);
    }

    public void tryDownload(){
        try {
            download();
        } catch (IOException ex) {
            getMetadata().setStatus(DownloadStatus.ERROR);
            getMetadata().retries++;
            Logger.getLogger(DownloadPart.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    @Override
    public void run() {
        if(DownloadStatus.COMPLETED==getMetadata().getStatus()){return;}
        tryDownload();
        //Infinite loop until the downloadstatus is completed 
        while (getMetadata().getStatus()!=DownloadStatus.COMPLETED){
            //Retry if there is any errors retry.
            if (getMetadata().getStatus()==DownloadStatus.ERROR){
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


