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
import mediaplayer.util.guice.PersistenceModule;
import mediaplayer.dao.PlaylistDAO;
import mediaplayer.model.Item;
import mediaplayer.model.Playlist;
import mediaplayer.util.TimeFormatter;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private MediaPlayer mediaPlayer;

    private Integer previousVolume;

    @FXML
    private MediaView mediaView;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Slider durationSlider;

    @FXML
    private HBox menuBar;

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
    private Text itemName;

    @FXML
    private ToggleButton repeatToggle;

    @FXML
    private ToggleButton shuffleToggle;

    private PlaylistController playlistController;

    private Scene playlistScene;

    private PlaylistDAO playlistDAO;
    private ItemDAO itemDAO;

    private ObjectProperty<String> selectedMedia = new SimpleObjectProperty<>();
    private ObjectProperty<String> selectedPlaylistName = new SimpleObjectProperty<>();

    private static Logger logger = LoggerFactory.getLogger(Controller.class);


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPlaylistController(PlaylistController playlistController) {
        this.playlistController = playlistController;
    }

    public void setPlaylistRoot(Parent playlistRoot) {
        playlistScene = new Scene(playlistRoot);
    }

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
                        playOrPauseMedia();
                    }
                }
            }
        });
    }

    private void setControls(MediaPlayer mediaPlayer) {
        fitToSize();

        volumeSlider.setValue(mediaPlayer.getVolume() * 100);

        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            }
        });

        durationSlider.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mediaPlayer.seek(Duration.seconds(durationSlider.getValue()));
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                durationSlider.setValue(newValue.toSeconds());
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateTime();
            }
        });

        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                durationSlider.setMax((mediaPlayer.getTotalDuration().toSeconds()));
                TimeFormatter timeUtil = new TimeFormatter();
                totalTime.setText(timeUtil.timeToString(mediaPlayer.getTotalDuration()));
            }
        });

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if (!mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)) {
                    if (repeatToggle.isSelected()) {
                        playItem(mediaPlayer.getMedia().getSource());
                    } else {
                        playNextMedia();
                        mediaPlayer.seek(Duration.ZERO);
                    }
                }
            }
        });

    }

    private void fitToSize() {
        DoubleProperty width = mediaView.fitWidthProperty();
        DoubleProperty height = mediaView.fitHeightProperty();
        width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height").subtract(menuBar.getHeight()).subtract(durationSlider.getHeight()));
    }

    private void updateTime() {
        if (mediaPlayer != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    TimeFormatter timeUtil = new TimeFormatter();
                    timeElapsed.setText(timeUtil.timeToString(mediaPlayer.getCurrentTime()));
                }
            });
        }
    }

    private void playOrPauseMedia() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED) || mediaPlayer.getStatus().equals(MediaPlayer.Status.STOPPED)) {
                logger.info("The media player has been RESUMED");
                mediaPlayer.play();
                playPauseButton.setImage(new Image(getClass().getResource("/icons/pause-button.png").toString()));
            } else {
                logger.info("The media player has been PAUSED");
                mediaPlayer.pause();
                playPauseButton.setImage(new Image(getClass().getResource("/icons/play-button-1.png").toString()));
            }
        } else {
            logger.warn("Unable to PLAY or PAUSE - no media has been selected");
        }
    }

    @FXML
    private void playOrPauseMedia(javafx.event.ActionEvent event) {
        playOrPauseMedia();
    }

    @FXML
    private void stopMedia(javafx.event.ActionEvent event) {
        if (mediaPlayer != null) {
            logger.info("The media player has been STOPPED");
            mediaPlayer.stop();
            playPauseButton.setImage(new Image(getClass().getResource("/icons/play-button-1.png").toString()));
        } else {
            logger.warn("Unable to STOP - no media has been selected");
        }
    }

    @FXML
    private void previousMedia(javafx.event.ActionEvent event) {
        if (mediaPlayer != null) {
            Playlist playlist = playlistDAO.getPlaylistByName(selectedPlaylistName.getValue());
            playlistDAO.updatePlaylistContents(playlist, itemDAO.getItemsByPlaylist(playlist));
            Item previousItem = playlist.getPreviousItem(itemDAO.getItemByPath(playlist, selectedMedia.getValue()));
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
    private void nextMedia(javafx.event.ActionEvent event) {
        if (mediaPlayer != null) {
            playNextMedia();
        } else {
            logger.warn("Unable to play the NEXT media - no media has been selected");
        }
    }

    private void playNextMedia() {
        Playlist playlist = playlistDAO.getPlaylistByName(selectedPlaylistName.getValue());
        playlistDAO.updatePlaylistContents(playlist, itemDAO.getItemsByPlaylist(playlist));
        Item nextItem = playlist.getNextItem(itemDAO.getItemByPath(playlist, selectedMedia.getValue()));
        if (nextItem != null) {
            logger.info("Started the NEXT media on the list");
            selectedMedia.set(nextItem.getPath());
        } else {
            mediaPlayer.stop();
            logger.info("This is the LAST media on the playlist, hence it is not possible to get the next one");
        }
    }

    private void playItem(String path) {
        logger.info("Set to play: " + path);
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        Media media = new Media(path);
        mediaPlayer = new MediaPlayer(media);
        Item item = itemDAO.getItemByPath(playlistDAO.getPlaylistByName(selectedPlaylistName.getValue()), path);
        item.incrementViews();
        itemDAO.update(item);
        if (item.getArtist() != null && item.getYear() != 0 && item.getAlbum() != null && item.getTitle() != null) {
            stage.setTitle(item.getArtist() + " < " + item.getYear() + " < " + item.getAlbum() + " < " + item.getTitle());
            itemName.setText(item.getTitle());
        } else {
            stage.setTitle("Media Player");
            itemName.setText("");
        }
        mediaPlayer.getMedia().getMetadata().addListener(new MapChangeListener<String, Object>() {
            @Override
            public void onChanged(Change<? extends String, ?> change) {
                if (change.wasAdded()) {
                    if (change.getKey().equals("image"))
                        albumCover.setImage((Image) change.getValueAdded());
                }
            }
        });
        mediaView.setMediaPlayer(mediaPlayer);
        setControls(mediaPlayer);
        mediaPlayer.play();
        playPauseButton.setImage(new Image(getClass().getResource("/icons/pause-button.png").toString()));
        playlistController.updateMostPlayed();
        itemDAO.flush();
    }

    @FXML
    private void muteUnmuteMedia(javafx.event.ActionEvent event) {
        if (mediaPlayer != null) {
            if (volumeSlider.valueProperty().intValue() != 0) {
                logger.info("The media player has been MUTED");
                previousVolume = volumeSlider.valueProperty().intValue();
                volumeSlider.setValue(0);
                muteUnmuteButton.setImage(new Image(getClass().getResource("/icons/volume-level.png").toString()));
            } else {
                logger.info("The media player has been UNMUTED");
                volumeSlider.setValue(previousVolume);
                muteUnmuteButton.setImage(new Image(getClass().getResource("/icons/speaker.png").toString()));
            }
        } else {
            logger.warn("Unable to MUTE or UNMUTE - no media has been selected");
        }
    }

    @FXML
    private void shuffle(javafx.event.ActionEvent event) {
        Playlist playlist = playlistDAO.getPlaylistByName(selectedPlaylistName.getValue());
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
    private void toggleFullscreen(javafx.event.ActionEvent event) {
        if (!stage.isFullScreen()) {
            logger.info("Switched to Full Screen Mode");
            stage.setFullScreen(true);
        } else {
            logger.info("Switched to Windowed Mode");
            stage.setFullScreen(false);
        }
    }

    @FXML
    private void showPlaylists(javafx.event.ActionEvent event) {
        logger.info("Opened the playlist management window");
        Stage stage = new Stage();
        stage.setScene(playlistScene);
        stage.setResizable(false);
        stage.setTitle("Manage playlists");
        stage.getIcons().add(new Image(getClass().getResource("/icons/music-note.png").toString()));
        stage.show();
        Bindings.bindBidirectional(selectedMedia, playlistController.selectedFile());
        Bindings.bindBidirectional(selectedPlaylistName, playlistController.selectedPlaylistName());

    }

}
