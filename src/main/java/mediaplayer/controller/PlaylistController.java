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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static final int MOST_PLAYED_ITEMS_MAX_COUNT = 5;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistController.class);

    /**
     * The currently selected item.
     */
    private final ObjectProperty<String> SELECTED_ITEM = new SimpleObjectProperty<>();

    /**
     * The index of the currently selected item.
     */
    private final ObjectProperty<Integer> SELECTED_INDEX = new SimpleObjectProperty<>();

    /**
     * The name of the currently selected playlist.
     */
    private final ObjectProperty<String> SELECTED_PLAYLIST_NAME = new SimpleObjectProperty<>();

    public ObjectProperty<String> getSelectedItem() {
        return SELECTED_ITEM;
    }

    public ObjectProperty<String> getSelectedPlaylistName() {
        return SELECTED_PLAYLIST_NAME;
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
        List<String> pathsInDatabase = itemDAO.getAllPaths();
        List<String> pathsNotFound = findUnavailablePaths(pathsInDatabase);

        if (!pathsNotFound.isEmpty()) {
            removeUnavailableItems(pathsNotFound);
            alertUnavailableFiles();
        }
    }

    private void removeUnavailableItems(List<String> pathsNotFound) {
        for (String pathNotFound : pathsNotFound) {
            itemDAO.removeItemByPath(pathNotFound);
        }
    }

    private List<String> findUnavailablePaths(List<String> pathsInDatabase) {
        List<String> pathsNotFound = new ArrayList<>();
        for (String pathInDatabase : pathsInDatabase) {
            Path path = convertStringToPath(pathInDatabase);
            if (!path.toFile().exists()) {
                pathsNotFound.add(pathInDatabase);
            }
        }
        return pathsNotFound;
    }

    private Path convertStringToPath(String pathInDatabase) {
        URI uriOfPath = URI.create(pathInDatabase);
        String uriPathToString = Paths.get(uriOfPath).toString();
        return Paths.get(uriPathToString);
    }

    /**
     * Alerts the user that some unavailable files has been deleted.
     */
    private void alertUnavailableFiles() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Some files were not found");
        alert.setContentText("Missing files have been removed from the database. \n");
        alert.showAndWait();
    }

    /**
     * Populates the {@link ListView} of playlists with the currently available {@link Playlist}s.
     */
    private void loadPlaylists() {
        List<String> playlistNames = playlistDAO.getPlaylistNames();
        ObservableList<String> observablePlaylistNames = FXCollections.observableArrayList(playlistNames);
        listViewPlaylist.setItems(observablePlaylistNames);
    }


    @FXML
    private void createPlaylist(ActionEvent event) {
        if (playlistNameField != null) {
            String playlistName = playlistNameField.getText();
            boolean isNameValid = validator.checkPlaylistName(playlistName);
            if (isNameValid) {
                LOGGER.info("CREATED a new playlist: {}", playlistName);
                Playlist emptyPlaylistWithGivenName = new Playlist(playlistName, new ArrayList<>());
                playlistDAO.persist(emptyPlaylistWithGivenName);
            } else {
                alertInvalidPlaylistName();
            }
            playlistNameField.setText("");
        } else {
            LOGGER.warn("The playlist text field is empty");
        }
        loadPlaylists();
    }

    @FXML
    private void updatePlaylist(ActionEvent event) {
        String selected = listViewPlaylist.getSelectionModel().getSelectedItem();
        if (selected == null) {
            LOGGER.warn("No playlist has been selected");
            return;
        }
        if (playlistNameField == null) {
            LOGGER.warn("The TextField is empty");
            return;
        }
        String playlistName = playlistNameField.getText();
        boolean isNameValid = validator.checkPlaylistName(playlistName);
        if (isNameValid) {
            Optional<Playlist> playlistWithGivenName = playlistDAO.getPlaylistByName(selected);
            playlistWithGivenName.ifPresent(playlist -> {
                playlistDAO.updatePlaylistName(playlist, playlistName);
                LOGGER.info("Playlist %s has been RENAMED to %s".formatted(selected, playlistName));
                listViewPlaylist.getItems().set(listViewPlaylist.getSelectionModel().getSelectedIndex(), playlistName);
                listViewPlaylist.getSelectionModel().select(-1);
                clearItemsAndMostPlayed();
            });
        } else {
            alertInvalidPlaylistName();
        }
        playlistNameField.setText("");
    }

    /**
     * Alerts the user that the playlist name they entered is invalid.
     */
    private void alertInvalidPlaylistName() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid playlist name");
        alert.setContentText("Playlist names have to meet the following criteria: \n" +
                             "- must be unique \n" +
                             "- must be between 2 and 50 characters \n" +
                             "- must not contain leading or trailing whitespaces");
        alert.showAndWait();
    }

    @FXML
    private void deletePlaylist(ActionEvent event) {
        String selectedPlaylistName = listViewPlaylist.getSelectionModel().getSelectedItem();
        if (selectedPlaylistName == null) {
            LOGGER.warn("No playlist has been selected");
            return;
        }
        Optional<Playlist> playlistWithGivenName = playlistDAO.getPlaylistByName(selectedPlaylistName);
        playlistWithGivenName.ifPresent(playlist -> {
            playlistDAO.remove(playlist);
            LOGGER.info("Playlist has been REMOVED: {}", playlist.getName());
            listViewPlaylist.getItems().remove(listViewPlaylist.getSelectionModel().getSelectedIndex());
            clearItemsAndMostPlayed();
            loadPlaylists();
        });
    }

    @FXML
    private void createItem(ActionEvent event) {
        String selectedPlaylistName = listViewPlaylist.getSelectionModel().getSelectedItem();
        if (selectedPlaylistName == null) {
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Files", "*mp4", "*.mp3"));
        List<File> listOfFiles = fileChooser.showOpenMultipleDialog(((Node) event.getSource()).getScene().getWindow());
        if (listOfFiles != null) {
            LOGGER.info("New media has been ADDED to the playlist");
            IntStream.range(0, listOfFiles.size()).forEach(fileIndex -> {
                Optional<Playlist> playlist = playlistDAO.getPlaylistByName(selectedPlaylistName);
                Item.ItemBuilder itemToCreate = Item.builder()
                        .path(listOfFiles.stream().map(e -> e.toURI().toString()).collect(Collectors.toList()).get(fileIndex))
                        .name(listOfFiles.stream().map(File::getName).collect(Collectors.toList()).get(fileIndex))
                        .playlist(playlist.orElse(null))
                        .numberOfViews(0);
                if (listOfFiles.get(fileIndex).getName().endsWith("mp3")) {
                    try {
                        AudioFile audioFile = AudioFileIO.read(listOfFiles.get(fileIndex));
                        Tag tag = audioFile.getTag();
                        itemToCreate.title(tag.getFirst(FieldKey.TITLE))
                                .artist(tag.getFirst(FieldKey.ARTIST))
                                .album(tag.getFirst(FieldKey.ALBUM))
                                .year(Year.of(Integer.parseInt(tag.getFirst(FieldKey.YEAR))))
                                .genre(tag.getFirst(FieldKey.GENRE));
                    } catch (CannotReadException | ReadOnlyFileException | InvalidAudioFrameException | TagException | IOException e) {
                        e.printStackTrace();
                    }
                }
                itemDAO.persist(itemToCreate.build());
                itemDAO.flush();
            });
            Optional<Playlist> selectedPlaylist = playlistDAO.getPlaylistByName(selectedPlaylistName);
            selectedPlaylist.ifPresent(playlist -> {
                Optional<List<Item>> selectedPlaylistItems = itemDAO.getItemsByPlaylist(playlist);
                selectedPlaylistItems.ifPresent(items -> {
                    playlistDAO.updatePlaylistContents(playlist, items);
                    setObservablePlaylistItems(items);
                });
            });
        } else {
            LOGGER.warn("No media has been selected");
        }
    }

    @FXML
    private void deleteItem(ActionEvent event) {
        String selectedPlaylistName = listViewPlaylist.getSelectionModel().getSelectedItem();
        String selectedItemName = listViewItem.getSelectionModel().getSelectedItem();
        if (selectedPlaylistName != null) {
            if (selectedItemName != null) {
                Optional<Playlist> selectedPlaylist = playlistDAO.getPlaylistByName(selectedPlaylistName);
                selectedPlaylist.ifPresent(playlist -> {
                    listViewItem.getItems().remove(listViewItem.getSelectionModel().getSelectedIndex());
                    if (playlist.getContents().size() > 1) {
                        Optional<Item> itemToDelete = itemDAO.getItemFromPlaylistByName(playlist, selectedItemName);
                        itemToDelete.ifPresent(item -> itemDAO.remove(item));
                        LOGGER.info("REMOVED from the playlist: {}", selectedItemName);
                        updateMostPlayed();
                    } else {
                        playlistDAO.remove(playlist);
                        clearItemsAndMostPlayed();
                        listViewPlaylist.getSelectionModel().select(-1);
                        loadPlaylists();
                    }
                });
            }
        } else {
            LOGGER.warn("No media has been selected");
        }
    }


    @FXML
    private void selectPlaylist(MouseEvent event) {
        if (event.getClickCount() == 1) {
            if (listViewPlaylist.getSelectionModel().getSelectedItem() != null) {
                SELECTED_PLAYLIST_NAME.setValue(listViewPlaylist.getSelectionModel().getSelectedItem());
                LOGGER.info("SELECTED the playlist {}", SELECTED_PLAYLIST_NAME.getValue());
                Optional<Playlist> selectedPlaylist = playlistDAO.getPlaylistByName(SELECTED_PLAYLIST_NAME.getValue());
                selectedPlaylist.ifPresent(playlist -> {
                    if (playlist.getContents().isEmpty()) {
                        clearItemsAndMostPlayed();
                    } else {
                        showPlaylistContents(playlist);
                    }
                });
            }
        }
    }

    private void showPlaylistContents(Playlist playlist) {
        clearItemsAndMostPlayed();
        Optional<List<Item>> itemsOfSelectedPlaylist = itemDAO.getItemsByPlaylist(playlist);
        itemsOfSelectedPlaylist.ifPresent(items -> {
            setObservablePlaylistItems(items);
            updateMostPlayed();
        });
    }

    private void setObservablePlaylistItems(List<Item> items) {
        ObservableList<String> itemNames = FXCollections.observableArrayList(items
                .stream().map(Item::getName).collect(Collectors.toList()));
        listViewItem.setItems(itemNames);
    }

    @FXML
    private void selectItem(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selectedItemName = listViewItem.getSelectionModel().getSelectedItem();
            if (selectedItemName == null) {
                selectedItemName = listViewMostPlayed.getSelectionModel().getSelectedItem();
            }
            if (selectedItemName != null) {
                LOGGER.info("SELECTED {} from the playlist", selectedItemName);
                selectItemByName(selectedItemName);
            } else {
                LOGGER.warn("No media has been selected");
            }
        }
    }

    /**
     * Selects an {@link Item} to play and sets the value of {@code selectedItem} and {@code selectedIndex} accordingly.
     *
     * @param selectedItemName the name of the currently selected {@link Item}
     */
    private void selectItemByName(String selectedItemName) {
        Optional<Playlist> selectedPlaylist = playlistDAO.getPlaylistByName(listViewPlaylist.getSelectionModel().getSelectedItem());
        selectedPlaylist.ifPresent(playlist -> {
            Optional<List<Item>> itemsOfPlaylist = itemDAO.getItemsByPlaylist(playlist);
            itemsOfPlaylist.ifPresent(items -> {
                itemDAO.getPathByItemName(selectedItemName);
                Optional<String> selectedItemPath = itemDAO.getPathByItemName(selectedItemName);
                selectedItemPath.ifPresent(itemPath -> {
                    SELECTED_ITEM.setValue(itemPath);
                    SELECTED_INDEX.setValue(listViewItem.getSelectionModel().getSelectedIndex());
                    updateMostPlayed();
                });
            });
        });
    }

    private void clearItemsAndMostPlayed() {
        listViewItem.getItems().clear();
        listViewMostPlayed.getItems().clear();
    }

    /**
     * Populates the {@link ListView} of most played items with the top 5 {@link Item}s based on their number of views.
     */
    public void updateMostPlayed() {
        Optional<Playlist> selectedPlaylist = playlistDAO.getPlaylistByName(listViewPlaylist.getSelectionModel().getSelectedItem());
        selectedPlaylist.ifPresent(playlist -> {
            ObservableList<String> mostPlayedList = FXCollections.observableArrayList(itemDAO.getMostPlayedFromPlaylist(playlist, MOST_PLAYED_ITEMS_MAX_COUNT));
            listViewMostPlayed.setItems(mostPlayedList);
        });
    }

}
