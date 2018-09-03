/* 
 * The MIT License
 *
 * Copyright 2018 gnik.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.downloadmanager;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author gnik
 */
public class DownloadPartMetadata{
   public SimpleObjectProperty<Integer> partID;
   public SimpleObjectProperty<DownloadStatus> status=new SimpleObjectProperty<>(DownloadStatus.STARTING);
   public String filename;
   
   //This field will be included multiple time if it is included
   @XStreamOmitField
   public DownloadMetadata downloadMetadata;
   
   public SimpleObjectProperty<Part> part;
   public SimpleObjectProperty<Long> completedBytes=new SimpleObjectProperty<>(0L);
   public SimpleObjectProperty<Integer> retries=new SimpleObjectProperty<>(0);


    public DownloadPartMetadata(DownloadMetadata downloadMetadata,int partID,Part part){
       this.downloadMetadata=downloadMetadata; 
       this.partID=new SimpleObjectProperty<>(partID);
       this.part=new SimpleObjectProperty<>(part);
       this.filename=downloadMetadata.getFilename()+".part"+String.valueOf(partID);
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
        this.downloadMetadata=downloadMetadata;
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
   
   public SimpleObjectProperty<Long> getCompletedBytesProperty(){
       return completedBytes;
   }
   
   public void setRetries(int r){
       retries.setValue(r);
   }
   
   public int getRetries(){
       return retries.getValue();
   }
   
   public void incrementRetries(){
       retries.setValue(retries.getValue()+1);
   }
   public SimpleObjectProperty<Integer> getRetriesProperty(){
       return retries;
   }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
   
   
}
