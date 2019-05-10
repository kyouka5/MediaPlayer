package mediaplayer.dao;

import com.google.inject.persist.Transactional;
import mediaplayer.model.Item;
import mediaplayer.model.Playlist;
import mediaplayer.util.jpa.GenericDAO;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * DAO class of the {@link Playlist} model.
 */
public class PlaylistDAO extends GenericDAO {
    /**
     * Calls {@link GenericDAO}'s constructor on the {@link Playlist} class.
     */
    public PlaylistDAO() {
        super(Playlist.class);
    }

    /**
     * Updates the {@code name} of the {@link Playlist}.
     * @param playlist the {@link Playlist} to be updated
     * @param name the new name
     */
    @Transactional
    public void updatePlaylistName(Playlist playlist, String name) {
        playlist.setName(name);
    }

    /**
     * Updates the {@code contents} of the {@link Playlist}.
     * @param playlist the {@link Playlist} to be updated
     * @param contents the new contents
     */
    @Transactional
    public void updatePlaylistContents(Playlist playlist, List<Item> contents) {
        playlist.getContents().clear();
        playlist.getContents().addAll(contents);
    }

    /**
     * Gets all the {@link Playlist} names from the database.
     * @return the list of {@link Playlist} names
     */
    public List<String> getPlaylistNames() {
        TypedQuery<String> query = entityManager.createQuery("select p.name from Playlist p", String.class);
        return query.getResultList();
    }

    /**
     * Gets a {@link Playlist} by its {@code name}.
     * @param name the name of the {@link Playlist}
     * @return the {@link Playlist} found, or {@code null} if it does not exists
     */
    public Playlist getPlaylistByName(String name) {
        TypedQuery<Playlist> query = entityManager.createQuery("select p from Playlist p where p.name='" + name + "'", Playlist.class);
        List<Playlist> result = query.getResultList();
        return !result.isEmpty() ? result.get(0) : null;
    }
}
