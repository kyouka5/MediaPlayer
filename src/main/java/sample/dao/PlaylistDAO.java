package sample.dao;

import sample.model.Item;
import sample.model.Playlist;

import java.util.List;

public interface PlaylistDAO {

    public void createPlaylist(String name, List<Item> contents);

    public Playlist readPlaylistByName(String name);

    public void updatePlaylistName(Playlist playlist, String name);

    public void updatePlaylistContents(Playlist playlist, List<Item> contents);

    public void removePlaylist(Playlist playlist);

    public List<String> getPlaylistNames();

    public void createItem(String path, String name, String title, String artist, String album, int year, String genre, Playlist playlist);

    public List<Item> getItemsByPlaylist(Playlist playlist);

    public Item getItemByPath(Playlist playlist, String path);

    public void removeItemFromPlaylistByName(Playlist playlist, String name);

    public List<String> getAllPaths();

    public void removeItemByPath(String path);

}
