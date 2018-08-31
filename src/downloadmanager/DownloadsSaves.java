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
