package sample;

import com.google.inject.Guice;
import com.google.inject.Injector;
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
import sample.dao.ItemDAO;
import sample.dao.PersistenceModule;
import sample.dao.PlaylistDAO;


public class MainApplication extends Application {

    private PlaylistDAO playlistDAO;
    private ItemDAO itemDAO;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Injector injector = Guice.createInjector(new PersistenceModule("mediaplayer"));
        playlistDAO = injector.getInstance(PlaylistDAO.class);
        itemDAO = injector.getInstance(ItemDAO.class);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        FXMLLoader playlistloader = new FXMLLoader(getClass().getResource("/fxml/listview.fxml"));
        playlistloader.load();
        playlistloader.<PlaylistController>getController().initData(playlistDAO, itemDAO);
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
                playlistDAO.getEntityManager().close();
                itemDAO.getEntityManager().close();
                Platform.exit();
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
