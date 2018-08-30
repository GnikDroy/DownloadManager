/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

/**
 *
 * @author gnik
 */
public class DownloadMetadata {
    public URL url;
    public String downloadPath="";
    public String filename;
    public int parts=8;
    public long size;
    public int timeout;
    public boolean accelerated=false;
    public DownloadStatus status=DownloadStatus.NEW;
    public DownloadMetadata(String url) throws MalformedURLException{
        this.url=new URL(url);
        this.filename=Paths.get(this.url.getPath()).getFileName().toString();
        
    }
}
