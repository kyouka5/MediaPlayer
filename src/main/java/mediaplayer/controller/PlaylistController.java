package mediaplayer.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mediaplayer.dao.ItemDAO;
import mediaplayer.util.guice.PersistenceModule;
import mediaplayer.dao.PlaylistDAO;
import mediaplayer.model.Item;
import mediaplayer.model.Playlist;
import mediaplayer.util.Validator;

public class PlaylistController implements Initializable {

    @FXML
    private TextField playlistNameField;

    @FXML
    private ListView<String> listViewPlaylist;

    @FXML
    private ListView<String> listViewItem;

    @FXML
    private ListView<String> listViewMostPlayed;

    private PlaylistDAO playlistDAO;
    private ItemDAO itemDAO;

    private Validator validator;

    private static Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    /**
     * The currently selected item.
     */
    private ObjectProperty<String> selectedItem = new SimpleObjectProperty<>();

    /**
     * The index of the currently selected item.
     */
    private ObjectProperty<Integer> selectedIndex = new SimpleObjectProperty<>();

    /**
     * The name of the currently selected playlist.
     */
    private ObjectProperty<String> selectedPlaylistName = new SimpleObjectProperty<>();

    public ObjectProperty<String> getSelectedItem() {
        return selectedItem;
    }

    public ObjectProperty<String> getSelectedPlaylistName() {
        return selectedPlaylistName;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Injector injector = Guice.createInjector(new PersistenceModule("mediaplayer"));
        playlistDAO = injector.getInstance(PlaylistDAO.class);
        itemDAO = injector.getInstance(ItemDAO.class);
        validator = new Validator(playlistDAO);

        removeUnavailableFiles();
        loadPlaylists();
    }

    /**
     * Checks if there are any files in the database which are no longer available and deletes them.
     */
    private void removeUnavailableFiles() {
        List<String> pathsNotFound = new ArrayList<>();
        List<String> pathsInDatabase = itemDAO.getAllPaths();

        for (String pathInDatabase : pathsInDatabase) {
            URI uriOfPath = URI.create(pathInDatabase);
            String uriPathToString = Paths.get(uriOfPath).toString();
            Path path = Paths.get(uriPathToString);
            if (!path.toFile().exists()) {
                pathsNotFound.add(pathInDatabase);
            }
        }

        if (!pathsNotFound.isEmpty()) {
            for (String pathNotFound : pathsNotFound) {
                itemDAO.removeItemByPath(pathNotFound);
            }
            alertUnavailableFiles();
        }
    }

