package mediaplayer.dao;

import com.google.inject.persist.Transactional;
import mediaplayer.model.Item;
import mediaplayer.model.Playlist;

import javax.persistence.TypedQuery;
import java.util.List;

public class ItemDAO extends GenericDAO {
    public ItemDAO() {
        super(Item.class);
    }

    public List<Item> getItemsByPlaylist(Playlist playlist) {
        TypedQuery<Item> query = entityManager.createQuery("select i from Item i where " + playlist.getId() + " = i.playlist.id", Item.class);
        return query.getResultList();
    }

    public Item getItemByPath(Playlist playlist, String path) {
        TypedQuery<Item> query = entityManager.createQuery("select i from Item i where " + playlist.getId() + " = i.playlist.id and i.path = '" + path + "'", Item.class);
        return query.getResultList().get(0);
    }

    @Transactional
    public void removeItemFromPlaylistByName(Playlist playlist, String name) {
        TypedQuery<Item> query = entityManager.createQuery("select i from Item i where i.name ='" + name + "' and " + playlist.getId() + " = i.playlist.id", Item.class);
        entityManager.remove(query.getResultList().get(0));
    }

    public List<String> getAllPaths() {
        TypedQuery<String> query = entityManager.createQuery("select i.path from Item i", String.class);
        return query.getResultList();
    }

    @Transactional
    public void removeItemByPath(String path) {
        TypedQuery<Item> query = entityManager.createQuery("select i from Item i where i.path ='" + path + "'", Item.class);
        entityManager.remove(query.getResultList().get(0));
    }

}
