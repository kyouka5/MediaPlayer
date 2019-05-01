package sample.utils;

import com.google.inject.Guice;
import com.google.inject.Injector;
import sample.dao.PersistenceModule;
import sample.dao.PlaylistDAO;

public class Validator {
    private boolean valid;

    private Injector injector = Guice.createInjector(new PersistenceModule("mediaplayer"));
    private PlaylistDAO playlistDAO = injector.getInstance(PlaylistDAO.class);

    public Validator() {
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean checkPlaylistName(String playlistName) {
        if (checkLength(playlistName) && checkWhitespaces(playlistName) && checkUniqueness(playlistName)) {
            return valid = true;
        } else {
            return valid = false;
        }
    }

    public boolean checkLength(String playlistName) {
        if (playlistName.length() >= 2 && playlistName.length() <= 50) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkWhitespaces(String playlistName) {
        if (!playlistName.trim().isEmpty() && playlistName.trim().equals(playlistName)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkUniqueness(String playlistName) {
        if (!playlistDAO.getPlaylistNames().contains(playlistName)) {
            return true;
        } else {
            return false;
        }
    }

}
