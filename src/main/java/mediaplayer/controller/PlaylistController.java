package mediaplayer.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import mediaplayer.dao.PersistenceModule;
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

    private ObjectProperty<String> selectedMedia = new SimpleObjectProperty<>();
    private ObjectProperty<Integer> selectedIndex = new SimpleObjectProperty<>();
    private ObjectProperty<String> selectedPlaylistName = new SimpleObjectProperty<>();

    public ObjectProperty<String> selectedFile() {
        return selectedMedia;
    }

    public ObjectProperty<String> selectedPlaylistName() {
        return selectedPlaylistName;
    }

    private PlaylistDAO playlistDAO;
    private ItemDAO itemDAO;

    private Validator validator = new Validator();

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

        List<String> pathsNotFound = new ArrayList<>();

        for (int i = 0; i < itemDAO.getAllPaths().size(); i++) {
            URI uriOfPath = URI.create(itemDAO.getAllPaths().get(i));
            String uriPathToString = Paths.get(uriOfPath).toString();
            Path path = Paths.get(uriPathToString);
            if (!path.toFile().exists()) {
                pathsNotFound.add(itemDAO.getAllPaths().get(i));
            }
        }

        if (!pathsNotFound.isEmpty()) {
            for (int i = 0; i < pathsNotFound.size(); i ++) {
                itemDAO.removeItemByPath(pathsNotFound.get(i));
            }

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Some files were not found");
            alert.setContentText("Some files are missing, hence they got removed from the database. \n");
            alert.showAndWait();
        }

        loadPlaylists();
    }

    @FXML
    private void createPlaylist(javafx.event.ActionEvent event) {
        if (playlistNameField != null) {
            String playlistName = playlistNameField.getText();
            boolean isNameValid = validator.checkPlaylistName(playlistName);
            if (isNameValid) {
                logger.info("CREATED a new playlist");
                List<Item> empty = new ArrayList<>();
                playlistDAO.persist(new Playlist(playlistName, empty));
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid playlist name");
                alert.setContentText("Playlist names have to meet the following criteria: \n"
                        + "- be unique \n"
                        + "- be between 2 and 50 characters \n"
                        + "- contain leading or trailing whitespaces");
                alert.showAndWait();
            }
            playlistNameField.setText("");
        } else {
            logger.warn("The TextField is empty");
        }
        loadPlaylists();
    }

    @FXML
    private void removePlaylist(javafx.event.ActionEvent event) {
        String selectedItem = listViewPlaylist.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Playlist playlist = playlistDAO.readPlaylistByName(selectedItem);
            playlistDAO.remove(playlist);
            logger.info("Playlist " + playlist.getName() + " has been REMOVED");
            listViewPlaylist.getItems().remove(listViewPlaylist.getSelectionModel().getSelectedIndex());
            loadPlaylists();
        } else {
            logger.warn("No playlist has been selected");
        }

    }

    @FXML
    private void editPlaylist(javafx.event.ActionEvent event) {
        if (listViewPlaylist.getSelectionModel().getSelectedItem() != null) {
            if (playlistNameField != null) {
                String playlistName = playlistNameField.getText();
                boolean isNameValid = validator.checkPlaylistName(playlistName);
                if (isNameValid) {
                    String currentName = listViewPlaylist.getSelectionModel().getSelectedItem();
                    Playlist playlist = playlistDAO.readPlaylistByName(currentName);
                    playlistDAO.updatePlaylistName(playlist, playlistName);
                    logger.info("Playlist " + currentName + " has been RENAMED to " + playlistName);
                    listViewPlaylist.getItems().set(listViewPlaylist.getSelectionModel().getSelectedIndex(), playlistName);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid playlist name");
                    alert.setContentText("Playlist names have to meet the following criteria: \n"
                            + "- be unique \n"
                            + "- be between 2 and 50 characters \n"
                            + "- contain leading or trailing whitespaces");
                    alert.showAndWait();
                }
                playlistNameField.setText("");
            } else {
                logger.warn("The TextField is empty");
            }
        } else {
            logger.warn("No playlist has been selected");
        }
    }


    @FXML
    private void addItem(javafx.event.ActionEvent event) {
        if (listViewPlaylist.getSelectionModel().getSelectedItem() != null) {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Files", "*mp4", "*.mp3"));
            List<File> listOfFiles = chooser.showOpenMultipleDialog(((Node) event.getSource()).getScene().getWindow());
            if (listOfFiles != null) {
                logger.info("New media has been ADDED to the playlist");
                for(int i=0; i< listOfFiles.size(); i++){
                    if (listOfFiles.get(i).getName().endsWith("mp3")) {
                        try {
                            java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
                            AudioFile audioFile = AudioFileIO.read(listOfFiles.get(i));
                            Tag tag = audioFile.getTag();
                            itemDAO.persist(new Item.Builder()
                                    .path(listOfFiles.stream().map(e -> e.toURI().toString()).collect(Collectors.toList()).get(i))
                                    .name(listOfFiles.stream().map(File::getName).collect(Collectors.toList()).get(i))
                                    .title(tag.getFirst(FieldKey.TITLE))
                                    .artist(tag.getFirst(FieldKey.ARTIST))
                                    .album(tag.getFirst(FieldKey.ALBUM))
                                    .year(tag.getFirst(FieldKey.YEAR).isEmpty() ? 0 : Integer.parseInt(tag.getFirst(FieldKey.YEAR)))
                                    .genre(tag.getFirst(FieldKey.GENRE))
                                    .playlist(playlistDAO.readPlaylistByName(listViewPlaylist.getSelectionModel().getSelectedItem()))
                                    .numberOfViews(0)
                                    .build());
                        } catch (CannotReadException | ReadOnlyFileException | InvalidAudioFrameException | TagException | IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        itemDAO.persist(new Item.Builder()
                                .path(listOfFiles.stream().map(e -> e.toURI().toString()).collect(Collectors.toList()).get(i))
                                .name(listOfFiles.stream().map(File::getName).collect(Collectors.toList()).get(i))
                                .title(null)
                                .artist(null)
                                .album(null)
                                .year(0)
                                .genre(null)
                                .playlist(playlistDAO.readPlaylistByName(listViewPlaylist.getSelectionModel().getSelectedItem()))
                                .numberOfViews(0)
                                .build());
                    }
                }
                Playlist selectedPlaylist = playlistDAO.readPlaylistByName(listViewPlaylist.getSelectionModel().getSelectedItem());
                List<Item> items = itemDAO.getItemsByPlaylist(selectedPlaylist);
                playlistDAO.updatePlaylistContents(selectedPlaylist, items);
                ObservableList<String> itemNames = FXCollections.observableArrayList(items.stream().map(Item::getName).collect(Collectors.toList()));
                listViewItem.setItems(itemNames);
            } else {
                logger.warn("No media has been selected");
            }
        }

    }

    private void loadPlaylists() {
        ObservableList<String> playlistNames = FXCollections.observableArrayList(playlistDAO.getPlaylistNames());
        listViewPlaylist.setItems(playlistNames);
    }

    @FXML
    private void removeItem(javafx.event.ActionEvent event) {
        if (listViewItem.getSelectionModel().getSelectedItem() != null) {
            Playlist playlist = playlistDAO.readPlaylistByName(listViewPlaylist.getSelectionModel().getSelectedItem());
            System.out.println(playlist.getContents());
            if (playlist.getContents().size() >= 2) {
                itemDAO.removeItemFromPlaylistByName(playlist, listViewItem.getSelectionModel().getSelectedItem());
                logger.info("REMOVED " + listViewItem.getSelectionModel().getSelectedItem() + " from the playlist");
                listViewItem.getItems().remove(listViewItem.getSelectionModel().getSelectedIndex());
            } else {
                playlistDAO.remove(playlist);
            }
            loadPlaylists();
        } else {
            logger.warn("No media has been selected");
        }
    }

    @FXML
    private void selectItem(MouseEvent event) {
        if (event.getClickCount() == 2) {
            if (listViewItem.getSelectionModel().getSelectedItem() != null) {
                Playlist playlist = playlistDAO.readPlaylistByName(listViewPlaylist.getSelectionModel().getSelectedItem());
                if (itemDAO.getItemsByPlaylist(playlist) != null) {
                    logger.info("SELECTED " + listViewItem.getSelectionModel().getSelectedItem() + " from the playlist");
                    List<Item> itemsOfSelectedPlaylist = itemDAO.getItemsByPlaylist(playlist);
                    String selectedItemName = itemsOfSelectedPlaylist.stream().filter(e -> e.getName().equals(listViewItem.getSelectionModel().getSelectedItem()))
                            .map(Item::getPath).collect(Collectors.joining());
                    selectedMedia.setValue(selectedItemName);
                    selectedIndex.setValue(listViewItem.getSelectionModel().getSelectedIndex());
                }
            } else {
                logger.warn("No media has been selected");
            }
        }
    }

    @FXML
    private void selectPlaylist(MouseEvent event) {
        if (event.getClickCount() == 1) {
            if (listViewPlaylist.getSelectionModel().getSelectedItem() != null) {
                logger.info("SELECTED the playlist " + listViewPlaylist.getSelectionModel().getSelectedItem());
                Playlist playlist = playlistDAO.readPlaylistByName(listViewPlaylist.getSelectionModel().getSelectedItem());
                selectedPlaylistName.setValue(playlist.getName());
                if (!playlist.getContents().isEmpty()) {
                    List<Item> itemsOfSelectedPlaylist = itemDAO.getItemsByPlaylist(playlist);
                    ObservableList<String> itemNames = FXCollections.observableArrayList(itemsOfSelectedPlaylist
                            .stream().map(Item::getName).collect(Collectors.toList()));
                    listViewItem.setItems(itemNames);
                } else {
                    listViewItem.getItems().clear();
                }

            } else {
                logger.warn("No media has been selected");
            }
        }
    }

}
