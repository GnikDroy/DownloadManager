/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        DownloadMetadata downloadfile=new DownloadMetadata("http://download.support.xerox.com/pub/docs/FlowPort2/userdocs/any-os/en/fp_dc_setup_guide.pdf");
  
        Download d=new Download(downloadfile);
        d.initialize();
        System.out.println("downloading");
        d.accelerated_download();
        Thread.sleep(20000);
        System.out.println(d.join_threads());
        d.join_parts();
    }
    
}
