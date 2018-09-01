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

import java.net.HttpURLConnection;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gnik
 */
public class Download implements Runnable {

    private final SimpleObjectProperty<DownloadMetadata> metadata;
    private final List<DownloadPartThread> downloadPartThreads = FXCollections.observableArrayList();
    private final ConcurrentLinkedQueue queueCommand;
    private final ConcurrentLinkedQueue queueResponse;

    public Download(DownloadMetadata metadata, ConcurrentLinkedQueue queueCommand, ConcurrentLinkedQueue queueResponse) {
        this.metadata = new SimpleObjectProperty<>(metadata);
        this.queueCommand = queueCommand;
        this.queueResponse = queueResponse;
    }

    @Override
    public String toString() {
        return "DownloadID:" + metadata.getValue().getDownloadID();
    }
    
    public DownloadMetadata getDownloadMetadata(){
        return metadata.getValue();
    }
    public SimpleObjectProperty<DownloadMetadata> getDownloadMetadataProperty(){
        return metadata;
    }
    
    public List<DownloadPartMetadata> getPartMetadatas(){
        List<DownloadPartMetadata> metadatas=new ArrayList<>();
        for (DownloadPartThread dthread: downloadPartThreads ){
            metadatas.add(dthread.getDownloadPartMetadata());
        }
        return metadatas;
    }
    
    public void setHeaders() throws IOException {
        HttpURLConnection conn = null;
        conn = (HttpURLConnection) getDownloadMetadata().getUrl().openConnection();
        conn.setRequestMethod("HEAD");
        getDownloadMetadata().setSize(conn.getContentLengthLong());
        String ranges = conn.getHeaderField("Accept-Ranges");
        if (ranges!=null && !ranges.equals("none")) {
            getDownloadMetadata().setAccelerated(true);
            getDownloadMetadata().setStatus(DownloadStatus.STARTING) ;
        }
        getDownloadMetadata().setStatus(DownloadStatus.ERROR);
        
    }

    public void loadDownlaodPartMetadatas(List<DownloadPartMetadata> downloadPartMetadatas) {
        for (DownloadPartMetadata downloadPartMetadata : downloadPartMetadatas) {
            ConcurrentLinkedQueue queueCom = new ConcurrentLinkedQueue();
            ConcurrentLinkedQueue queueRes = new ConcurrentLinkedQueue();
            downloadPartMetadata.setDownloadMetadata(getDownloadMetadata());
            DownloadPart downloadPart = new DownloadPart(downloadPartMetadata, queueCom, queueRes);
            downloadPartThreads.add(new DownloadPartThread(downloadPart, downloadPartMetadata, queueCom, queueRes));
        }
    }

    public void createDownloadPartThreads() {
        int partID = 0;
        for (Part part : divideDownload()) {
            DownloadPartMetadata part_metadata = new DownloadPartMetadata(getDownloadMetadata(), partID, part);
            ConcurrentLinkedQueue queueCom = new ConcurrentLinkedQueue();
            ConcurrentLinkedQueue queueRes = new ConcurrentLinkedQueue();
            DownloadPart downloadPart = new DownloadPart(part_metadata, queueCom, queueRes);
            downloadPartThreads.add(new DownloadPartThread(downloadPart, part_metadata, queueCom, queueRes));
            partID++;
        }

    }
    public void initialize(){
        if (downloadPartThreads.isEmpty()) {
            try {
                setHeaders();

            } catch (IOException ex) {
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
                getDownloadMetadata().setStatus(DownloadStatus.ERROR);
                return;
            }
            createDownloadPartThreads();
           
        }
    }

    private List<Part> divideDownload() {
        List<Part> parts = new ArrayList<>();
        long start = 0;
        double size = (double) getDownloadMetadata().getSize() / getDownloadMetadata().getParts();
        for (int cnt = 0; cnt < getDownloadMetadata().getParts(); cnt++) {
            Part part = new Part(start, (int) Math.round(size * (cnt + 1)));
            parts.add(part);
            start = (int) Math.round(size * (cnt + 1)) + 1;
            
        }
        return parts;
    }

    public DownloadStatus getStatus() {
        return getDownloadMetadata().getStatus();
    }

    public boolean isDownloaded() {
        for (DownloadPartThread downloadThread : downloadPartThreads) {
            if (downloadThread.getDownloadPart().getStatus() != DownloadStatus.COMPLETED) {
                return false;
            }
        }
        return true;
    }

