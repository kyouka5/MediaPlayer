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

    public void createItem(String name, String path, Playlist playlist);

    public List<Item> getItemsByPlaylist(Playlist playlist);

    public void removeItemFromPlaylistByName(Playlist playlist, String name);

}
