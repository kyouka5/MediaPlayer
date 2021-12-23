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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private static final int MIN_VOLUME_PERCENTAGE = 0;
    private static final int MAX_VOLUME_PERCENTAGE = 100;

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

    private TimeFormatter timeFormatter;

    /**
     * The name of the currently selected item.
     */
    private final ObjectProperty<String> SELECTED_ITEM = new SimpleObjectProperty<>();

    /**
     * The name of the currently selected playlist.
     */
    private final ObjectProperty<String> SELECTED_PLAYLIST_NAME = new SimpleObjectProperty<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);


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
        timeFormatter = new TimeFormatter();
        injectDAOs();
        setSelectedItemToPlay();
        setPlayOrPauseOnClick();
    }

    private void setSelectedItemToPlay() {
        SELECTED_ITEM.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                playItem(newValue);
            }
        });
    }

    private void injectDAOs() {
        Injector injector = Guice.createInjector(new PersistenceModule("mediaplayer"));
        playlistDAO = injector.getInstance(PlaylistDAO.class);
        itemDAO = injector.getInstance(ItemDAO.class);
    }

    private void setPlayOrPauseOnClick() {
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
        adjustMediaSizeToFitMediaPlayer();
        setVolumeControls(mediaPlayer);
        setDurationControls(mediaPlayer);
        setJumpControls(mediaPlayer);
    }

    private void setJumpControls(MediaPlayer mediaPlayer) {
        mediaPlayer.setOnEndOfMedia(() -> playNextItemAfterCurrentHasEnded(mediaPlayer));
    }

    private void setDurationControls(MediaPlayer mediaPlayer) {
        durationSlider.setOnMouseClicked(event -> mediaPlayer.seek(Duration.seconds(durationSlider.getValue())));
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> durationSlider.setValue(newValue.toSeconds()));
        mediaPlayer.currentTimeProperty().addListener(observable -> updateElapsedTime());
        mediaPlayer.setOnReady(() -> setTotalDurationOfCurrentMedia(mediaPlayer));
    }

    private void setVolumeControls(MediaPlayer mediaPlayer) {
        volumeSlider.setValue(mediaPlayer.getVolume() * MAX_VOLUME_PERCENTAGE);
        volumeSlider.valueProperty().addListener(observable -> mediaPlayer.setVolume(volumeSlider.getValue() / MAX_VOLUME_PERCENTAGE));
    }

    private void setTotalDurationOfCurrentMedia(MediaPlayer mediaPlayer) {
        durationSlider.setMax((mediaPlayer.getTotalDuration().toSeconds()));
        totalTime.setText(timeFormatter.formatTime(mediaPlayer.getTotalDuration()));
    }

    private void playNextItemAfterCurrentHasEnded(MediaPlayer mediaPlayer) {
        if (!mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)) {
            if (repeatToggle.isSelected()) {
                playItem(mediaPlayer.getMedia().getSource());
            } else {
                playNextMedia();
                mediaPlayer.seek(Duration.ZERO);
            }
        }
    }

    /**
     * Fits the size of the media to the size of the media player.
     */
    private void adjustMediaSizeToFitMediaPlayer() {
        DoubleProperty width = mediaView.fitWidthProperty();
        DoubleProperty height = mediaView.fitHeightProperty();
        width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height").subtract(menuBar.getHeight()).subtract(durationSlider.getHeight()));
    }

    /**
     * Updates the elapsed time of the currently playing media.
     */
    private void updateElapsedTime() {
        Platform.runLater(() -> timeElapsed.setText(timeFormatter.formatTime(mediaPlayer.getCurrentTime())));
    }

    private void playOrPauseMedia() {
        if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)) {
            resumeMediaPlayer();
        } else if (mediaPlayer.getStatus().equals(MediaPlayer.Status.STOPPED)) {
            startMediaPlayer();
        } else {
            pauseMediaPlayer();
        }
    }

    private void resumeMediaPlayer() {
        LOGGER.info("The media player has been RESUMED");
        mediaPlayer.play();
        setButtonIcon(playPauseButtonIcon, "/icons/pause-button.png");
    }

    private void startMediaPlayer() {
        playItem(mediaPlayer.getMedia().getSource());
        setButtonIcon(playPauseButtonIcon, "/icons/pause-button.png");
    }

    private void pauseMediaPlayer() {
        LOGGER.info("The media player has been PAUSED");
        mediaPlayer.pause();
        setButtonIcon(playPauseButtonIcon, "/icons/play-button-1.png");
    }

    @FXML
    private void playOrPauseMedia(ActionEvent event) {
        if (mediaPlayer == null) {
            LOGGER.warn("Unable to PLAY or PAUSE - no media has been selected");
            return;
        }
        playOrPauseMedia();
    }

    @FXML
    private void stopMedia(ActionEvent event) {
        if (mediaPlayer == null) {
            LOGGER.warn("Unable to PLAY or PAUSE - no media has been selected");
            return;
        }
        stopMediaPlayer();
    }

    private void stopMediaPlayer() {
        LOGGER.info("The media player has been STOPPED");
        mediaPlayer.stop();
        setButtonIcon(playPauseButtonIcon, "/icons/play-button-1.png");
    }

    @FXML
    private void previousMedia(ActionEvent event) {
        if (mediaPlayer == null) {
            LOGGER.warn("Unable to play the PREVIOUS media - no media has been selected");
            return;
        }
        Optional<Playlist> selectedPlaylist = playlistDAO.getPlaylistByName(SELECTED_PLAYLIST_NAME.getValue());
        selectedPlaylist.ifPresentOrElse(playlist -> {
            Optional<List<Item>> itemsOfPlaylist = itemDAO.getItemsByPlaylist(playlist);
            itemsOfPlaylist.ifPresent(items -> playlistDAO.updatePlaylistContents(playlist, items));
            shufflePlaylistIfNeeded(playlist);
            Optional<Item> currentlyPlayingItem = itemDAO.getItemByPath(playlist, SELECTED_ITEM.getValue());
            currentlyPlayingItem.ifPresent(currentItem -> {
                Optional<Item> previousItem = playlist.getPreviousItem(currentItem);
                previousItem.ifPresentOrElse(item -> {
                    LOGGER.info("Started the PREVIOUS media on the list");
                    SELECTED_ITEM.set(item.getPath());
                }, () -> LOGGER.info("This is the FIRST media on the playlist, hence it is not possible to get the previous one"));
            });
        }, () -> LOGGER.info("This playlist does not exist anymore."));
    }

    private void shufflePlaylistIfNeeded(Playlist playlist) {
        if (shuffleToggle.isSelected()) {
            playlist.shufflePlaylist();
        } else {
            playlist.unshufflePlaylist();
        }
    }

    @FXML
    private void nextMedia(ActionEvent event) {
        if (mediaPlayer == null) {
            LOGGER.warn("Unable to play the NEXT media - no media has been selected");
            return;
        }
        playNextMedia();
    }

    private void playNextMedia() {
        Optional<Playlist> selectedPlaylist = playlistDAO.getPlaylistByName(SELECTED_PLAYLIST_NAME.getValue());
        selectedPlaylist.ifPresentOrElse(playlist -> {
            Optional<List<Item>> itemsOfPlaylist = itemDAO.getItemsByPlaylist(playlist);
            itemsOfPlaylist.ifPresent(items -> playlistDAO.updatePlaylistContents(playlist, items));
            shufflePlaylistIfNeeded(playlist);
            Optional<Item> selectedItem = itemDAO.getItemByPath(playlist, SELECTED_ITEM.getValue());
            selectedItem.ifPresent(currentItem -> {
                Optional<Item> nextItem = playlist.getNextItem(currentItem);
                nextItem.ifPresentOrElse(item -> {
                    LOGGER.info("Started the NEXT media on the list");
                    SELECTED_ITEM.set(item.getPath());
                }, () -> {
                    mediaPlayer.stop();
                    LOGGER.info("This is the LAST media on the playlist, hence it is not possible to get the next one");
                });
            });
        }, () -> LOGGER.info("This playlist does not exist anymore."));
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
        Optional<Playlist> selectedPlaylist = playlistDAO.getPlaylistByName(SELECTED_PLAYLIST_NAME.getValue());
        selectedPlaylist.ifPresentOrElse(playlist -> {
            Optional<Item> selectedItem = itemDAO.getItemByPath(playlist, path);
            selectedItem.ifPresent(item -> {
                LOGGER.info("Started playing: %s".formatted(item.getName()));
                item.incrementViews();
                itemDAO.update(item);
                playlistController.updateMostPlayed();
                mediaView.setMediaPlayer(mediaPlayer);
                setControls(mediaPlayer);
                setMetadata(item);
                setButtonIcon(playPauseButtonIcon, "/icons/pause-button.png");
                mediaPlayer.play();
            });
        }, () -> LOGGER.info("This playlist does not exist anymore."));
    }

    private void setButtonIcon(ImageView buttonIconToChange, String imagePath) {
        buttonIconToChange.setImage(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toString()));
    }

    /**
     * Sets the metadata of an {@link Item} if it is available.
     *
     * @param item the {@link Item} to be played
     */
    private void setMetadata(Item item) {
        setStageTitle(item);
        setAlbumCoverImageIfPresent();
    }

    private void setStageTitle(Item item) {
        if (item.getArtist() != null && item.getYear().getValue() != 0 && item.getAlbum() != null && item.getTitle() != null) {
            stage.setTitle(MessageFormat.format("{0} < {1} < {2} < {3}", item.getArtist(), item.getYear(), item.getAlbum(), item.getTitle()));
            itemName.setText(item.getTitle());
        } else {
            stage.setTitle("Media Player");
            itemName.setText("");
        }
    }

    private void setAlbumCoverImageIfPresent() {
        mediaPlayer.getMedia().getMetadata().addListener((MapChangeListener<String, Object>) change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("image"))
                    albumCover.setImage((Image) change.getValueAdded());
            }
        });
    }

    @FXML
    private void muteUnmuteMedia(ActionEvent event) {
        if (mediaPlayer == null) {
            LOGGER.warn("Unable to MUTE or UNMUTE - no media has been selected");
            return;
        }
        if (volumeSlider.valueProperty().intValue() != MIN_VOLUME_PERCENTAGE) {
            LOGGER.info("The media player has been MUTED");
            previousVolume = volumeSlider.valueProperty().intValue();
            volumeSlider.setValue(MIN_VOLUME_PERCENTAGE);
            setButtonIcon(muteUnmuteButtonIcon, "/icons/volume-level.png");
        } else {
            LOGGER.info("The media player has been UNMUTED");
            volumeSlider.setValue(previousVolume);
            setButtonIcon(muteUnmuteButtonIcon, "/icons/speaker.png");
        }
    }

    @FXML
    private void shuffle(ActionEvent event) {
        if (shuffleToggle.isSelected()) {
            LOGGER.info("Shuffle ON");
        } else {
            LOGGER.info("Shuffle OFF");
        }
    }

    @FXML
    private void repeat(ActionEvent event) {
        if (repeatToggle.isSelected()) {
            LOGGER.info("Repeat ON");
        } else {
            LOGGER.info("Repeat OFF");
        }
    }

    @FXML
    private void toggleFullscreen(ActionEvent event) {
        if (stage.isFullScreen()) {
            LOGGER.info("Switched to Windowed Mode");
            stage.setFullScreen(false);
        } else {
            LOGGER.info("Switched to Full Screen Mode");
            stage.setFullScreen(true);
        }
    }

    @FXML
    private void showPlaylists(ActionEvent event) {
        if (playlistStage != null) {
            LOGGER.info("The playlist management window is already open.");
            playlistStage.show();
            playlistStage.toFront();
            return;
        }
        setupPlaylistsStage();
        LOGGER.info("Opened the playlist management window");
    }

    private void setupPlaylistsStage() {
        playlistStage = new Stage();
        playlistStage.setScene(new Scene(playlistRoot));
        playlistStage.setResizable(false);
        playlistStage.setTitle("Manage playlists");
        playlistStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/icons/music-note.png")).toString()));
        playlistStage.show();
        Bindings.bindBidirectional(SELECTED_ITEM, playlistController.getSelectedItem());
        Bindings.bindBidirectional(SELECTED_PLAYLIST_NAME, playlistController.getSelectedPlaylistName());
    }

}
