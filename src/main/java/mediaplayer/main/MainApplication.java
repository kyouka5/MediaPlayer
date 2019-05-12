package mediaplayer.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mediaplayer.controller.Controller;


public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mediaplayer.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        FXMLLoader playlistLoader = new FXMLLoader(getClass().getResource("/fxml/playlists.fxml"));
        playlistLoader.load();
        controller.setPlaylistController(playlistLoader.getController());
        controller.setPlaylistRoot(playlistLoader.getRoot());
        controller.setStage(primaryStage);
        primaryStage.setTitle("Media Player");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResource("/icons/music-note.png").toString()));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
