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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author gnik
 */
public class DownloadPool {

    private final ObservableList<DownloadThread> downloadThreads = FXCollections.observableArrayList();
    DownloadSaves downloadSaves=new DownloadSaves();
    
    public DownloadPool() {
        downloadSaves.load();
    }
    
    
    public void save(){
        downloadSaves.clear();
        for (DownloadThread downloadThread:downloadThreads){
            DownloadState download;
            download=new DownloadState(downloadThread.getDownloadMetadata(),downloadThread.download.getValue().getPartMetadatas());
            downloadSaves.addDownload(download);
        }
        downloadSaves.save();
    }
    
    public DownloadPool load() {
        if(downloadSaves.getDownloads()==null){return this;}
        for (DownloadState downloadState : downloadSaves.getDownloads()) {
            
                DownloadMetadata downloadMetadata=downloadState.downloadMetadata;
                List<DownloadPartMetadata> downloadPartMetadata=downloadState.downloadPartMetadata;
                ConcurrentLinkedQueue queueCommand = new ConcurrentLinkedQueue();
                ConcurrentLinkedQueue queueResponse = new ConcurrentLinkedQueue();
                Download download = new Download(downloadMetadata, queueCommand, queueResponse);
                download.loadDownlaodPartMetadatas(downloadPartMetadata);
                Thread thread = new Thread(download);
                DownloadThread downloadThread = new DownloadThread(downloadMetadata, download, thread, queueCommand, queueResponse);
                downloadThreads.add(downloadThread);
                thread.start();

            
        }
        return this;
    }

    
    public boolean isValidUrl(String url) {
        try {
            URL test=new URL(url);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }
    
    public ObservableList<DownloadThread> getDownloadThreads() {
        return downloadThreads;
    }


    private void waitUntilCommand(DownloadThread downloadThread,DownloadAction.Response command){
        while (true) {
            if(!downloadThread.queueResponse.isEmpty()){
                 if(downloadThread.queueResponse.peek().equals(command)){
                     downloadThread.queueResponse.poll();
                     break;
                 }
            }
        }
    }
    public void stopDownload(DownloadThread downloadThread) {
        if (!downloadThread.thread.isAlive()) {
            return;
        }
        downloadThread.queueCommand.add(DownloadAction.Command.STOP);
        waitUntilCommand(downloadThread,DownloadAction.Response.STOPPED);
        joinThread(downloadThread);

    }

    
    public void pauseDownload(DownloadThread downloadThread) {
        if (!downloadThread.thread.isAlive()) {
            return;
        }
        downloadThread.queueCommand.add(DownloadAction.Command.PAUSE);
        waitUntilCommand(downloadThread,DownloadAction.Response.PAUSED);
    }
    public void resumeDownload(DownloadThread downloadThread) {
        if (!downloadThread.thread.isAlive()) {
            return;
        }
        downloadThread.queueCommand.add(DownloadAction.Command.RESUME);
        waitUntilCommand(downloadThread,DownloadAction.Response.RESUMED);
    }
    
    public void removeDownload(DownloadThread downloadThread){
        if(downloadThread.thread.isAlive()){
            stopDownload(downloadThread);
            try {
                downloadThread.thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloadPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        downloadThreads.remove(downloadThread);
    }

    public void pauseAll() {
        for (DownloadThread downloadThread : downloadThreads) {
            pauseDownload(downloadThread);
        }
    }

    public void resumeAll() {
        for (DownloadThread downloadThread : downloadThreads) {
            resumeDownload(downloadThread);
        }
    }

    public void stopAll() {
        for (DownloadThread downloadThread : downloadThreads) {
            stopDownload(downloadThread);
        }
    }
    public void joinThread(DownloadThread downloadThread){
        try {
                downloadThread.thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloadPool.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    public void joinThreads() {
        for (DownloadThread downloadThread : downloadThreads) {
            joinThread(downloadThread);
        }
    }
    public void newDownload(String url) {
        DownloadMetadata downloadMetadata;
        try {
            downloadMetadata = new DownloadMetadata(url, downloadThreads.size());
        } catch (MalformedURLException ex) {
            Logger.getLogger(DownloadManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        ConcurrentLinkedQueue queueCommand = new ConcurrentLinkedQueue();
        ConcurrentLinkedQueue queueResponse = new ConcurrentLinkedQueue();
        Download download = new Download(downloadMetadata, queueCommand, queueResponse);
        Thread thread = new Thread(download);
        DownloadThread downloadThread = new DownloadThread(downloadMetadata, download, thread, queueCommand, queueResponse);
        downloadThreads.add(downloadThread);
        thread.start();
    }

}