    /**
     * Alerts the user that some unavailable files has been deleted.
     */
    private void alertUnavailableFiles() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Some files were not found");
        alert.setContentText("Some files are missing, hence they got removed from the database. \n");
        alert.showAndWait();
    }

    /**
     * Populates the {@link ListView} of playlists with the currently available {@link Playlist}s.
     */
    private void loadPlaylists() {
        ObservableList<String> playlistNames = FXCollections.observableArrayList(playlistDAO.getPlaylistNames());
        listViewPlaylist.setItems(playlistNames);
    }


    @FXML
    private void createPlaylist(ActionEvent event) {
        if (playlistNameField != null) {
            String playlistName = playlistNameField.getText();
            boolean isNameValid = validator.checkPlaylistName(playlistName);
            if (isNameValid) {
                logger.info("CREATED a new playlist");
                playlistDAO.persist(new Playlist(playlistName, new ArrayList<>()));
            } else {
                alertInvalidPlaylistName();
            }
            playlistNameField.setText("");
        } else {
            logger.warn("The TextField is empty");
        }
        loadPlaylists();
    }

    @FXML
    private void updatePlaylist(ActionEvent event) {
        String selected = listViewPlaylist.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (playlistNameField != null) {
                String playlistName = playlistNameField.getText();
                boolean isNameValid = validator.checkPlaylistName(playlistName);
                if (isNameValid) {
                    Playlist playlist = playlistDAO.getPlaylistByName(selected);
                    playlistDAO.updatePlaylistName(playlist, playlistName);
                    logger.info("Playlist " + selected + " has been RENAMED to " + playlistName);
                    listViewPlaylist.getItems().set(listViewPlaylist.getSelectionModel().getSelectedIndex(), playlistName);
                    listViewPlaylist.getSelectionModel().select(-1);
                    clearItemsAndMostPlayed();
                } else {
                    alertInvalidPlaylistName();
                }
                playlistNameField.setText("");
            } else {
                logger.warn("The TextField is empty");
            }
        } else {
            logger.warn("No playlist has been selected");
        }
    }

    /**
     * Alerts the user that the playlist name they entered is invalid.
     */
    private void alertInvalidPlaylistName() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid playlist name");
        alert.setContentText("Playlist names have to meet the following criteria: \n" +
                             "- be unique \n" +
                             "- be between 2 and 50 characters \n" +
                             "- contain neither leading nor trailing whitespaces");
        alert.showAndWait();
    }

    @FXML
    private void deletePlaylist(ActionEvent event) {
        String selectedPlaylistName = listViewPlaylist.getSelectionModel().getSelectedItem();
        if (selectedPlaylistName != null) {
            Playlist playlist = playlistDAO.getPlaylistByName(selectedPlaylistName);
            playlistDAO.remove(playlist);
            logger.info("Playlist " + playlist.getName() + " has been REMOVED");
            listViewPlaylist.getItems().remove(listViewPlaylist.getSelectionModel().getSelectedIndex());
            clearItemsAndMostPlayed();
            loadPlaylists();
        } else {
            logger.warn("No playlist has been selected");
        }

    }

    @FXML
    private void createItem(ActionEvent event) {
        String selectedPlaylistName = listViewPlaylist.getSelectionModel().getSelectedItem();
        if (selectedPlaylistName != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Files", "*mp4", "*.mp3"));
            List<File> listOfFiles = fileChooser.showOpenMultipleDialog(((Node) event.getSource()).getScene().getWindow());
            if (listOfFiles != null) {
                logger.info("New media has been ADDED to the playlist");
                for (int i = 0; i < listOfFiles.size(); i++) {
                    if (listOfFiles.get(i).getName().endsWith("mp3")) {
                        try {
                            java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
                            AudioFile audioFile = AudioFileIO.read(listOfFiles.get(i));
                            Tag tag = audioFile.getTag();
                            itemDAO.persist(Item.builder()
                                    .path(listOfFiles.stream().map(e -> e.toURI().toString()).collect(Collectors.toList()).get(i))
                                    .name(listOfFiles.stream().map(File::getName).collect(Collectors.toList()).get(i))
                                    .title(tag.getFirst(FieldKey.TITLE))
                                    .artist(tag.getFirst(FieldKey.ARTIST))
                                    .album(tag.getFirst(FieldKey.ALBUM))
                                    .year(tag.getFirst(FieldKey.YEAR).isEmpty() ? Year.of(0) : Year.of(Integer.parseInt(tag.getFirst(FieldKey.YEAR))))
                                    .genre(tag.getFirst(FieldKey.GENRE))
                                    .playlist(playlistDAO.getPlaylistByName(selectedPlaylistName))
                                    .numberOfViews(0)
                                    .build());
                        } catch (CannotReadException | ReadOnlyFileException | InvalidAudioFrameException | TagException | IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        itemDAO.persist(Item.builder()
                                .path(listOfFiles.stream().map(e -> e.toURI().toString()).collect(Collectors.toList()).get(i))
                                .name(listOfFiles.stream().map(File::getName).collect(Collectors.toList()).get(i))
                                .title(null)
                                .artist(null)
                                .album(null)
                                .year(Year.of(0))
                                .genre(null)
                                .playlist(playlistDAO.getPlaylistByName(selectedPlaylistName))
                                .numberOfViews(0)
                                .build());
                    }
                    itemDAO.flush();
                }
                Playlist selectedPlaylist = playlistDAO.getPlaylistByName(selectedPlaylistName);
                List<Item> items = itemDAO.getItemsByPlaylist(selectedPlaylist);
                playlistDAO.updatePlaylistContents(selectedPlaylist, items);
                ObservableList<String> itemNames = FXCollections.observableArrayList(items.stream().map(Item::getName).collect(Collectors.toList()));
                listViewItem.setItems(itemNames);
            } else {
                logger.warn("No media has been selected");
            }
        }

    }

    @FXML
    private void deleteItem(ActionEvent event) {
        String selectedPlaylistName = listViewPlaylist.getSelectionModel().getSelectedItem();
        String selectedItemName = listViewItem.getSelectionModel().getSelectedItem();
        if (selectedPlaylistName != null) {
            if (selectedItemName != null) {
                Playlist playlist = playlistDAO.getPlaylistByName(selectedPlaylistName);
                if (playlist.getContents().size() >= 2) {
                    itemDAO.removeItemFromPlaylistByName(playlist, selectedItemName);
                    playlist.getContents().clear();
                    playlist.getContents().addAll(itemDAO.getItemsByPlaylist(playlist));
                    logger.info("REMOVED " + selectedPlaylistName + " from the playlist");
                    listViewItem.getItems().remove(listViewItem.getSelectionModel().getSelectedIndex());
                    updateMostPlayed();
                } else {
                    itemDAO.update(itemDAO.getItemFromPlaylistByName(playlist, selectedItemName));
                    playlistDAO.remove(playlist);
                    clearItemsAndMostPlayed();
                    listViewPlaylist.getItems().remove(listViewPlaylist.getSelectionModel().getSelectedIndex());
                    listViewPlaylist.getSelectionModel().select(-1);
                    loadPlaylists();
                }
            }
        } else {
            logger.warn("No media has been selected");
        }
    }


    @FXML
    private void selectPlaylist(MouseEvent event) {
        if (event.getClickCount() == 1) {
            if (listViewPlaylist.getSelectionModel().getSelectedItem() != null) {
                selectedPlaylistName.setValue(listViewPlaylist.getSelectionModel().getSelectedItem());
                if (selectedPlaylistName != null) {
                    logger.info("SELECTED the playlist " + selectedPlaylistName.getValue());
                    Playlist playlist = playlistDAO.getPlaylistByName(selectedPlaylistName.getValue());
                    if (!playlist.getContents().isEmpty()) {
                        showPlaylistContents(playlist);
                    } else {
                        clearItemsAndMostPlayed();
                    }
                } else {
                    logger.warn("No media has been selected");
                }
            }
        }
    }

    private void showPlaylistContents(Playlist playlist) {
        clearItemsAndMostPlayed();
        List<Item> itemsOfSelectedPlaylist = itemDAO.getItemsByPlaylist(playlist);
        ObservableList<String> itemNames = FXCollections.observableArrayList(itemsOfSelectedPlaylist
                .stream().map(Item::getName).collect(Collectors.toList()));
        listViewItem.setItems(itemNames);
        updateMostPlayed();
    }

    @FXML
    private void selectItem(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selectedItemName = listViewItem.getSelectionModel().getSelectedItem();
            String selectedMostPlayedName = listViewMostPlayed.getSelectionModel().getSelectedItem();
            if (selectedItemName != null) {
                logger.info("SELECTED " + selectedItemName + " from the playlist");
                select(selectedItemName);
            } else if (selectedMostPlayedName != null) {
                logger.info("SELECTED " + selectedMostPlayedName + " from the playlist");
                select(selectedMostPlayedName);
            } else {
                logger.warn("No media has been selected");
            }
        }
    }

    /**
     * Selects an {@link Item} to play and sets the value of {@code selectedItem} and {@code selectedIndex} accordingly.
     *
     * @param selectedItemName the name of the currently selected {@link Item}
     */
    private void select(String selectedItemName) {
        Playlist playlist = playlistDAO.getPlaylistByName(listViewPlaylist.getSelectionModel().getSelectedItem());
        if (itemDAO.getItemsByPlaylist(playlist) != null) {
            itemDAO.getPathByItemName(selectedItemName);
            String selectedItemPath = itemDAO.getPathByItemName(selectedItemName);
            selectedItem.setValue(selectedItemPath);
            selectedIndex.setValue(listViewItem.getSelectionModel().getSelectedIndex());
            updateMostPlayed();
        }
    }

    private void clearItemsAndMostPlayed() {
        listViewItem.getItems().clear();
        listViewMostPlayed.getItems().clear();
    }

    /**
     * Populates the {@link ListView} of most played items with the top 5 {@link Item}s based on their number of views.
     */
    public void updateMostPlayed() {
        Playlist playlist = playlistDAO.getPlaylistByName(listViewPlaylist.getSelectionModel().getSelectedItem());
        ObservableList<String> mostPlayedList = FXCollections.observableArrayList(itemDAO.getMostPlayedFromPlaylist(playlist, 5));
        listViewMostPlayed.setItems(mostPlayedList);
    }

}
