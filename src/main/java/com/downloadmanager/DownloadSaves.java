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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This object contains methods to load and save downloads when the program execution stops.
 * @author gnik
 */
public class DownloadSaves {

    private List<DownloadState> downloads = new ArrayList<>();
    private final String saveFilename = "history.dat";
    private final static String DEFAULT_PATH = "./";
    private final String path;

    public DownloadSaves() {
        this(DEFAULT_PATH);
    }

    protected DownloadSaves(String path) {
        this.path = path;
    }

    /**
     * Adds a download to the list to save.
     * @param download DownloadState object which represents the state of download.
     */
    public void addDownload(DownloadState download) {
        downloads.add(download);
    }

    /**
     * Returns the list of DownloadState objects.
     * @return All the downloads.
     */
    public List<DownloadState> getDownloads() {
        return downloads;
    }

    /**
     * Clears the list of downloads.
     */
    public void clear() {
        downloads = new ArrayList<>();

    }

    /**
     * Saves the current list of downloads to the disk.
     */
    public void save() {
        XStream xstream = new XStream(new StaxDriver());
        String object = xstream.toXML(downloads);
        try (OutputStreamWriter file = new OutputStreamWriter(new FileOutputStream(path+saveFilename), StandardCharsets.UTF_8)) {
            file.write(object);
        } catch (IOException ex) {
            Logger.getLogger(DownloadSaves.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Creates an empty file to store download information.
     */
    public void createNewFile() {
        String object="<?xml version=\"1.0\" ?><list></list>";
        try (OutputStreamWriter file = new OutputStreamWriter(new FileOutputStream(path+saveFilename), StandardCharsets.UTF_8)) {
            file.write(object);
        } catch (IOException ex) {
            Logger.getLogger(DownloadSaves.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Loads the download list from the disk.
     */
    public void load() {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(path+saveFilename), StandardCharsets.UTF_8)) {

            XStream xstream = new XStream(new StaxDriver());
            downloads = (List<DownloadState>) xstream.fromXML(reader);
        } catch (FileNotFoundException ex) {
            createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(DownloadSaves.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
