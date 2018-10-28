package com.downloadmanager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xmlunit.builder.Input;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

public class DownloadSavesTest {
    private final static String SAVING_FILENAME = "history.dat";
    private final static String EMPTY_XML = "<?xml version=\"1.0\" ?><list></list>";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private String path;

    @Before
    public void init() throws IOException {
        File tmpFolder = folder.newFolder("subfolder");
        path = tmpFolder.getPath();
    }

    @Test
    public void testCreateNewFile() {
        DownloadSaves instance = new DownloadSaves(path);
        DownloadState downloadState = new DownloadState();
        instance.addDownload(downloadState);
        instance.createNewFile();
        File file = new File(path + SAVING_FILENAME);
        Assert.assertTrue(file.exists());
        Assert.assertThat(file, isIdenticalTo(Input.fromString(EMPTY_XML)));
    }

    @Test
    public void testSaveEmpty() {
        DownloadSaves instance = new DownloadSaves(path);
        instance.save();
        File file = new File(path + SAVING_FILENAME);
        Assert.assertTrue(file.exists());
        Assert.assertThat(file, isIdenticalTo(Input.fromString(EMPTY_XML)));
    }

    @Test
    public void testSaveWithState() {
        DownloadSaves instance = new DownloadSaves(path);
        DownloadState downloadState = new DownloadState();
        instance.addDownload(downloadState);
        instance.save();
        File file = new File(path + SAVING_FILENAME);
        Assert.assertTrue(file.exists());
        Assert.assertThat(file, isIdenticalTo(Input.fromString("<?xml version=\"1.0\" ?><list><com.downloadmanager.DownloadState></com.downloadmanager.DownloadState></list>")));
    }


    @Test
    public void testSaveWithDownload() throws MalformedURLException {
        DownloadSaves instance = new DownloadSaves(path);
        addDownloads(instance);
        instance.save();

        File file = new File(path + SAVING_FILENAME);
        Assert.assertTrue(file.exists());
        Assert.assertThat(file, isIdenticalTo(Input.fromFile(new File("src/test/resources/result_example.xml")))
                .normalizeWhitespace());
    }

    @Test
    public void testLoad() {
        DownloadSaves instance = new DownloadSaves(path);
        DownloadState downloadState = new DownloadState();
        instance.addDownload(downloadState);
        instance.load();
        File file = new File(path + SAVING_FILENAME);
        Assert.assertTrue(file.exists());

    }

    private void addDownloads(DownloadSaves downloadSaves) throws MalformedURLException {
        String url = "http://example.org/%d.pdf";
        for (int i = 0; i < 5; i++) {
            downloadSaves.addDownload(new DownloadState(new DownloadMetadata(String.format(url, i), i), new ArrayList<>()));
        }
    }
}