    public boolean joinThreads() {
        boolean alljoined = true;
        for (DownloadPartThread downloadThread : downloadPartThreads) {
            Thread th = downloadThread.thread;
            if (th!=null && !th.isAlive()) {
                try {
                    th.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                alljoined = false;
            }
        }
        return alljoined;
    }

    public void pause() {
        if (getDownloadMetadata().getStatus() != DownloadStatus.DOWNLOADING) {
            return;
        }
        for (DownloadPartThread dthread : downloadPartThreads) {
            if (!dthread.thread.isAlive()) {
                return;
            }
            dthread.queueCommand.add("pause");
            while (true) {
                if (!dthread.queueResponse.isEmpty() && dthread.queueResponse.peek().equals("paused")) {
                    dthread.queueResponse.poll();
                    break;
                }
            }
        }
        getDownloadMetadata().setStatus( DownloadStatus.PAUSED);

    }

    public void resume() {
        if (getDownloadMetadata().getStatus() != DownloadStatus.PAUSED) {
            return;
        }
        for (DownloadPartThread dthread : downloadPartThreads) {
            if (!dthread.thread.isAlive()) {
                return;
            }
            dthread.queueCommand.add("resume");
            while (true) {
                if (!dthread.queueResponse.isEmpty() && dthread.queueResponse.peek().equals("resumed")) {
                    dthread.queueResponse.poll();
                    break;
                }
            }
        }
        getDownloadMetadata().setStatus(DownloadStatus.DOWNLOADING);
    }

    public void stop() {
        if (getDownloadMetadata().getStatus() != DownloadStatus.PAUSED) {
            return;
        }
        for (DownloadPartThread dthread : downloadPartThreads) {
            if (!dthread.thread.isAlive()) {
                return;
            }
            dthread.queueCommand.add("stop");
            while (true) {
                if (!dthread.queueResponse.isEmpty() && dthread.queueResponse.peek().equals("stopped")) {
                    dthread.queueResponse.poll();
                    break;
                }
            }
        }
        getDownloadMetadata().setStatus(DownloadStatus.STOPPED);
    }

    public void acceleratedDownload() {
        if (!getDownloadMetadata().getAccelerated()) {
            return;
        }
        getDownloadMetadata().setStatus(DownloadStatus.DOWNLOADING);
        for (DownloadPartThread downloadThread : downloadPartThreads) {
            Thread thread = new Thread(downloadThread.getDownloadPart());
            thread.setName(this.toString() + " " + downloadThread.downloadPart.toString());
            downloadThread.thread = thread;
            thread.start();
        }
    }

    public void joinParts() {
        if (!isDownloaded()) {
            return;
        }
        getDownloadMetadata().setStatus(DownloadStatus.JOINING);
        BufferedOutputStream outFile = null;
        try {
            outFile = new BufferedOutputStream(new FileOutputStream(getDownloadMetadata().getFilename()));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            for (DownloadPartThread downloadThread : downloadPartThreads) {
                DownloadPart downloadPart = downloadThread.getDownloadPart();
                BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(downloadPart.getFilename()));
                int byt;
                while ((byt = inFile.read()) != -1 && outFile != null) {
                    outFile.write(byt);
                }
                inFile.close();
            }
            if (outFile != null) {
                outFile.close();
            }
            
            //This deletes the temporary Files.
            for (DownloadPartThread downloadThread : downloadPartThreads) {
                DownloadPart downloadPart = downloadThread.getDownloadPart();
                Files.deleteIfExists(Paths.get(downloadPart.getFilename()));
            }
            getDownloadMetadata().setStatus(DownloadStatus.COMPLETED);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void run() {
        if (getDownloadMetadata().getStatus()==DownloadStatus.COMPLETED){return;}
        this.initialize();
        this.acceleratedDownload();
        
        while (!isDownloaded()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                this.getDownloadMetadata().setStatus(DownloadStatus.ERROR);
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!this.queueCommand.isEmpty()) {
                String command = (String) this.queueCommand.poll();
                switch (command) {
                    case "pause":
                        this.pause();
                        this.queueResponse.add("paused");
                        break;
                    case "stop":
                        this.pause();
                        this.stop();
                        this.joinThreads();
                        this.queueResponse.add("stopped");
                        return;
                    case "resume":
                        this.resume();
                        this.queueResponse.add("resumed");
                        break;
                    default:
                        break;
                }
            }
        }
        this.joinThreads();
        this.joinParts();
    }

}

