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

import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author gnik
 */
public class DownloadPartThread {

    public Thread thread;
    public SimpleObjectProperty<DownloadPart> downloadPart;
    public ConcurrentLinkedQueue queueCommand;
    public ConcurrentLinkedQueue queueResponse;
    public SimpleObjectProperty<DownloadPartMetadata> downloadPartMetadata;

    public DownloadPartThread(DownloadPart downloadPart, DownloadPartMetadata downloadPartMetadata, Thread thread, ConcurrentLinkedQueue queueCommand, ConcurrentLinkedQueue queueResponse) {
        this.thread = thread;
        this.downloadPart = new SimpleObjectProperty<>(downloadPart);
        this.downloadPartMetadata = new SimpleObjectProperty<>(downloadPartMetadata);
        this.queueCommand = queueCommand;
        this.queueResponse = queueResponse;
    }

    public DownloadPartThread(DownloadPart downloadPart, DownloadPartMetadata downloadPartMetadata, ConcurrentLinkedQueue queueCommand, ConcurrentLinkedQueue queueResponse) {
        this.downloadPart = new SimpleObjectProperty<>(downloadPart);
        this.downloadPartMetadata = new SimpleObjectProperty<>(downloadPartMetadata);
        this.queueCommand = queueCommand;
        this.queueResponse = queueResponse;
        
    }
    
    public DownloadPart getDownloadPart(){
        return downloadPart.getValue();
    }
    
    public void setDownloadPart(DownloadPart t){
        downloadPart.setValue(t);
    }
    public DownloadPartMetadata getDownloadPartMetadata(){
        return downloadPartMetadata.getValue();
    }
}


