package fx1.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import fx1.model.*;
import javafx.scene.image.Image;

public class Fx1 extends Application {
    public static DbConnection gatto = new DbConnection();
    public static Parent root;
    public static Scene scene;
    
    @Override
    public void start(Stage stage) throws Exception {
        root = FXMLLoader.load(getClass().getResource("/fx1/view/Login.fxml"));
        
        scene = new Scene(root);
        stage.setScene(scene);
        stage.getIcons().add(new Image("/fx1/assets/mainB.png"));
        stage.setTitle("      Progetto Maturità | Ludovico Venturi | 5°AIA");
        stage.setResizable(false);
        stage.show();
        gatto.connetti();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
