package mediaplayer.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mediaplayer.dao.ItemDAO;
import mediaplayer.dao.PersistenceModule;
import mediaplayer.dao.PlaylistDAO;
import mediaplayer.model.Item;
import mediaplayer.model.Playlist;
import mediaplayer.utils.TimeUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private MediaPlayer mediaPlayer;

    private int previousVolume;

    @FXML
    private MediaView mediaView;

    @FXML
    private Slider slider;

    @FXML
    private Slider seekSlider;

    @FXML
    private HBox hBox;

    @FXML
    private Text timeElapsed;

    @FXML
    private Text totalTime;

    private Stage stage;

    @FXML
    private ImageView albumCover;

    @FXML
    private ImageView playPauseButton;

    @FXML
    private ImageView muteUnmuteButton;

    @FXML
    private Text metadata;

    @FXML
    ToggleButton repeatToggle;

    @FXML
    ToggleButton shuffleToggle;


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private PlaylistController playlistController;

    public void setPlaylistController(PlaylistController playlistController) {
        this.playlistController = playlistController;
    }

    private Scene playlistScene;

    public void setPlaylistRoot(Parent playlistRoot) {
        playlistScene = new Scene(playlistRoot);
    }

    private ObjectProperty<String> selectedMedia = new SimpleObjectProperty<>();
    private ObjectProperty<String> selectedPlaylistName = new SimpleObjectProperty<>();

    private PlaylistDAO playlistDAO;
    private ItemDAO itemDAO;

    private static Logger logger = LoggerFactory.getLogger(Controller.class);

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Injector injector = Guice.createInjector(new PersistenceModule("mediaplayer"));
        playlistDAO = injector.getInstance(PlaylistDAO.class);
        itemDAO = injector.getInstance(ItemDAO.class);

        selectedMedia.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                playItem(newValue);
            }
        });

        mediaView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mediaPlayer != null) {
                    if (mouseEvent.getClickCount() == 1 && !mediaPlayer.getCurrentTime().equals(mediaPlayer.getTotalDuration())) {
                        playOrPauseVideo();
                    }
                }
            }
        });
    }

    private void bindControls(MediaPlayer mediaPlayer) {
        fitToSize();

        slider.setValue(mediaPlayer.getVolume() * 100);

        slider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(slider.getValue() / 100);
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                seekSlider.setValue(newValue.toSeconds());
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateTime();
            }
        });

        seekSlider.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mediaPlayer.seek(Duration.seconds(seekSlider.getValue()));
            }
        });

        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                seekSlider.setMax((mediaPlayer.getTotalDuration().toSeconds()));
                TimeUtil timeUtil = new TimeUtil();
                totalTime.setText(timeUtil.timeToString(mediaPlayer.getTotalDuration()));
            }
        });

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if (!mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)) {
                    if (repeatToggle.isSelected()) {
                        playItem(mediaPlayer.getMedia().getSource());
                    }
                    else {
                        playNextVideo();
                        mediaPlayer.seek(Duration.ZERO);
                    }
                }
            }
        });

    }

    private void updateTime() {
        if (mediaPlayer != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    TimeUtil timeUtil = new TimeUtil();
                    timeElapsed.setText(timeUtil.timeToString(mediaPlayer.getCurrentTime()));
                }
            });
        }
    }

    private void playOrPauseVideo() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED) || mediaPlayer.getStatus().equals(MediaPlayer.Status.STOPPED)) {
                logger.info("The media player has been RESUMED");
                mediaPlayer.play();
                playPauseButton.setImage(new Image(getClass().getResource("/style/pause-button.png").toString()));
            } else {
                logger.info("The media player has been PAUSED");
                mediaPlayer.pause();
                playPauseButton.setImage(new Image(getClass().getResource("/style/play-button-1.png").toString()));
            }
        } else {
            logger.warn("Unable to PLAY or PAUSE - no media has been selected");
        }
    }

    @FXML
    private void playOrPauseVideo(javafx.event.ActionEvent event) {
        playOrPauseVideo();
    }

    @FXML
    private void stopVideo(javafx.event.ActionEvent event) {
        if (mediaPlayer != null) {
            logger.info("The media player has been STOPPED");
            mediaPlayer.stop();
            playPauseButton.setImage(new Image(getClass().getResource("/style/play-button-1.png").toString()));
        } else {
            logger.warn("Unable to STOP - no media has been selected");
        }
    }

    @FXML
    private void muteUnmuteVideo(javafx.event.ActionEvent event) {
        if (mediaPlayer != null) {
            if (slider.valueProperty().intValue() != 0) {
                logger.info("The media player has been MUTED");
                previousVolume = slider.valueProperty().intValue();
                slider.setValue(0);
                muteUnmuteButton.setImage(new Image(getClass().getResource("/style/volume-level.png").toString()));
            } else {
                logger.info("The media player has been UNMUTED");
                slider.setValue(previousVolume);
                muteUnmuteButton.setImage(new Image(getClass().getResource("/style/speaker.png").toString()));
            }
        } else {
            logger.warn("Unable to MUTE or UNMUTE - no media has been selected");
        }
    }

    @FXML
    private void previousVideo(javafx.event.ActionEvent event) {
        if (mediaPlayer != null) {
            Playlist playlist = playlistDAO.readPlaylistByName(selectedPlaylistName.getValue());
            Item previousItem = playlist.getPreviousItem(playlist.getItemByPath(selectedMedia.getValue()));
            if (previousItem != null) {
                logger.info("Started the PREVIOUS media on the list");
                selectedMedia.set(previousItem.getPath());
            } else {
                logger.info("This is the FIRST media on the playlist, hence it is not possible to get the previous one");
            }
        } else {
            logger.warn("Unable to play the PREVIOUS media - no media has been selected");
        }
    }

    @FXML
    private void nextVideo(javafx.event.ActionEvent event) {
        if (mediaPlayer != null) {
            playNextVideo();
        } else {
            logger.warn("Unable to play the NEXT media - no media has been selected");
        }
    }

    private void playNextVideo() {
        Playlist playlist = playlistDAO.readPlaylistByName(selectedPlaylistName.getValue());
        Item nextItem = playlist.getNextItem(playlist.getItemByPath(selectedMedia.getValue()));
        if (nextItem != null) {
            logger.info("Started the NEXT media on the list");
            selectedMedia.set(nextItem.getPath());
        } else {
            mediaPlayer.stop();
            logger.info("This is the LAST media on the playlist, hence it is not possible to get the next one");
        }
    }

    @FXML
    private void shuffle(javafx.event.ActionEvent event) {
        Playlist playlist = playlistDAO.readPlaylistByName(selectedPlaylistName.getValue());
        if (shuffleToggle.isSelected()) {
            playlist.shufflePlaylist();
        } else {
            playlist.unshufflePlaylist();
        }
    }

    @FXML
    private void repeat(javafx.event.ActionEvent event) {
        if (repeatToggle.isSelected()) {
            logger.info("Repeat OFF");
        } else {
            logger.info("Repeat ON");
        }
    }

    @FXML
    private void togglePlaylists(javafx.event.ActionEvent event) {
        logger.info("Opened the playlist management window");
        Stage stage = new Stage();
        stage.setScene(playlistScene);
        stage.setResizable(false);
        stage.setTitle("Manage playlists");
        stage.getIcons().add(new Image(getClass().getResource("/style/music-note.png").toString()));
        stage.show();
        Bindings.bindBidirectional(selectedMedia, playlistController.selectedFile());
        Bindings.bindBidirectional(selectedPlaylistName, playlistController.selectedPlaylistName());

    }

    @FXML
    private void toggleFullscreen(javafx.event.ActionEvent event) {
        if (!stage.isFullScreen()) {
            logger.info("Switched to Full Screen Mode");
            stage.setFullScreen(true);
        } else {
            logger.info("Switched to Windowed Mode");
            stage.setFullScreen(false);
        }
    }

    private void fitToSize() {
        DoubleProperty width = mediaView.fitWidthProperty();
        DoubleProperty height = mediaView.fitHeightProperty();
        width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height").subtract(hBox.getHeight()).subtract(seekSlider.getHeight()));
    }

    private void playItem(String path) {
        logger.info("Set to play: " + path);
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        Media media = new Media(path);
        mediaPlayer = new MediaPlayer(media);
        Item item = itemDAO.getItemByPath(playlistDAO.readPlaylistByName(selectedPlaylistName.getValue()), path);
        item.incrementViews();
        itemDAO.update(item);
        if (item.getArtist() != null && item.getYear() != 0 && item.getAlbum() != null && item.getTitle() != null) {
            stage.setTitle(item.getArtist() + " < " + item.getYear() + " < " + item.getAlbum() + " < " + item.getTitle());
            metadata.setText(item.getTitle());
        } else {
            stage.setTitle("Media Player");
            metadata.setText("");
        }
        mediaPlayer.getMedia().getMetadata().addListener(new MapChangeListener<String, Object>(){
            @Override
            public void onChanged(Change<? extends String, ?> change) {
                if(change.wasAdded()){
                    if(change.getKey().equals("image"))
                        albumCover.setImage((Image)change.getValueAdded());
                }
            }
        });
        mediaView.setMediaPlayer(mediaPlayer);
        bindControls(mediaPlayer);
        mediaPlayer.play();
        playPauseButton.setImage(new Image(getClass().getResource("/style/pause-button.png").toString()));
    }

}
