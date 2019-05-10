package mediaplayer.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import mediaplayer.dao.PlaylistDAO;

/**
 * Class to validate input from the user.
 */
@NoArgsConstructor
@AllArgsConstructor
public class Validator {

    /**
     * A {@link PlaylistDAO} object.
     */
    private PlaylistDAO playlistDAO;

    /**
     * Checks if the given {@code playlist name} meets all the criteria to be considered as valid.
     * @param playlistName the name to be checked
     * @return whether the {@code playlist name} is valid or not
     */
    public boolean checkPlaylistName(String playlistName) {
        return checkLength(playlistName) && checkWhitespaces(playlistName) && checkUniqueness(playlistName);
    }

    /**
     * Checks if the given {@code playlist name} is valid. A {@code playlist name} is considered to be valid if it is between 2 and 50 characters.
     * @param playlistName the name to be checked
     * @return whether the {@code playlist name} is valid or not
     */
    public boolean checkLength(String playlistName) {
        return playlistName.length() >= 2 && playlistName.length() <= 50;
    }

    /**
     * Checks if the given {@code playlist name} is valid. A {@code playlist name} is considered to be valid if it contains neither leading nor trailing whitespaces.
     * @param playlistName the name to be checked
     * @return whether the {@code playlist name} is valid or not
     */
    public boolean checkWhitespaces(String playlistName) {
        return !playlistName.trim().isEmpty() && playlistName.trim().equals(playlistName);
    }

    /**
     * Checks if the given {@code playlist name} is valid. A {@code playlist name} is considered valid if no other playlist exists with the same name.
     * @param playlistName the name to be checked
     * @return whether the {@code playlist name} is valid or not
     */
    public boolean checkUniqueness(String playlistName) {
        return playlistDAO.getPlaylistByName(playlistName) == null;
    }

}
