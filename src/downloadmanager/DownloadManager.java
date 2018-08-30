/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author gnik
 */
public class DownloadManager {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String args[]) throws IOException, InterruptedException{
        DownloadMetadata downloadfile=new DownloadMetadata("http://download.support.xerox.com/pub/docs/FlowPort2/userdocs/any-os/en/fp_dc_setup_guide.pdf",0);
        //downloadfile=new DownloadMetadata("https://www.sample-videos.com/audio/mp3/crowd-cheering.mp3");
        ConcurrentLinkedQueue queueCommand=new ConcurrentLinkedQueue();
        ConcurrentLinkedQueue queueResponse=new ConcurrentLinkedQueue();
        Download download=new Download(downloadfile,queueCommand,queueResponse);
        Thread th=new Thread(download);
        th.start();
        th.join();
        
    }
    
}
