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
 * This object contains all DownloadThread/Download objects.
 * It handles all actions of the download.
 * @author gnik
 */
public class DownloadPool {

    private final ObservableList<DownloadThread> downloadThreads = FXCollections.observableArrayList();
    DownloadSaves downloadSaves=new DownloadSaves();
    
    public DownloadPool() {
        downloadSaves.load();
    }

    /**
     * This saves the current download list to the disk.
     */
    public void save(){
        downloadSaves.clear();
        for (DownloadThread downloadThread:downloadThreads){
            DownloadState download;
            download=new DownloadState(downloadThread.getDownloadMetadata(),downloadThread.download.getValue().getPartMetadatas());
            downloadSaves.addDownload(download);
        }
        downloadSaves.save();
    }

    /**
     * This loads the download list from the disk.
     * Creates a DownloadPool object that contains all downloads
     *  @return Returns the DownloadPool object that contains all downloads from disk
     */
    public DownloadPool load() {
        if(downloadSaves.getDownloads()==null){return this;}
        for (DownloadState downloadState : downloadSaves.getDownloads()) {
            
                DownloadMetadata downloadMetadata=downloadState.downloadMetadata;
                List<DownloadPartMetadata> downloadPartMetadata=downloadState.downloadPartMetadata;
                ConcurrentLinkedQueue queueCommand = new ConcurrentLinkedQueue();
                ConcurrentLinkedQueue queueResponse = new ConcurrentLinkedQueue();
                Download download = new Download(downloadMetadata, queueCommand, queueResponse);
                download.loadDownloadPartMetadatas(downloadPartMetadata);
                Thread thread = new Thread(download);
                DownloadThread downloadThread = new DownloadThread(downloadMetadata, download, thread, queueCommand, queueResponse);
                downloadThreads.add(downloadThread);
                thread.start();

            
        }
        return this;
    }

    /**
     * Checks if a URL is valid.
     * @param url String representation of the URL
     * @return If a URL is valid
     */
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

    /**
     * Waits until response of a command is recieved.
     * @param downloadThread The thread to which the command is issued
     * @param response The response from the command.
     */
    private void waitUntilCommand(DownloadThread downloadThread,DownloadAction.Response response){
        if (!downloadThread.thread.isAlive()) {
            return;
        }
        while (true) {
            if(!downloadThread.queueResponse.isEmpty()){
                 if(downloadThread.queueResponse.peek().equals(response)){
                     downloadThread.queueResponse.poll();
                     break;
                 }
            }
        }
    }

    /**
     * Issues a command to the thread.
     * @param downloadThread The object to issue a command to
     * @param command The command to be issued.
     */
    private void issueCommand(DownloadThread downloadThread, DownloadAction.Command command){
        if (!downloadThread.thread.isAlive()) {
            return;
        }
        downloadThread.queueCommand.add(command);
    }

    /**
     * Stops the download from a particular DownloadThread
     * @param downloadThread The download thread to be stopped.
     */
    public void stopDownload(DownloadThread downloadThread) {
        issueCommand(downloadThread,DownloadAction.Command.STOP);
        waitUntilCommand(downloadThread,DownloadAction.Response.STOPPED);
        joinThread(downloadThread);

    }

    /**
     * Pauses the download from a particular DownloadThread.
     * @param downloadThread The download thread to be paused.
     */
    public void pauseDownload(DownloadThread downloadThread) {
        issueCommand(downloadThread, DownloadAction.Command.PAUSE);
        waitUntilCommand(downloadThread,DownloadAction.Response.PAUSED);
    }
    /**
     * Resumes the download from a particular DownloadThread.
     * @param downloadThread The download thread to be resumed.
     */

    public void resumeDownload(DownloadThread downloadThread) {
        issueCommand(downloadThread, DownloadAction.Command.RESUME);
        waitUntilCommand(downloadThread,DownloadAction.Response.RESUMED);
    }

    /**
     * Stops and removes the download from pool.
     * @param downloadThread The download thread to be removed.
     */

    public void removeDownload(DownloadThread downloadThread){
        if(downloadThread.thread.isAlive()){
            stopDownload(downloadThread);
        }
        downloadThreads.remove(downloadThread);
    }

    /**
     * Stops all downloads.
     */

    public void stopAll() {
        for (DownloadThread downloadThread : downloadThreads) {
            stopDownload(downloadThread);
        }
    }

    /**
     * Joins a thread of the downloadThread object.
      * @param downloadThread The downloadThread object to be joined
     */
    public void joinThread(DownloadThread downloadThread){
        try {
                downloadThread.thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloadPool.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    /**
     * Joins all downloadThread objects.
     */
    public void joinThreads() {
        for (DownloadThread downloadThread : downloadThreads) {
            joinThread(downloadThread);
        }
    }

    /**
     * Starts a new download.
     * @param url The download URL
     */
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

