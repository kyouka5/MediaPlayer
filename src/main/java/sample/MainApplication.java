package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.controller.Controller;
import sample.controller.PlaylistController;
import sample.dao.PlaylistDAO;
import sample.dao.PlaylistDAOFactory;


public class MainApplication extends Application {

    private PlaylistDAO playlistDAO;

    @Override
    public void start(Stage primaryStage) throws Exception{
        playlistDAO = PlaylistDAOFactory.getInstance().createPlaylistDAO();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        FXMLLoader playlistloader = new FXMLLoader(getClass().getResource("/fxml/listview.fxml"));
        playlistloader.load();
        playlistloader.<PlaylistController>getController().initData(playlistDAO);
        controller.setPlaylistController(playlistloader.getController());
        controller.setPlaylistRoot(playlistloader.getRoot());
        primaryStage.setTitle("Media Player");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        controller.setStage(primaryStage);
        primaryStage.getIcons().add(new Image(getClass().getResource("/style/music-note.png").toString()));
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                PlaylistDAOFactory.getInstance().close();
                Platform.exit();
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
