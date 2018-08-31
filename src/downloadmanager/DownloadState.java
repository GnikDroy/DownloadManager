/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gnik
 */
public class DownloadState {
    public DownloadMetadata downloadMetadata;
    public List<DownloadPartMetadata> downloadPartMetadata;
    public DownloadState(){
        
    }
    public DownloadState(DownloadMetadata downloadMetadata, List<DownloadPartMetadata> downloadPartMetadata) {
        this.downloadMetadata = downloadMetadata;
        this.downloadPartMetadata = downloadPartMetadata;
    }

        
}
