package mediaplayer.dao;

import com.google.inject.persist.Transactional;
import mediaplayer.model.Item;
import mediaplayer.model.Playlist;
import mediaplayer.util.jpa.GenericDAO;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * DAO class of the {@link Item} model.
 */
public class ItemDAO extends GenericDAO {
    /**
     * Calls {@link GenericDAO}'s constructor on the {@link Item} class.
     */
    public ItemDAO() {
        super(Item.class);
    }

    /**
     * Gets the {@link Item}s by a given {@link Playlist}.
     * @param playlist the {@link Playlist}
     * @return the list of {@link Item}s
     */
    public List<Item> getItemsByPlaylist(Playlist playlist) {
        TypedQuery<Item> query = entityManager.createQuery("select i from Item i where " + playlist.getId() + " = i.playlist.id", Item.class);
        return query.getResultList();
    }

    /**
     * Gets an {@link Item} by its {@link Playlist} and {@code path}.
     * @param playlist the {@link Playlist} of the {@link Item}
     * @param path the path of the {@link Item}
     * @return the item found
     */
    public Item getItemByPath(Playlist playlist, String path) {
        TypedQuery<Item> query = entityManager.createQuery("select i from Item i where " + playlist.getId() + " = i.playlist.id and i.path = '" + path + "'", Item.class);
        return query.getResultList().get(0);
    }

    /**
     * Removes the {@link Item} with the given {@code name} from the {@link Playlist}.
     * @param playlist the {@link Playlist} containing the {@link Item}
     * @param name the name of the {@link Item}
     */
    @Transactional
    public void removeItemFromPlaylistByName(Playlist playlist, String name) {
        TypedQuery<Item> query = entityManager.createQuery("select i from Item i where i.name ='" + name + "' and " + playlist.getId() + " = i.playlist.id", Item.class);
        entityManager.remove(query.getResultList().get(0));
    }

    /**
     * Gets all the paths of {@link Item}s which are currently in the database.
     * @return the list of paths
     */
    public List<String> getAllPaths() {
        TypedQuery<String> query = entityManager.createQuery("select i.path from Item i", String.class);
        return query.getResultList();
    }

    /**
     * Removes the {@link Item} with the given {@code path}.
     * @param path the path of the {@link Item}
     */
    @Transactional
    public void removeItemByPath(String path) {
        TypedQuery<Item> query = entityManager.createQuery("select i from Item i where i.path ='" + path + "'", Item.class);
        entityManager.remove(query.getResultList().get(0));
    }

}
