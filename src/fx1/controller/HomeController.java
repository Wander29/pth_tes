package fx1.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;
import fx1.application.Fx1;
import static fx1.controller.LoginController.tipoUtenteLoggato;
import static fx1.model.DbConnection.query;
import static fx1.model.DbConnection.resultSet;
import static fx1.model.DbConnection.statement;
import static fx1.model.DbConnection.statementPr;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.codec.digest.DigestUtils;

public class HomeController {
/**********************************************/
/*      DICHIARAZIONE VARIABILI               */
/**********************************************/
    @FXML private ResourceBundle resources;
    @FXML private URL location;
//Inizio
    @FXML private JFXHamburger btnMenuHamb;
    @FXML private Label lbl_titolo;
//SideMenu
    @FXML private AnchorPane sideMenu;
    @FXML private Pane paneInsUt;
    @FXML private Pane paneAlg;
    @FXML private Pane paneEsci;
//Contentiore principale
    @FXML private GridPane mainStage;
    @FXML private TabPane tabPane;
    @FXML private Tab tab1;
    @FXML private Tab tab2;
    @FXML private Tab tab3;
//Inserimento utente
    @FXML private JFXTextField txt_1_user;
    @FXML private JFXTextField txt_1_psw;
    @FXML private JFXPasswordField psw_1_psw;
    @FXML private JFXCheckBox check_1_viewPsw;
    @FXML private JFXTextField txt_1_cog;
    @FXML private JFXTextField txt_1_nome;
    @FXML private JFXComboBox<Label> cb_1_tipoUt;
    @FXML private JFXButton btn_1_ins;
//Algoritmo 
    @FXML private StackPane sp_piano;
    @FXML private JFXButton btnPrepara;
    @FXML private JFXButton btnStartAlg;
    @FXML private Pane panePointStart;
    @FXML private Pane panePointEnd;
    @FXML private JFXTextField txtStartX;
    @FXML private JFXTextField txtStartY;
    @FXML private JFXTextField txtEndX;
    @FXML private JFXTextField txtEndY;
    @FXML private Pane imgRefresh;
  
/****************************************/
//Stage
    public static Parent rootEvent, root;
    public Scene scene, scene_appo;
    public static Stage appStage;
//Var aiuto
    HamburgerSlideCloseTransition burgerTask;
    SingleSelectionModel<Tab> selectionModel;
    Boolean inserted = false;
//Var
    String user, psw, nome, cog, tipoUt, codiceNome, appo, permessi, string_temp;
    int codRel, codTipoUt, tab = 0;
//Init necessari globali
    Chart piano;
    
//NumeriField Filter
    UnaryOperator<Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
    
/**********************************************/
/*        FUNZIONI                            */
/**********************************************/
    void apriMenu(){
        burgerTask.setRate(burgerTask.getRate()*-1);
        burgerTask.play();
        
        TranslateTransition openNav = new TranslateTransition(new Duration(350), sideMenu);
        TranslateTransition spostaDxTabPane = new TranslateTransition(new Duration(350), mainStage);
        openNav.setToX(0);
        TranslateTransition closeNav = new TranslateTransition(new Duration(350), sideMenu);
        TranslateTransition spostaSxTabPane = new TranslateTransition(new Duration(350), mainStage);
        spostaSxTabPane.setToX(0);
        
        if(sideMenu.getTranslateX()!=0){
            spostaDxTabPane.setToX(sideMenu.getWidth());
            openNav.play();
            spostaDxTabPane.play();
            tabPane.setEffect(new GaussianBlur());
            tabPane.setDisable(true);
        }else{
            closeNav.setToX(-(sideMenu.getWidth()));
            closeNav.play();
            spostaSxTabPane.play();
            tabPane.setDisable(false);
            tabPane.setEffect(null);
        }
    }
    
    void apriTab(Tab t){
        apriMenu();
        selectionModel.select(t); 
    }
    
    void esciDialog() throws IOException{
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("LOGOUT");
        alert.setHeaderText("Stai per uscire..");
        alert.setContentText(".. sei certo di volerlo fare?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            logout();
        } else { }
    }
    
