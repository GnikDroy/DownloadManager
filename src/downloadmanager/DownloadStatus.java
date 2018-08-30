package downloadmanager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gnik
 */
public enum DownloadStatus{
    NEW,
    STARTING,
    DOWNLOADING,
    PAUSED,
    STOPPED,
    ERROR,
    JOINING,
    COMPLETED,

}