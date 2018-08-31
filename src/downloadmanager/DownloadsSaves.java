/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gnik
 */
public class DownloadsSaves {

    private List<DownloadState> downloads = new ArrayList<>();
    private final String saveFilename = "history.dat";

    public DownloadsSaves() {

    }

    public void addDownload(DownloadState download) {
        downloads.add(download);
    }

    public List<DownloadState> getDownloads() {
        return downloads;
    }

    public void clear() {
        downloads = new ArrayList<>();

    }

    public void save() {
        XStream xstream = new XStream(new StaxDriver());
        String object = xstream.toXML(downloads);

        FileWriter file = null;
        try {
            file = new FileWriter(saveFilename);
        } catch (IOException ex) {
            Logger.getLogger(DownloadsSaves.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if (file != null) {
                file.write(object);
                file.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(DownloadsSaves.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void load() {
        FileReader reader = null;
        try {
            reader = new FileReader(saveFilename);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DownloadsSaves.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        XStream xstream = new XStream(new StaxDriver());
        downloads = (List<DownloadState>) xstream.fromXML(reader);
    }
}
