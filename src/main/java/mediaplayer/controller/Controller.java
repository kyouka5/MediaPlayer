package mediaplayer.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import mediaplayer.dao.ItemDAO;
import mediaplayer.dao.PlaylistDAO;
import mediaplayer.model.Item;
import mediaplayer.model.Playlist;
import mediaplayer.util.TimeFormatter;
import mediaplayer.util.guice.PersistenceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;
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
    private ImageView playPauseButtonIcon;

    @FXML
    private ImageView muteUnmuteButtonIcon;

    @FXML
    private Text itemName;

    @FXML
    private ToggleButton repeatToggle;

    @FXML
    private ToggleButton shuffleToggle;

    private PlaylistController playlistController;

    private Parent playlistRoot;
    private Stage playlistStage;

    private PlaylistDAO playlistDAO;
    private ItemDAO itemDAO;

    /**
     * The name of the currently selected item.
     */
    private ObjectProperty<String> selectedItem = new SimpleObjectProperty<>();

    /**
     * The name of the currently selected playlist.
     */
    private ObjectProperty<String> selectedPlaylistName = new SimpleObjectProperty<>();

    private static Logger logger = LoggerFactory.getLogger(Controller.class);


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPlaylistController(PlaylistController playlistController) {
        this.playlistController = playlistController;
    }

    public void setPlaylistRoot(Parent playlistRoot) {
        this.playlistRoot = playlistRoot;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Injector injector = Guice.createInjector(new PersistenceModule("mediaplayer"));
        playlistDAO = injector.getInstance(PlaylistDAO.class);
        itemDAO = injector.getInstance(ItemDAO.class);

        selectedItem.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                playItem(newValue);
            }
        });

        mediaView.setOnMouseClicked(mouseEvent -> {
            if (mediaPlayer != null) {
                if (mouseEvent.getClickCount() == 1 && !mediaPlayer.getCurrentTime().equals(mediaPlayer.getTotalDuration())) {
                    playOrPauseMedia();
                }
            }
        });
    }

    /**
     * Sets volume controls and duration controls.
     *
     * @param mediaPlayer the {@link MediaPlayer} to be set
     */
    private void setControls(MediaPlayer mediaPlayer) {
        fitToSize();

        volumeSlider.setValue(mediaPlayer.getVolume() * 100);

        volumeSlider.valueProperty().addListener(observable -> mediaPlayer.setVolume(volumeSlider.getValue() / 100));

        durationSlider.setOnMouseClicked(event -> mediaPlayer.seek(Duration.seconds(durationSlider.getValue())));

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> durationSlider.setValue(newValue.toSeconds()));

        mediaPlayer.currentTimeProperty().addListener(observable -> updateTime());

        mediaPlayer.setOnReady(() -> {
            durationSlider.setMax((mediaPlayer.getTotalDuration().toSeconds()));
            TimeFormatter timeUtil = new TimeFormatter();
            totalTime.setText(timeUtil.formatTime(mediaPlayer.getTotalDuration()));
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            if (!mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)) {
                if (repeatToggle.isSelected()) {
                    playItem(mediaPlayer.getMedia().getSource());
                } else {
                    playNextMedia();
                    mediaPlayer.seek(Duration.ZERO);
                }
            }
        });

    }

    /**
     * Fits the size of the media to the size of the media player.
     */
    private void fitToSize() {
        DoubleProperty width = mediaView.fitWidthProperty();
        DoubleProperty height = mediaView.fitHeightProperty();
        width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height").subtract(menuBar.getHeight()).subtract(durationSlider.getHeight()));
    }

    /**
     * Updates the elapsed time of the currently playing media.
     */
    private void updateTime() {
        if (mediaPlayer != null) {
            Platform.runLater(() -> {
                TimeFormatter timeUtil = new TimeFormatter();
                timeElapsed.setText(timeUtil.formatTime(mediaPlayer.getCurrentTime()));
            });
        }
    }

    private void playOrPauseMedia() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)) {
                logger.info("The media player has been RESUMED");
                mediaPlayer.play();
                playPauseButtonIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/icons/pause-button.png")).toString()));
            } else if (mediaPlayer.getStatus().equals(MediaPlayer.Status.STOPPED)) {
                playItem(mediaPlayer.getMedia().getSource());
                playPauseButtonIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/icons/pause-button.png")).toString()));
            } else {
                logger.info("The media player has been PAUSED");
                mediaPlayer.pause();
                playPauseButtonIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/icons/play-button-1.png")).toString()));
            }
        } else {
            logger.warn("Unable to PLAY or PAUSE - no media has been selected");
        }
    }

    @FXML
    private void playOrPauseMedia(ActionEvent event) {
        playOrPauseMedia();
    }

    @FXML
    private void stopMedia(ActionEvent event) {
        if (mediaPlayer != null) {
            logger.info("The media player has been STOPPED");
            mediaPlayer.stop();
            playPauseButtonIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/icons/play-button-1.png")).toString()));
        } else {
            logger.warn("Unable to STOP - no media has been selected");
        }
    }

    @FXML
    private void previousMedia(ActionEvent event) {
        if (mediaPlayer != null) {
            Playlist playlist = playlistDAO.getPlaylistByName(selectedPlaylistName.getValue());
            if (playlist != null) {
                playlistDAO.updatePlaylistContents(playlist, itemDAO.getItemsByPlaylist(playlist));
                if (shuffleToggle.isSelected()) {
                    playlist.shufflePlaylist();
                } else {
                    playlist.unshufflePlaylist();
                }
                Item previousItem = playlist.getPreviousItem(itemDAO.getItemByPath(playlist, selectedItem.getValue()));
                if (previousItem != null) {
                    logger.info("Started the PREVIOUS media on the list");
                    selectedItem.set(previousItem.getPath());
                } else {
                    logger.info("This is the FIRST media on the playlist, hence it is not possible to get the previous one");
                }
            } else {
                logger.info("This playlist does not exist anymore.");
            }
        } else {
            logger.warn("Unable to play the PREVIOUS media - no media has been selected");
        }
    }

    @FXML
    private void nextMedia(ActionEvent event) {
        if (mediaPlayer != null) {
            playNextMedia();
        } else {
            logger.warn("Unable to play the NEXT media - no media has been selected");
        }
    }

    private void playNextMedia() {
        Playlist playlist = playlistDAO.getPlaylistByName(selectedPlaylistName.getValue());
        if (playlist != null) {
            playlistDAO.updatePlaylistContents(playlist, itemDAO.getItemsByPlaylist(playlist));
            if (shuffleToggle.isSelected()) {
                playlist.shufflePlaylist();
            } else {
                playlist.unshufflePlaylist();
            }
            Item nextItem = playlist.getNextItem(itemDAO.getItemByPath(playlist, selectedItem.getValue()));
            if (nextItem != null) {
                logger.info("Started the NEXT media on the list");
                selectedItem.set(nextItem.getPath());
            } else {
                mediaPlayer.stop();
                logger.info("This is the LAST media on the playlist, hence it is not possible to get the next one");
            }
        } else {
            logger.info("This playlist does not exist anymore.");
        }
    }

    /**
     * Sets an {@link Item} to play.
     *
     * @param path the path of the selected {@link Item}.
     */
    private void playItem(String path) {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        Media media = new Media(path);
        mediaPlayer = new MediaPlayer(media);
        Item item = itemDAO.getItemByPath(playlistDAO.getPlaylistByName(selectedPlaylistName.getValue()), path);
        if (item != null) {
            logger.info("Set to play: " + path);
            item.incrementViews();
            itemDAO.update(item);
            mediaView.setMediaPlayer(mediaPlayer);
            setControls(mediaPlayer);
            setMetadata(item);
            mediaPlayer.play();
            playPauseButtonIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/icons/pause-button.png")).toString()));
            playlistController.updateMostPlayed();
        } else {
            logger.info("This playlist does not exist anymore.");
        }
    }

    /**
     * Sets the metadata of an {@link Item} if it is available.
     *
     * @param item the {@link Item} to be played
     */
    private void setMetadata(Item item) {
        if (item.getArtist() != null && item.getYear().getValue() != 0 && item.getAlbum() != null && item.getTitle() != null) {
            stage.setTitle(MessageFormat.format("{0} < {1} < {2} < {3}", item.getArtist(), item.getYear(), item.getAlbum(), item.getTitle()));
            itemName.setText(item.getTitle());
        } else {
            stage.setTitle("Media Player");
            itemName.setText("");
        }

        mediaPlayer.getMedia().getMetadata().addListener((MapChangeListener<String, Object>) change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("image"))
                    albumCover.setImage((Image) change.getValueAdded());
            }
        });
    }

    @FXML
    private void muteUnmuteMedia(ActionEvent event) {
        if (mediaPlayer != null) {
            if (volumeSlider.valueProperty().intValue() != 0) {
                logger.info("The media player has been MUTED");
                previousVolume = volumeSlider.valueProperty().intValue();
                volumeSlider.setValue(0);
                muteUnmuteButtonIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/icons/volume-level.png")).toString()));
            } else {
                logger.info("The media player has been UNMUTED");
                volumeSlider.setValue(previousVolume);
                muteUnmuteButtonIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/icons/speaker.png")).toString()));
            }
        } else {
            logger.warn("Unable to MUTE or UNMUTE - no media has been selected");
        }
    }

    @FXML
    private void shuffle(ActionEvent event) {
        if (shuffleToggle.isSelected()) {
            logger.info("Shuffle ON");
        } else {
            logger.info("Shuffle OFF");
        }
    }

    @FXML
    private void repeat(ActionEvent event) {
        if (repeatToggle.isSelected()) {
            logger.info("Repeat ON");
        } else {
            logger.info("Repeat OFF");
        }
    }

    @FXML
    private void toggleFullscreen(ActionEvent event) {
        if (!stage.isFullScreen()) {
            logger.info("Switched to Full Screen Mode");
            stage.setFullScreen(true);
        } else {
            logger.info("Switched to Windowed Mode");
            stage.setFullScreen(false);
        }
    }

    @FXML
    private void showPlaylists(ActionEvent event) {
        if (playlistStage == null) {
            logger.info("Opened the playlist management window");
            playlistStage = new Stage();
            playlistStage.setScene(new Scene(playlistRoot));
            playlistStage.setResizable(false);
            playlistStage.setTitle("Manage playlists");
            playlistStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/icons/music-note.png")).toString()));
            playlistStage.show();
            Bindings.bindBidirectional(selectedItem, playlistController.getSelectedItem());
            Bindings.bindBidirectional(selectedPlaylistName, playlistController.getSelectedPlaylistName());
        } else {
            playlistStage.show();
            playlistStage.toFront();
            logger.info("The playlist management window is already open.");
        }

    }

}
