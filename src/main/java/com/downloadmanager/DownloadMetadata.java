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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author gnik
 */
public class DownloadMetadata{
    private final SimpleObjectProperty<URL> url;
    private final SimpleObjectProperty<Integer> downloadID;
    private final SimpleObjectProperty<String> filename;
    private static final int parts=8;
    private final SimpleObjectProperty<Long> size=new SimpleObjectProperty<>();
    private static final int timeout=10000;
    private final SimpleObjectProperty<Boolean> accelerated=new SimpleObjectProperty<>(false);
    private final SimpleObjectProperty<DownloadStatus> status=new SimpleObjectProperty<>(DownloadStatus.NEW);
    
    public DownloadMetadata(String url,int ID) throws MalformedURLException{
        this.url=new SimpleObjectProperty<>(new URL(url));
        this.downloadID=new SimpleObjectProperty(ID);
        String file=String.valueOf(ID)+"_"+Paths.get(this.url.getValue().getPath()).getFileName().toString();
        this.filename=new SimpleObjectProperty<>(file);
    }

    public URL getUrl() {
        return url.getValue();
    }
    
    public SimpleObjectProperty getUrlProperty() {
        return url;
    }

    public Integer getDownloadID() {
        return downloadID.getValue();
    }
    
    
    public SimpleObjectProperty<Integer> getDownloadIDProperty() {
        return downloadID;
    }
    
    public String getFilename() {
        return filename.getValue();
    }
    public SimpleObjectProperty getFilenameProperty() {
        return filename;
    }

    public long getSize() {
        return size.getValue();
    }
    
    public SimpleObjectProperty getSizeProperty() {
        return size;
    }
    
    public void setSize(long s){
        size.setValue(s);
    }
    
    public DownloadStatus getStatus() {
        return status.getValue();
    }

    public SimpleObjectProperty getStatusProperty() {
        return status;
    }

    public void setStatus(DownloadStatus status) {
       this.status.setValue(status);
    }
    
    public boolean getAccelerated(){
        return accelerated.getValue();
    }
    
    public SimpleObjectProperty getAcceleratedProperty(){
        return accelerated;
    }
    
    public void setAccelerated(boolean a){
        accelerated.setValue(a);
    }
    public int getTimeout(){
        return timeout;
    }
    public int getParts(){
        return parts;
    }
}
