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
    DownloadsSaves downloadssaves=new DownloadsSaves();
    
    public DownloadPool() {
        downloadssaves.load();
        if(downloadssaves.getDownloads()==null){return;}
        load(downloadssaves);
    }
    
    
    public void save(){
        downloadssaves.clear();
        for (DownloadThread downloadThread:downloadThreads){
            DownloadState download;
            download=new DownloadState(downloadThread.getDownloadMetadata(),downloadThread.download.getValue().get_part_metadata());
            downloadssaves.addDownload(download);
        }
        downloadssaves.save();
    }
    
    public void load(DownloadsSaves saves) {
        for (DownloadState downloadState : saves.getDownloads()) {
            
                DownloadMetadata downloadMetadata=downloadState.downloadMetadata;
                List<DownloadPartMetadata> downloadPartMetadata=downloadState.downloadPartMetadata;
                ConcurrentLinkedQueue queueCommand = new ConcurrentLinkedQueue();
                ConcurrentLinkedQueue queueResponse = new ConcurrentLinkedQueue();
                Download download = new Download(downloadMetadata, queueCommand, queueResponse);
                download.load(downloadPartMetadata);
                Thread thread = new Thread(download);
                DownloadThread downloadThread = new DownloadThread(downloadMetadata, download, thread, queueCommand, queueResponse);
                downloadThreads.add(downloadThread);
                thread.start();

            
        }
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


    public void stopDownload(DownloadThread downloadThread) {
        if (!downloadThread.thread.isAlive()) {
            return;
        }
        downloadThread.queueCommand.add("stop");
        while (downloadThread.queueResponse.peek() != "stopped") {
        }
        downloadThread.queueResponse.poll();
        try {
            downloadThread.thread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(DownloadPool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resumeDownload(DownloadThread downloadThread) {
        if (!downloadThread.thread.isAlive()) {
            return;
        }
        downloadThread.queueCommand.add("resume");
        while (downloadThread.queueResponse.peek() != "resumed") {
        }
        downloadThread.queueResponse.poll();
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
    
    public void pauseDownload(DownloadThread downloadThread) {
        if (!downloadThread.thread.isAlive()) {
            return;
        }
        downloadThread.queueCommand.add("pause");
        while (downloadThread.queueResponse.peek() != "paused") {
        }
        downloadThread.queueResponse.poll();
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

    public void joinThreads() {
        for (DownloadThread downloadThread : downloadThreads) {
            try {
                downloadThread.thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloadPool.class.getName()).log(Level.SEVERE, null, ex);
            }
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

