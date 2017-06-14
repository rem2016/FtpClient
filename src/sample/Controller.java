package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML TextField serverAddress, account, password;
    @FXML
    ListView<String> listLocalFiles;
    @FXML
    ListView listServerFiles;
    @FXML Button btnLogin, btnUpload, btnDownload, btnRefresh;
    @FXML Label log;

    FTP ftp;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File thisDir = new File("./");
        ObservableList<String> obs = FXCollections.observableArrayList(thisDir.list());
        listLocalFiles.setItems(obs);
        ftp = null;

        btnUpload.setOnMouseClicked((MouseEvent event)->{
            String filename = listLocalFiles.getSelectionModel().getSelectedItem();
            if(ftp == null) {
                log.setText("请先登录后再试");
                return;
            }
            ftp.upload(filename);
        });

        btnRefresh.setOnMouseClicked((MouseEvent event)->{
            if(ftp == null) {
                log.setText("请先登录后再试");
                return;
            }
            ObservableList server_obs = FXCollections.observableArrayList(ftp.listDir().toArray());
            listServerFiles.setItems(server_obs);
        });

        btnDownload.setOnMouseClicked((MouseEvent event)->{
            String filename = (String)listServerFiles.getSelectionModel().getSelectedItem();
            if(ftp == null) {
                log.setText("请先登录后再试");
                return;
            }
            ftp.download(filename);
        });

        btnLogin.setOnMouseClicked((MouseEvent event)->{
            String username = account.getText(), psw = password.getText();
            if(username.length() == 0){
                username = "anonymous";
                psw = "";
            }
            String address = serverAddress.getText();
            ftp = new FTP(address, username, psw);
            if(ftp.isAlive())log.setText("登陆成功");
        });
    }
}
