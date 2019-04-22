package sample.dao;

import sample.model.Item;
import sample.model.Playlist;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class PlaylistDAOImpl implements PlaylistDAO {
    private EntityManager em;

    public PlaylistDAOImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void createPlaylist(String name, List<Item> contents) {
        em.getTransaction().begin();
        Playlist playlist = new Playlist(name, contents);
        em.persist(playlist);
        em.getTransaction().commit();
    }

    @Override
    public void updatePlaylistName(Playlist playlist, String name) {
        em.getTransaction().begin();
        playlist.setName(name);
        em.getTransaction().commit();
    }

    @Override
    public void updatePlaylistContents(Playlist playlist, List<Item> contents) {
        em.getTransaction().begin();
        playlist.getContents().clear();
        playlist.getContents().addAll(contents);
        em.getTransaction().commit();
    }

//    @Override
//    public void updatePlaylist(Playlist playlist, String name, List<Item> contents) {
//        em.getTransaction().begin();
//        playlist.setName(name);
//        playlist.getContents().clear();
//        playlist.getContents().addAll(contents);
//        em.getTransaction().commit();
//    }

    @Override
    public void removePlaylist(Playlist playlist) {
        if (!playlist.getContents().isEmpty()) {
            getItemsByPlaylist(playlist).clear();
        }
        em.getTransaction().begin();
        em.remove(playlist);
        em.getTransaction().commit();
    }


    @Override
    public Playlist readPlaylistByName(String name) {
        TypedQuery<Playlist> query = em.createQuery("select p from Playlist p where p.name='" + name + "'", Playlist.class);
        List<Playlist> result = query.getResultList();
        return !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public List<String> getPlaylistNames() {
        TypedQuery<String> query = em.createQuery("select p.name from Playlist p", String.class);
        return query.getResultList();
    }

    @Override
    public List<Item> getItemsByPlaylist(Playlist playlist) {
        TypedQuery<Item> query = em.createQuery("select i from Item i where " + playlist.getId() + " = i.playlist.id", Item.class);
        return query.getResultList();
    }

    @Override
    public void createItem(String name, String path, Playlist playlist) {
        em.getTransaction().begin();
        Item item = new Item(name, path, playlist);
        em.persist(item);
        em.getTransaction().commit();
    }

    @Override
    public void removeItemFromPlaylistByName(Playlist playlist, String name) {
        em.getTransaction().begin();
        TypedQuery<Item> query = em.createQuery("select i from Item i where i.name ='" + name + "' and " + playlist.getId() + " = i.playlist.id", Item.class);
        em.remove(query.getResultList().get(0));
        em.getTransaction().commit();
    }

}
