package fx1.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import static fx1.model.DbConnection.*;
import fx1.application.Fx1;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.codec.digest.DigestUtils;

public class LoginController {
/**********************************************/
/*      DICHIARAZIONE VARIABILI               */
/**********************************************/
    String email, psw;
    public static Parent rootEvent, root;
    public Scene scene, scene_appo;
    public static Stage appStage;
    public static int tipoUtenteLoggato;

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private JFXButton btnLogin;
    @FXML private JFXButton btnSign;
    @FXML private JFXTextField txtEmail;
    @FXML private JFXPasswordField txtPsw;
    @FXML private JFXButton btnOspite;

    @FXML
    void handleBtnOspite(ActionEvent event) throws IOException {
        tipoUtenteLoggato = 2; //2 = ospite sul mio db
        goHome();
    }
    
    @FXML
    void handleBtnLogin(ActionEvent event) {
        email = txtEmail.getText();
        psw = txtPsw.getText();
        if (!email.isEmpty() && !psw.isEmpty()) {
            psw = DigestUtils.md5Hex(psw);     
            query = "SELECT u.FkTipoUtente FROM users AS u WHERE u.nomeUt = '" + email 
                + "' AND u.psw = '" + psw + "'";
            try {
                statement = Fx1.gatto.getConnection().createStatement();
                resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    tipoUtenteLoggato = resultSet.getInt(1);
                    goHome();
                } else {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Autenticazione FAIL");
                    alert.setHeaderText("Non è stato possibile effettuare l'accesso");
                    alert.setContentText("Controlla le credenziali e riprova");
                    alert.showAndWait();
                }
            } catch (Exception e) {  }
        }
    }    
    
    void goHome() throws IOException{
        root = FXMLLoader.load(getClass().getResource("/fx1/view/Home.fxml"));
        scene = new Scene(root);
        scene_appo =  txtEmail.getScene();
        appStage = (Stage) scene_appo.getWindow();
        rootEvent = scene_appo.getRoot();

        Timeline tick0 = new Timeline();
        tick0.setCycleCount(Timeline.INDEFINITE);
        tick0.getKeyFrames().add(
                new KeyFrame(new Duration(10), (ActionEvent t) -> {
                    rootEvent.setOpacity(rootEvent.getOpacity()-0.01);
                    if(rootEvent.getOpacity()<0.01){ //30 divided by 0.01 equals 3000 so you take the duration and divide it be the opacity to get your transition time in milliseconds
                        appStage.setScene(scene);
                        appStage.setResizable(false);
                        appStage.show();
                        tick0.stop();
                    }
        }));
        tick0.play();
    }

    @FXML
    void initialize() {
        assert btnLogin != null : "fx:id=\"btnLogin\" was not injected: check your FXML file 'Login.fxml'.";
        assert btnSign != null : "fx:id=\"btnSign\" was not injected: check your FXML file 'Login.fxml'.";
        assert txtEmail != null : "fx:id=\"txtEmail\" was not injected: check your FXML file 'Login.fxml'.";
        assert txtPsw != null : "fx:id=\"txtPsw\" was not injected: check your FXML file 'Login.fxml'.";

    }
}
