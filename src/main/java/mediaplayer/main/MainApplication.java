package mediaplayer.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mediaplayer.controller.Controller;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader mediaPlayerLoader = loadFXML("/fxml/mediaplayer.fxml");
        FXMLLoader playlistLoader = loadFXML("/fxml/playlists.fxml");
        injectPlaylistController(mediaPlayerLoader, playlistLoader);
        initializeStage(primaryStage, mediaPlayerLoader);
    }

    private void initializeStage(Stage primaryStage, FXMLLoader mediaPlayerLoader) {
        Controller mediaPlayerController = mediaPlayerLoader.getController();
        mediaPlayerController.setStage(primaryStage);
        primaryStage.setTitle("Media Player");
        Scene scene = new Scene(mediaPlayerLoader.getRoot());
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/icons/music-note.png")).toString()));
        primaryStage.show();
    }

    private void injectPlaylistController(FXMLLoader mediaPlayerLoader, FXMLLoader playlistLoader) {
        Controller mediaPlayerController = mediaPlayerLoader.getController();
        mediaPlayerController.setPlaylistController(playlistLoader.getController());
        mediaPlayerController.setPlaylistRoot(playlistLoader.getRoot());
    }

    private FXMLLoader loadFXML(String pathToFxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(pathToFxml));
        loader.load();
        return loader;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
