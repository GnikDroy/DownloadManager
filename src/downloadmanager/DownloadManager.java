/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloadmanager;

import java.io.IOException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author gnik
 */
public class DownloadManager extends Application {

    DownloadPool downloadPool = new DownloadPool();
    Stage window;
    TableView<DownloadThread> table;

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String args[]) throws IOException, InterruptedException {

        launch(args);
    }

    @Override
    public void stop() {
        downloadPool.stopAll();
        downloadPool.joinThreads();
        downloadPool.save();
    }

    @Override
    public void start(Stage stage) throws Exception {
        window = stage;
        window.setTitle("Download Manager");
        TableColumn<DownloadThread, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setMinWidth(50);
        idColumn.setCellValueFactory((TableColumn.CellDataFeatures<DownloadThread, Integer> download) -> download.getValue().getDownloadMetadata().getDownloadIDProperty());

        TableColumn<DownloadThread, String> urlColumn = new TableColumn<>("URL");
        urlColumn.setMinWidth(200);
        urlColumn.setCellValueFactory((TableColumn.CellDataFeatures<DownloadThread, String> download) -> download.getValue().getDownloadMetadata().getUrlProperty());

        TableColumn<DownloadThread, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setMinWidth(200);
        statusColumn.setCellValueFactory((TableColumn.CellDataFeatures<DownloadThread, String> download) -> download.getValue().getDownloadMetadata().getStatusProperty());

        TableColumn<DownloadThread, String> filenameColumn = new TableColumn<>("Filename");
        filenameColumn.setMinWidth(150);
        filenameColumn.setCellValueFactory((TableColumn.CellDataFeatures<DownloadThread, String> download) -> download.getValue().getDownloadMetadata().getFilenameProperty());

        TableColumn<DownloadThread, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setMinWidth(100);
        sizeColumn.setCellValueFactory((TableColumn.CellDataFeatures<DownloadThread, String> download) -> download.getValue().getDownloadMetadata().getSizeProperty());

        TableColumn<DownloadThread, String> acceleratedColumn = new TableColumn<>("Accelerated");
        acceleratedColumn.setMinWidth(50);
        acceleratedColumn.setCellValueFactory((TableColumn.CellDataFeatures<DownloadThread, String> download) -> download.getValue().getDownloadMetadata().getAcceleratedProperty());

        table = new TableView();
        table.setItems(downloadPool.getDownloadThreads());
        table.getColumns().addAll(idColumn, urlColumn, statusColumn, filenameColumn, sizeColumn, acceleratedColumn);

        Label urlLabel = new Label("URL:");

        TextField urlInput = new TextField();
        urlInput.setMinWidth(400);

        Button newDownload = new Button("Download");
        newDownload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                downloadPool.newDownload(urlInput.getText());
                urlInput.clear();
            }
        });

        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                downloadPool.pauseDownload(table.getSelectionModel().getSelectedItem());
            }
        });

        Button resumeButton = new Button("Resume");
        resumeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                downloadPool.resumeDownload(table.getSelectionModel().getSelectedItem());
            }
        });
        
        
        Button stopButton = new Button("Stop");
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                downloadPool.stopDownload(table.getSelectionModel().getSelectedItem());
            }
        });
        
        Button removeButton = new Button("Remove");
        removeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent eh) {
                downloadPool.removeDownload(table.getSelectionModel().getSelectedItem());
            }
        });
        
        HBox hBox = new HBox();
        hBox.getChildren().addAll(urlLabel, urlInput, newDownload);
        hBox.setSpacing(10);
        
        HBox buttonList=new HBox();
        buttonList.getChildren().addAll(pauseButton,stopButton,resumeButton,removeButton);
        buttonList.setSpacing(10);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(hBox,buttonList, table);
        Scene scene = new Scene(vBox);
        scene.getStylesheets().add("/resources/modena_dark.css");
        window.setScene(scene);
        window.show();
    }


}

