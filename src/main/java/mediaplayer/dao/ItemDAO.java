package mediaplayer.dao;

import com.google.inject.persist.Transactional;
import mediaplayer.model.Item;
import mediaplayer.model.Playlist;
import mediaplayer.util.jpa.GenericDAO;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * DAO class of the {@link Item} model.
 */
public class ItemDAO extends GenericDAO<Item> {
    /**
     * Calls {@link GenericDAO}'s constructor on the {@link Item} class.
     */
    public ItemDAO() {
        super(Item.class);
    }

    /**
     * Gets the list of {@link Item}s by a given {@link Playlist}.
     *
     * @param playlist the {@link Playlist}
     * @return optional list of {@link Item}s
     */
    public Optional<List<Item>> getItemsByPlaylist(Playlist playlist) {
        List<Item> itemsOfGivenPlaylist = entityManager.createQuery("select i from Item i where :playlistId = i.playlist.id", Item.class)
                .setParameter("playlistId", playlist.getId())
                .getResultList();
        return itemsOfGivenPlaylist.isEmpty() ? Optional.empty() : Optional.of(itemsOfGivenPlaylist);
    }

    /**
     * Gets an {@link Item} by its {@link Playlist} and {@code path}.
     *
     * @param playlist the {@link Playlist} of the {@link Item}
     * @param path     the path of the {@link Item}
     * @return the item found
     */
    public Optional<Item> getItemByPath(Playlist playlist, String path) {
        return entityManager.createQuery("select i from Item i where :playlistId = i.playlist.id and i.path = :path", Item.class)
                .setParameter("path", path)
                .setParameter("playlistId", playlist.getId())
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    /**
     * Gets the {@link Item} from the given {@link Playlist} with the given {@code name}.
     *
     * @param playlist the {@link Playlist} containing the {@link Item}
     * @param name     the name of the {@link Item}
     * @return the item found
     */
    @Transactional
    public Optional<Item> getItemFromPlaylistByName(Playlist playlist, String name) {
        return entityManager.createQuery("select i from Item i where i.name = :name and :playlistId = i.playlist.id", Item.class)
                .setParameter("name", name)
                .setParameter("playlistId", playlist.getId())
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    /**
     * Gets all the paths of {@link Item}s which are currently in the database.
     *
     * @return the list of paths
     */
    public List<String> getAllPaths() {
        TypedQuery<String> allPaths = entityManager.createQuery("select i.path from Item i", String.class);
        return allPaths.getResultList();
    }

    /**
     * Gets the path of the {@link Item} with the given name.
     *
     * @param name the name of the {@link Item}
     * @return optional path of the {@link Item}
     */
    public Optional<String> getPathByItemName(String name) {
        return entityManager.createQuery("select i.path from Item i where i.name = :name", String.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    /**
     * Removes the {@link Item} with the given {@code path}.
     *
     * @param path the path of the {@link Item}
     */
    @Transactional
    public void removeItemByPath(String path) {
        Optional<Item> itemWithGivenPath = entityManager.createQuery("select i from Item i where i.path = :path", Item.class)
                .setParameter("path", path)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
        itemWithGivenPath.ifPresent(itemToDelete -> entityManager.remove(itemToDelete));
    }

    /**
     * Gets the {@link Item}s with the greatest number of views from the given {@link Playlist}.
     *
     * @param playlist the {@link Playlist} to be inspected
     * @param n        the number of {@link Item}s to be returned
     * @return the list of {@link Item} names
     */
    public List<String> getMostPlayedFromPlaylist(Playlist playlist, int n) {
        return entityManager.createQuery("select i.name from Item i where " + playlist.getId() + " = i.playlist.id and i.numberOfViews > 0 order by i.numberOfViews desc", String.class)
                .setMaxResults(n).getResultList();
    }

}
