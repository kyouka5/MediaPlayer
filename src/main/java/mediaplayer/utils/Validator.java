package mediaplayer.utils;

import com.google.inject.Guice;
import com.google.inject.Injector;
import mediaplayer.dao.PersistenceModule;
import mediaplayer.dao.PlaylistDAO;

public class Validator {

    private Injector injector = Guice.createInjector(new PersistenceModule("mediaplayer"));
    private PlaylistDAO playlistDAO = injector.getInstance(PlaylistDAO.class);

    public Validator() {
    }

    public boolean checkPlaylistName(String playlistName) {
        return checkLength(playlistName) && checkWhitespaces(playlistName) && checkUniqueness(playlistName);
    }

    public boolean checkLength(String playlistName) {
        return playlistName.length() >= 2 && playlistName.length() <= 50;
    }

    public boolean checkWhitespaces(String playlistName) {
        return !playlistName.trim().isEmpty() && playlistName.trim().equals(playlistName);
    }

    public boolean checkUniqueness(String playlistName) {
        return !playlistDAO.getPlaylistNames().contains(playlistName);
    }

}
