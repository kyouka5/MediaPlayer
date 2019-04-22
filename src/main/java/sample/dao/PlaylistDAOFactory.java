package sample.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PlaylistDAOFactory implements AutoCloseable {

    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static PlaylistDAOFactory instance;

    public PlaylistDAOFactory() {
    }

    public static PlaylistDAOFactory getInstance() {
        if (instance == null) {
            instance = new PlaylistDAOFactory();
        }

        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("mediaplayer");
        }

        if (em == null) {
            em = emf.createEntityManager();
        }

        return instance;
    }

    public static void setInstance(PlaylistDAOFactory instance) {
        PlaylistDAOFactory.instance = instance;
    }

    public PlaylistDAO createPlaylistDAO() {

        return new PlaylistDAOImpl(em);

    }

    @Override
    public void close() {
        em.close();
        emf.close();
    }

}
