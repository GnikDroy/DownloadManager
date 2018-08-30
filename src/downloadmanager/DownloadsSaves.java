/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gnik
 */
public class DownloadsSaves implements Serializable {
    
    public List<Map<DownloadMetadata,List<DownloadPartMetadata>>> downloads=new ArrayList<>();
    
    public DownloadsSaves(){
        
    }
    public void addDownload(Map<DownloadMetadata,List<DownloadPartMetadata>> download){
        downloads.add(download);
    }
}
