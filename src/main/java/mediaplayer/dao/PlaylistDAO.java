package mediaplayer.dao;

import com.google.inject.persist.Transactional;
import mediaplayer.model.Item;
import mediaplayer.model.Playlist;

import javax.persistence.TypedQuery;
import java.util.List;

public class PlaylistDAO extends GenericDAO {
    public PlaylistDAO() {
        super(Playlist.class);
    }

    @Transactional
    public void updatePlaylistName(Playlist playlist, String name) {
        playlist.setName(name);
    }

    @Transactional
    public void updatePlaylistContents(Playlist playlist, List<Item> contents) {
        playlist.getContents().clear();
        playlist.getContents().addAll(contents);
    }

    public List<String> getPlaylistNames() {
        TypedQuery<String> query = entityManager.createQuery("select p.name from Playlist p", String.class);
        return query.getResultList();
    }

    public Playlist readPlaylistByName(String name) {
        TypedQuery<Playlist> query = entityManager.createQuery("select p from Playlist p where p.name='" + name + "'", Playlist.class);
        List<Playlist> result = query.getResultList();
        return !result.isEmpty() ? result.get(0) : null;
    }
}