    void logout() throws IOException{        
        tipoUtenteLoggato = 0;
        root = FXMLLoader.load(getClass().getResource("/fx1/view/Login.fxml"));
        scene = new Scene(root);
        scene_appo = tabPane.getScene(); //appoggio 
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
    
    public int getIntPoint(JFXTextField var){
        string_temp = var.getText();
        var.setText("");
        if(string_temp.isEmpty())
            return -1;
        else
            return Integer.parseInt(string_temp);
    }
    
    private void fade(Node n, int from, int to){
        FadeTransition ft = new FadeTransition(Duration.millis(300), n);
        ft.setFromValue(from);
        ft.setToValue(to);
        ft.play();
    }
    
    private void svuotaCampiAlg(){
        txtStartX.setText("");
        txtStartY.setText("");
        txtEndX.setText("");
        txtEndY.setText("");
    }
    
    private void svuotaCampiInsUt(){
        inserted = false;
        txt_1_user.setText("");
        txt_1_psw.setText("");
        txt_1_nome.setText("");
        txt_1_cog.setText("");
        cb_1_tipoUt.setValue(null);
    }

/**********************************************/
/*          EVENTI                            */
/**********************************************/
    @FXML
    void handleBtnStartMenu(MouseEvent event) {
        apriMenu();
    }
    
    @FXML
    void handleBtnHamb(MouseEvent event) {
        apriMenu();
    }
    
    @FXML
    void handleMenu_esci(ActionEvent event) throws IOException {
        esciDialog();
    }
    
    @FXML 
    void handlePaneAlg(MouseEvent event) {
        if(tab != 2)
            svuotaCampiAlg();
        lbl_titolo.setText("CAMMINO MINIMO");
        if (piano == null)
            piano = new Chart(sp_piano);
        apriTab(tab2);
        tab = 2;
    }

    @FXML
    void handlePaneInsUt(MouseEvent event) {
        if (tab != 1)
            svuotaCampiInsUt();
        cb_1_tipoUt.getItems().clear();
        query = "SELECT nomeTipo FROM tipo_utente WHERE CodTipoUt = 1";
        try {
            statement = Fx1.gatto.getConnection().createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                appo = resultSet.getString(1);
                cb_1_tipoUt.getItems().add(new Label(appo));
            }
        } catch (Exception e) {}
        lbl_titolo.setText("INSERIMENTO UTENTE");
        apriTab(tab1);
        tab = 1;
    }
    
    @FXML
    void handlePaneEsci(MouseEvent event) throws IOException {
        esciDialog();
    }
    
    @FXML
    void handleBtn_1(ActionEvent event) {
        inserted = false;
        user = txt_1_user.getText();
        psw = txt_1_psw.getText();
        nome = txt_1_nome.getText();
        cog = txt_1_cog.getText();
        tipoUt = cb_1_tipoUt.getValue().getText();
        if (!user.isEmpty() && !psw.isEmpty() && !nome.isEmpty() && 
                !cog.isEmpty() && !tipoUt.isEmpty()) {
            psw = DigestUtils.md5Hex(psw).toUpperCase();   
            
            query = "INSERT INTO " + tipoUt + " (nome, cognome) VALUES (?, ?)";
            try {
                statementPr = Fx1.gatto.getConnection().prepareStatement(query);
                statementPr.setString(1, nome);
                statementPr.setString(2, cog);
                if (statementPr.executeUpdate() > 0) {
                    switch(tipoUt){
                        case "utente": 
                            codiceNome = "CodUt";
                            break;
                        default:
                            break;
                    }
                    
                    query = "SELECT " + codiceNome + " FROM " + tipoUt + 
                            " WHERE nome = '" + nome + "' AND cognome = '" + 
                            cog + "'";
                    try {
                        statement = Fx1.gatto.getConnection().createStatement();
                        resultSet = statement.executeQuery(query);
                        if (resultSet.next()) {
                            codRel = resultSet.getInt(1);
                            
                            query = "SELECT CodTipoUt FROM tipo_utente WHERE"
                                    + " nomeTipo = '" + tipoUt + "'";
                            try {
                                statement = Fx1.gatto.getConnection().createStatement();
                                resultSet = statement.executeQuery(query);
                                if (resultSet.next()) {
                                    codTipoUt = resultSet.getInt(1);
                                    
                                    query = "INSERT INTO users (nomeUt, psw, "
                                            + "FkTipoUtente, FkCodRel) VALUES "
                                            + "(?, ?, ?, ?)";
                                    try {
                                        statementPr = Fx1.gatto.getConnection().prepareStatement(query);
                                        statementPr.setString(1, user);
                                        statementPr.setString(2, psw);
                                        statementPr.setInt(3, codTipoUt);
                                        statementPr.setInt(4, codRel);
                                        if (statementPr.executeUpdate() > 0) {
                                            inserted = true;
                                        }  
                                    } catch (Exception e) {};
                                }
                            } catch (Exception e) {}
                        }
                    } catch (Exception e) {}
                    
                } else { }
            } catch (Exception e) { }
            
            if (inserted) {
                Alert alertAbout = new Alert(AlertType.INFORMATION);
                alertAbout.setTitle("SUCCESSO");
                alertAbout.setHeaderText(null);
                alertAbout.setContentText("Utente Inserito correttamente\n\nUsername: " + user);
                alertAbout.showAndWait();
                
            //SVUOTA CAMPI FORM
                svuotaCampiInsUt();
            } else {
                Alert alertAbout = new Alert(AlertType.INFORMATION);
                alertAbout.setTitle("ERRORE");
                alertAbout.setHeaderText(null);
                alertAbout.setContentText("Utente NON Inserito\nRiprovare");
                alertAbout.showAndWait();
            }
        }
    }
    
