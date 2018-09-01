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

import java.io.IOException;
import java.net.URLConnection;
import java.io.*;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author gnik
 */
public class DownloadPart implements Runnable {

    private final SimpleObjectProperty<DownloadPartMetadata> metadata;
    private final ConcurrentLinkedQueue queueCommand;
    private final ConcurrentLinkedQueue queueResponse;

    public DownloadPart(DownloadPartMetadata metadata, ConcurrentLinkedQueue queueCommand, ConcurrentLinkedQueue queueResponse) {
        this.queueCommand = queueCommand;
        this.queueResponse = queueResponse;
        this.metadata = new SimpleObjectProperty<>(metadata);

    }

    public DownloadPartMetadata getMetadata() {
        return metadata.getValue();
    }

    @Override
    public String toString() {
        return "DownloadPartID:" + getMetadata().partID;
    }

    public DownloadStatus getStatus() {
        return getMetadata().getStatus();
    }

    public void pause() {
        if (getMetadata().getStatus() == DownloadStatus.DOWNLOADING) {
            getMetadata().setStatus(DownloadStatus.PAUSED);
        }
    }

    public void resume() {
        if (getMetadata().getStatus() == DownloadStatus.PAUSED) {
            getMetadata().setStatus(DownloadStatus.DOWNLOADING);
        }
    }

    public void stop() {
        if (getMetadata().getStatus() == DownloadStatus.PAUSED || getMetadata().getStatus() == DownloadStatus.PAUSED) {
            getMetadata().setStatus(DownloadStatus.STOPPED);
        }
    }

    public String getFilename() {
        return getMetadata().getFilename();
    }

    public boolean isComplete() {
        return ((getMetadata().getCompletedBytes() + getMetadata().getPart().getStartByte()) == getMetadata().getPart().getEndByte());
    }

    private BufferedInputStream getConnectionStream() throws IOException {
        //Setting up the connection.
        URLConnection connection = getMetadata().downloadMetadata.getUrl().openConnection();
        connection.setRequestProperty("Range", "bytes=" + String.valueOf(getMetadata().getPart().getStartByte() + getMetadata().getCompletedBytes()) + "-" + String.valueOf(getMetadata().getPart().getEndByte()));
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(getMetadata().downloadMetadata.getTimeout());
        connection.connect();

        BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
        return inputStream;
    }

    private boolean copyToStream(BufferedInputStream inputStream, BufferedOutputStream fileStream) throws IOException {
        int byt;
        long completedBytes = getMetadata().getCompletedBytes();

        while ((byt = inputStream.read()) != -1) {
            fileStream.write(byt);
            completedBytes++;
            getMetadata().setCompletedBytes(completedBytes);

            if (!queueCommand.isEmpty()) {
                if (queueCommand.peek().equals(DownloadAction.Command.PAUSE)) {
                    pause();
                    queueCommand.poll();
                    queueResponse.add(DownloadAction.Response.PAUSED);
                    return false;
                } else if (queueCommand.peek().equals(DownloadAction.Command.STOP)) {
                    stop();
                    //I am not adding a poll here because it will stop execution in run thread as well.
                    queueResponse.add(DownloadAction.Response.STOPPED);
                    return false;
                }
            }
        }
        return true;

    }

    public void download() throws IOException, SocketTimeoutException {
        getMetadata().setStatus(DownloadStatus.DOWNLOADING);
        boolean append = (getMetadata().getCompletedBytes() != 0);

        BufferedInputStream inputStream = getConnectionStream();
        BufferedOutputStream fileStream = new BufferedOutputStream(new FileOutputStream(getMetadata().filename, append));
        try {
            if (copyToStream(inputStream, fileStream)) {
                getMetadata().setStatus(DownloadStatus.COMPLETED);
            }
        } finally {
            inputStream.close();
            fileStream.close();
        }

    }

    public void safeDownload() {
        try {
            download();
        } catch (IOException ex) {
            getMetadata().setStatus(DownloadStatus.ERROR);
            getMetadata().incrementRetries();
            Logger.getLogger(DownloadPart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        if (DownloadStatus.COMPLETED == getMetadata().getStatus()) {
            return;
        }
        safeDownload();
        //Infinite loop until the downloadstatus is completed 
        while (getMetadata().getStatus() != DownloadStatus.COMPLETED) {
            //Retry if there is any errors.
            if (getMetadata().getStatus() == DownloadStatus.ERROR) {
                safeDownload();
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloadPart.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!queueCommand.isEmpty()) {
                DownloadAction.Command command = (DownloadAction.Command) queueCommand.poll();
                switch (command) {
                    case STOP:
                        stop();
                        queueResponse.add(DownloadAction.Response.STOPPED);
                        return;
                    case RESUME:
                        resume();
                        queueResponse.add(DownloadAction.Response.RESUMED);
                        safeDownload();
                        break;
                    default:
                        break;
                }

            }
        }
    }
}
