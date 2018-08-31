/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author gnik
 */
public class DownloadMetadata{
    private SimpleObjectProperty<URL> url;
    private SimpleObjectProperty<Integer> downloadID;
    private SimpleObjectProperty<String> filename;
    public int parts=8;
    private SimpleObjectProperty<Long> size=new SimpleObjectProperty<>();
    public int timeout=10000;
    private SimpleObjectProperty<Boolean> accelerated=new SimpleObjectProperty<>(false);
    private SimpleObjectProperty<DownloadStatus> status=new SimpleObjectProperty<>(DownloadStatus.NEW);
    
    public DownloadMetadata(String url,int ID) throws MalformedURLException{
        this.url=new SimpleObjectProperty<>(new URL(url));
        this.downloadID=new SimpleObjectProperty(ID);
        String file=String.valueOf(ID)+"_"+Paths.get(this.getUrl().getPath()).getFileName().toString();
        if (file==null){file=String.valueOf(ID);}
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
}