    @FXML
    void handleBtnPrepara(ActionEvent event) {
        piano.refresh();
        if(imgRefresh.isVisible()){
            imgRefresh.setVisible(false);
        }
        if (!Chart.base_exists){
            btnStartAlg.setVisible(true);
            fade(btnStartAlg, 0, 1);
            btnStartAlg.setDisable(false);
            panePointStart.setVisible(true);
            fade(panePointStart, 0, 1);
            panePointEnd.setVisible(true);
            fade(panePointEnd, 0, 1);
        }
        piano.preparaAlg(
                getIntPoint(txtStartX), getIntPoint(txtStartY),
                getIntPoint(txtEndX), getIntPoint(txtEndY) 
        );
    }

    @FXML
    void handleBtnStartAlg(ActionEvent event) {
        piano.refresh();
        svuotaCampiAlg();
        imgRefresh.setVisible(true);
        fade(imgRefresh, 0, 1);
        piano.startAlg();
    }
    
    @FXML
    void handleImgRefresh(MouseEvent event) {
        fade(imgRefresh, 1, 0);
        imgRefresh.setVisible(false);
        piano.refresh();
    }
    
    @FXML
    void handleAboutClick() {
        Alert alertAbout = new Alert(AlertType.INFORMATION);
        alertAbout.setTitle("Tesina 5°AIA");
        alertAbout.setHeaderText(null);
        alertAbout.setContentText("Ludovico Venturi \n©2018 | v.0.1");
        alertAbout.showAndWait();
    }

/**********************************************/
/*          INIZIALIZZAZIONE                  */
/**********************************************/  
    @FXML
    void initialize() {
    //NumericField
        TextFormatter<String> textFormatter1 = new TextFormatter<>(filter);
        TextFormatter<String> textFormatter2 = new TextFormatter<>(filter);
        TextFormatter<String> textFormatter3 = new TextFormatter<>(filter);
        TextFormatter<String> textFormatter4 = new TextFormatter<>(filter);
        txtStartX.setTextFormatter(textFormatter1);
        txtStartY.setTextFormatter(textFormatter2);
        txtEndX.setTextFormatter(textFormatter3);
        txtEndY.setTextFormatter(textFormatter4);
        
    //Show Password
        txt_1_psw.setManaged(false);
        txt_1_psw.setVisible(false);
        
        txt_1_psw.managedProperty().bind(check_1_viewPsw.selectedProperty());
        txt_1_psw.visibleProperty().bind(check_1_viewPsw.selectedProperty());
        
        psw_1_psw.managedProperty().bind(check_1_viewPsw.selectedProperty().not());
        psw_1_psw.visibleProperty().bind(check_1_viewPsw.selectedProperty().not());
        
        txt_1_psw.textProperty().bindBidirectional(psw_1_psw.textProperty());
    
    //Hamburger
        burgerTask = new HamburgerSlideCloseTransition(btnMenuHamb);
        burgerTask.setRate(-1);
    
    //Gestione TAB Menù
        selectionModel = tabPane.getSelectionModel();
        
    //Gestione Utenze, sfetcho i permessi dell'utente loggato dal db
        permessi = "";
        query = "SELECT permessi FROM tipo_utente WHERE"
                + " CodTipoUt = " + tipoUtenteLoggato;
        try {
            statement = Fx1.gatto.getConnection().createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                permessi = resultSet.getString(1);
            }
        } catch(Exception e) {}
    
        if (!permessi.contains("INS")) {
            paneInsUt.setDisable(true);
        }
        if (!permessi.contains("VIEW")) {
            paneAlg.setDisable(true);
        }
        
    //Bottoni Piano cartesiano
        btnStartAlg.setDisable(true);
        panePointStart.setVisible(false);
        panePointEnd.setVisible(false);
    }
}
