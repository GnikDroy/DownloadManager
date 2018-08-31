/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author gnik
 */
public class DownloadPartMetadata{
   public SimpleObjectProperty<Integer> partID;
   public SimpleObjectProperty<DownloadStatus> status=new SimpleObjectProperty<>(DownloadStatus.STARTING);
   public final String filename;
   
   @XStreamOmitField
   public DownloadMetadata download;
   
   public SimpleObjectProperty<Part> part;
   public SimpleObjectProperty<Long> completedBytes=new SimpleObjectProperty<>(0L);
   public int retries=0;

   public DownloadPartMetadata(DownloadMetadata download,int partID,Part part){
       this.download=download; 
       this.partID=new SimpleObjectProperty<>(partID);
       this.part=new SimpleObjectProperty<>(part);
       this.filename=download.getFilename()+".part"+String.valueOf(partID);
   }

    public Part getPart(){
        return part.getValue();
    }
    public void setPart(Part p){
        part.setValue(p);
    }
    public SimpleObjectProperty<Part> getPartProperty(){
        return part;
    }
    
    public void setDownloadMetadata(DownloadMetadata downloadMetadata){
        download=downloadMetadata;
    }
    
    public SimpleObjectProperty<DownloadStatus> getStatusProperty() {
        return status;
    }
    public DownloadStatus getStatus(){
        return status.getValue();
    }
    public void setStatus(DownloadStatus s) {
        status.setValue(s);
    }
   
   public void setCompletedBytes(long b){
       completedBytes.setValue(b);
   }
      
   public long getCompletedBytes(){
       return completedBytes.getValue();
   }
}
