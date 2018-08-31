/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.io.IOException;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 *
 * @author gnik
 */
public class DownloadManager extends Application{
    DownloadPool downloadPool=new DownloadPool();
    Stage window;
    TableView<Download> table;
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String args[]) throws IOException, InterruptedException{

        
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
    String url="http://download.support.xerox.com/pub/docs/FlowPort2/userdocs/any-os/en/fp_dc_setup_guide.pdf";
    downloadPool.newDownload(url);
    url="http://kmmc.in/wp-content/uploads/2014/01/lesson2.pdf";
    downloadPool.newDownload(url);
    window=stage;
    window.setTitle("Download Manager");
    
    TableColumn<Download,Integer> idColumn=new TableColumn<>("ID");
    idColumn.setMinWidth(50);
    idColumn.setCellValueFactory( (TableColumn.CellDataFeatures<Download, Integer> download) -> new ReadOnlyObjectWrapper(download.getValue().get_metadata().getDownloadID()));
    
    TableColumn<Download,String> urlColumn=new TableColumn<>("URL");
    urlColumn.setMinWidth(200);
    urlColumn.setCellValueFactory((TableColumn.CellDataFeatures<Download, String> download) -> new SimpleObjectProperty(download.getValue().get_metadata().getUrl()));
    
    
    TableColumn<Download,String> statusColumn=new TableColumn<>("Status");
    statusColumn.setMinWidth(200);
    statusColumn.setCellValueFactory((TableColumn.CellDataFeatures<Download, String> download) -> new SimpleObjectProperty(download.getValue().get_metadata().getStatus()));
    
    
    
    table=new TableView();
    table.setItems(get_downloads());
    table.getColumns().addAll(idColumn,urlColumn,statusColumn);
    
    
    
            
    VBox vBox=new VBox();
    vBox.getChildren().addAll(table);
    Scene scene=new Scene(vBox);
    window.setScene(scene);
    window.show();
    }
    
    public ObservableList<Download> get_downloads(){
        ObservableList<Download> downloads=FXCollections.observableArrayList();
        downloads.addAll(downloadPool.getDownloads());
        return downloads;
    }
    
}
