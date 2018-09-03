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

    public DownloadMetadata getDownloadMetadata() {
        return metadata.getValue();
    }

    public SimpleObjectProperty<DownloadMetadata> getDownloadMetadataProperty() {
        return metadata;
    }

    public List<DownloadPartMetadata> getPartMetadatas() {
        List<DownloadPartMetadata> metadatas = new ArrayList<>();
        for (DownloadPartThread dthread : downloadPartThreads) {
            metadatas.add(dthread.getDownloadPartMetadata());
        }
        return metadatas;
    }

    public void setHeaders() throws IOException {
        HttpURLConnection conn;
        conn = (HttpURLConnection) getDownloadMetadata().getUrl().openConnection();
        conn.setRequestMethod("HEAD");
        getDownloadMetadata().setSize(conn.getContentLengthLong());
        String ranges = conn.getHeaderField("Accept-Ranges");
        if (ranges != null && !ranges.equals("none")) {
            getDownloadMetadata().setAccelerated(true);
            setStatus(DownloadStatus.STARTING);
        }

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

    public void initialize() {
        //If download Part Threads is not empty and loaded from file then skip.
        if (downloadPartThreads.isEmpty()) {
            try {
                setHeaders();

            } catch (IOException ex) {
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
                setStatus(DownloadStatus.ERROR);
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

    private void setStatus(DownloadStatus downloadStatus) {
        getDownloadMetadata().setStatus(downloadStatus);
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

    public void joinThread(Thread thread) {
        if (thread != null && !thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void joinThreads() {
        for (DownloadPartThread downloadThread : downloadPartThreads) {
            joinThread(downloadThread.thread);
        }
    }

    public void waitUntilResponse(DownloadPartThread dthread, DownloadAction.Response response) {
        while (true) {
            if (!dthread.queueResponse.isEmpty() && dthread.queueResponse.peek().equals(response)) {
                dthread.queueResponse.poll();
                break;
            }
        }

    }

    public void pause() {
        if (getStatus() != DownloadStatus.DOWNLOADING) {
            return;
        }
        for (DownloadPartThread dthread : downloadPartThreads) {
            if (dthread.thread==null || !dthread.thread.isAlive()) {
                return;
            }
            dthread.queueCommand.add(DownloadAction.Command.PAUSE);
            waitUntilResponse(dthread, DownloadAction.Response.PAUSED);
        }

        setStatus(DownloadStatus.PAUSED);

    }

    public void resume() {
        if (getStatus() != DownloadStatus.PAUSED) {
            return;
        }
        for (DownloadPartThread dthread : downloadPartThreads) {
            if (dthread.thread==null || !dthread.thread.isAlive()) {
                return;
            }
            dthread.queueCommand.add(DownloadAction.Command.RESUME);
            waitUntilResponse(dthread, DownloadAction.Response.RESUMED);
        }
        setStatus(DownloadStatus.DOWNLOADING);
    }

    public void stop() {
        if (getStatus() == DownloadStatus.STOPPED) {
            return;
        }
        for (DownloadPartThread dthread : downloadPartThreads) {
            if (dthread.thread==null || !dthread.thread.isAlive()) {
                return;
            }
            dthread.queueCommand.add(DownloadAction.Command.STOP);
            waitUntilResponse(dthread, DownloadAction.Response.STOPPED);
        }

        setStatus(DownloadStatus.STOPPED);
    }

    public void startDownloadPartThreads() {
        if (!getDownloadMetadata().getAccelerated()) {
            setStatus(DownloadStatus.ERROR);
            return;
        }
        setStatus(DownloadStatus.DOWNLOADING);
        for (DownloadPartThread downloadThread : downloadPartThreads) {
            Thread thread = new Thread(downloadThread.getDownloadPart());
            thread.setName(this.toString() + " " + downloadThread.downloadPart.toString());
            downloadThread.thread = thread;
            thread.start();
        }
    }

    public void deleteDownloadPartFiles() throws IOException {
        for (DownloadPartThread downloadThread : downloadPartThreads) {
            DownloadPart downloadPart = downloadThread.getDownloadPart();
            Files.deleteIfExists(Paths.get(downloadPart.getFilename()));
        }
    }

    public void copyToStream(BufferedOutputStream outFile, BufferedInputStream inFile) throws IOException {
        int byt;
        while ((byt = inFile.read()) != -1 && outFile != null) {
            outFile.write(byt);
        }
    }

    public void joinDownloadParts() {
        if (!isDownloaded()) {
            return;
        }
        setStatus(DownloadStatus.JOINING);

        try(BufferedOutputStream outFile = new BufferedOutputStream(new FileOutputStream(getDownloadMetadata().getFilename()))) {
            for (DownloadPartThread downloadThread : downloadPartThreads) {
                DownloadPart downloadPart = downloadThread.getDownloadPart();
                try(BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(downloadPart.getFilename()))){
                    copyToStream(outFile, inFile);
                }
            }
            setStatus(DownloadStatus.COMPLETED);
            deleteDownloadPartFiles();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public void downloadLoop(){
        while (!isDownloaded()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                setStatus(DownloadStatus.ERROR);
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!this.queueCommand.isEmpty()) {
                DownloadAction.Command command = (DownloadAction.Command) this.queueCommand.poll();
                switch (command) {
                    case PAUSE:
                        this.pause();
                        this.queueResponse.add(DownloadAction.Response.PAUSED);
                        break;
                    case STOP:
                        this.stop();
                        this.joinThreads();
                        this.queueResponse.add(DownloadAction.Response.STOPPED);
                        return;
                    case RESUME:
                        this.resume();
                        this.queueResponse.add(DownloadAction.Response.RESUMED);
                        break;
                    default:
                        break;
                }
            }
        }
    }
    @Override
    public void run() {
        if (getDownloadMetadata().getStatus() == DownloadStatus.COMPLETED) {
            return;
        }
        this.initialize();
        this.startDownloadPartThreads();
        this.downloadLoop();
        this.joinThreads();
        this.joinDownloadParts();
    }

}
