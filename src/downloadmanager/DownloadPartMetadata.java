/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.io.Serializable;

/**
 *
 * @author gnik
 */
public class DownloadPartMetadata implements Serializable{
   public int partID;
   public DownloadStatus status=DownloadStatus.STARTING;
   public final String filename;
   public DownloadMetadata download;
   public Part part;
   public long completedBytes=0;
   public int retries=0;

   public DownloadPartMetadata(DownloadMetadata download,int partID,Part part){
       this.download=download; 
       this.partID=partID;
       this.part=part;
       this.filename=download.filename+".part"+String.valueOf(partID);
   }
   public void setCompletedBytes(long b){
       completedBytes=b;
   }
}
