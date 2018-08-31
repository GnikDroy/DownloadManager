package downloadmanager;


import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.beans.property.SimpleObjectProperty;

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

/**
 *
 * @author gnik
 */
public class DownloadThread {

    public SimpleObjectProperty<DownloadMetadata> downloadMetadata;
    public SimpleObjectProperty<Download> download;
    public Thread thread;
    public ConcurrentLinkedQueue queueCommand;
    public ConcurrentLinkedQueue queueResponse;

    public DownloadThread(DownloadMetadata downloadMetadata, Download download, Thread thread, ConcurrentLinkedQueue queueCommand, ConcurrentLinkedQueue queueResponse) {
        this.downloadMetadata = new SimpleObjectProperty<>(downloadMetadata);
        this.download = new SimpleObjectProperty<>(download);
        this.thread = thread;
        this.queueCommand = queueCommand;
        this.queueResponse = queueResponse;
    }
    
    
    public Download getDownload(){
        return download.getValue();
    }
    public DownloadMetadata getDownloadMetadata() {
        return downloadMetadata.getValue();
    }
}
