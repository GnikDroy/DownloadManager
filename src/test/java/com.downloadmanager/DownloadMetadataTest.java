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


import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

import java.net.MalformedURLException;
import java.net.URL;

public class DownloadMetadataTest {

    @Test
    public void testConstructor() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        Assert.assertThat(instance, is(not(nullValue())));
    }

    @Test(expected = MalformedURLException.class)
    public void testConstructorNullUrl() throws MalformedURLException {
        int id = 1;
        new DownloadMetadata(null, id);
    }

    @Test(expected = MalformedURLException.class)
    public void testConstructorEmptyUrl() throws MalformedURLException {
        String url = "";
        int id = 1;
        new DownloadMetadata(url, id);
    }

    @Test
    public void testGetUrl() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        URL result = instance.getUrl();
        URL expected = new URL(url);
        Assert.assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void testGetUrlProperty() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        URL result = (URL) instance.getUrlProperty().getValue();
        URL expected = new URL(url);
        Assert.assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void testGetDownloadID() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        Integer result = instance.getDownloadID();
        Assert.assertThat(result, is(equalTo(id)));
    }

    @Test
    public void testGetDownloadIDProperty() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        Integer result = instance.getDownloadIDProperty().getValue();
        Assert.assertThat(result, is(equalTo(id)));
    }

    @Test
    public void testGetFilename() throws MalformedURLException {
        String url = "http://www.example.org/sample.doc";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        String result = instance.getFilename();
        String expected = String.format("%d_sample.doc", id);
        Assert.assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void testGetFilenameNoFileUrl() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        String result = instance.getFilename();
        String expected = String.format("%d_", id);
        Assert.assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void testGetFilenameNegativeId() throws MalformedURLException {
        String url = "http://www.example.org/sample.doc";
        int id = -1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        String result = instance.getFilename();
        String expected = String.format("%d_sample.doc", id);
        Assert.assertThat(result, is(equalTo(expected)));
    }

    @Test(expected = NullPointerException.class)
    public void testGetSize() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        instance.getSize();
    }

    @Test
    public void testSetSize() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        long size = 10L;
        instance.setSize(size);
        long result = instance.getSize();
        Assert.assertThat(result, is(equalTo(size)));
    }

    @Test
    public void testSetSizeNegativeValue() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        long size = -10L;
        instance.setSize(size);
        long result = instance.getSize();
        Assert.assertThat(result, is(equalTo(size)));
    }

    @Test
    public void testGetStatus() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        DownloadStatus result = instance.getStatus();
        DownloadStatus expected = DownloadStatus.NEW;
        Assert.assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void testSetStatus() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        for (DownloadStatus downloadStatus : DownloadStatus.values()) {
            instance.setStatus(downloadStatus);
            DownloadStatus result = instance.getStatus();
            Assert.assertThat(result, is(equalTo(downloadStatus)));
        }
    }

    @Test
    public void testGetAccelerated() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        boolean result = instance.getAccelerated();
        Assert.assertThat(result, is(equalTo(false)));
    }

    @Test
    public void testSetAccelerated() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        instance.setAccelerated(true);
        boolean result = instance.getAccelerated();
        Assert.assertThat(result, is(equalTo(true)));
    }


    @Test
    public void testGetTimeout() throws MalformedURLException {
        String url = "http://www.example.org";
        int id = 1;
        DownloadMetadata instance = new DownloadMetadata(url, id);
        int result = instance.getTimeout();
        Assert.assertThat(result, is(equalTo(10000)));
    }
}
