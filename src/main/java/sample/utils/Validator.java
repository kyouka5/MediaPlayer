package sample.utils;

import sample.dao.PlaylistDAO;
import sample.dao.PlaylistDAOFactory;

public class Validator {
    private boolean isValid = true;

    private PlaylistDAO playlistDAO = PlaylistDAOFactory.getInstance().createPlaylistDAO();

    public Validator() {
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean checkPlaylistName(String playlistName) {
        if (playlistName.length() >= 2
                && playlistName.length() <= 50
                && !playlistName.trim().isEmpty()
                && playlistName.trim().equals(playlistName)
                && !playlistDAO.getPlaylistNames().contains(playlistName)) {
            return isValid;
        } else {
            return !isValid;
        }
    }

}
