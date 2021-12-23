package mediaplayer.util;

import mediaplayer.dao.PlaylistDAO;
import mediaplayer.model.Playlist;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatorTest {
    private Validator validator;
    private PlaylistDAO playlistDAO;

    @BeforeEach
    public void setUp() {
        playlistDAO = Mockito.mock(PlaylistDAO.class);
        validator = new Validator(playlistDAO);
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testCheckPlaylistNameShouldReturnTrueWhenNameIsValid() {
        var validName = "Hunting Party";
        Mockito.when(playlistDAO.getPlaylistByName(validName)).thenReturn(Optional.empty());
        boolean validationResult = validator.checkPlaylistName(validName);

        Mockito.verify(playlistDAO).getPlaylistByName(validName);
        Mockito.verifyNoMoreInteractions(playlistDAO);
        assertTrue(validationResult);
    }

    @Test
    public void testCheckPlaylistNameShouldReturnFalseWhenNameHasLeadingAndTrailingWhitespaces() {
        var nameWithWhitespaces = "      Minutes To Midnight      ";
        boolean validationResult = validator.checkPlaylistName(nameWithWhitespaces);

        Mockito.verifyNoInteractions(playlistDAO);
        assertFalse(validationResult);
    }

    @Test
    public void testCheckPlaylistNameShouldReturnFalseWhenNameIsTooLong() {
        var tooLongName = "And when I close my eyes tonight, to symphonies of blinding light";
        boolean validationResult = validator.checkPlaylistName(tooLongName);

        Mockito.verifyNoInteractions(playlistDAO);
        assertFalse(validationResult);
    }

    @Test
    public void testCheckPlaylistNameShouldReturnFalseWhenNameIsNotUnique() {
        var notUniqueName = "Hybrid Theory (2000)";
        Playlist playlistWithGivenName = Playlist.builder().name(notUniqueName).build();
        Mockito.when(playlistDAO.getPlaylistByName(notUniqueName)).thenReturn(Optional.of(playlistWithGivenName));
        boolean validationResult = validator.checkUniqueness(notUniqueName);

        Mockito.verify(playlistDAO).getPlaylistByName(notUniqueName);
        Mockito.verifyNoMoreInteractions(playlistDAO);
        assertFalse(validationResult);
    }

    @Test
    public void testCheckLengthShouldReturnTrueWhenNameHasValidLength() {
        var validName = "Hybrid Theory (2000)";
        boolean validationResult = validator.checkLength(validName);

        Mockito.verifyNoInteractions(playlistDAO);
        assertTrue(validationResult);
    }

    @Test
    public void testCheckLengthShouldReturnFalseWhenNameIsTooShort() {
        var tooShortName = "a";
        boolean validationResult = validator.checkLength(tooShortName);

        Mockito.verifyNoInteractions(playlistDAO);
        assertFalse(validationResult);
    }

    @Test
    public void testCheckLengthShouldReturnFalseWhenNameIsTooLong() {
        var tooShortName = "And when I close my eyes tonight, To symphonies of blinding light";
        boolean validationResult = validator.checkLength(tooShortName);

        Mockito.verifyNoInteractions(playlistDAO);
        assertFalse(validationResult);
    }

    @Test
    public void testCheckWhitespacesShouldReturnTrueWhenNameHasNoLeadingOrTrailingWhitespaces() {
        var validName = "A Thousand Suns";
        boolean validationResult = validator.checkWhitespaces(validName);

        Mockito.verifyNoInteractions(playlistDAO);
        assertTrue(validationResult);
    }

    @Test
    public void testCheckWhitespacesShouldReturnFalseWhenNameHasLeadingWhitespaces() {
        var nameWithLeadingWhitespaces = "      Meteora";
        boolean validationResult = validator.checkWhitespaces(nameWithLeadingWhitespaces);

        Mockito.verifyNoInteractions(playlistDAO);
        assertFalse(validationResult);
    }

    @Test
    public void testCheckWhitespacesShouldReturnFalseWhenNameHasTrailingWhitespaces() {
        var nameWithTrailingWhitespaces = "Reanimation      ";
        boolean validationResult = validator.checkWhitespaces(nameWithTrailingWhitespaces);

        Mockito.verifyNoInteractions(playlistDAO);
        assertFalse(validationResult);
    }

    @Test
    public void testCheckWhitespacesShouldReturnFalseWhenNameHasLeadingAndTrailingWhitespaces() {
        var nameWithWhitespaces = "      Minutes To Midnight      ";
        boolean validationResult = validator.checkWhitespaces(nameWithWhitespaces);

        Mockito.verifyNoInteractions(playlistDAO);
        assertFalse(validationResult);
    }

    @Test
    public void testCheckUniquenessShouldReturnTrueWhenNameIsUnique() {
        var uniqueName = "Hybrid Theory (2000)";
        Mockito.when(playlistDAO.getPlaylistByName(uniqueName)).thenReturn(Optional.empty());
        boolean validationResult = validator.checkUniqueness(uniqueName);

        Mockito.verify(playlistDAO).getPlaylistByName(uniqueName);
        Mockito.verifyNoMoreInteractions(playlistDAO);
        assertTrue(validationResult);
    }

    @Test
    public void testCheckUniquenessShouldReturnFalseWhenNameIsNotUnique() {
        var notUniqueName = "Hybrid Theory";
        Playlist playlistWithGivenName = Playlist.builder().name(notUniqueName).build();
        Mockito.when(playlistDAO.getPlaylistByName(notUniqueName)).thenReturn(Optional.of(playlistWithGivenName));
        boolean validationResult = validator.checkUniqueness(notUniqueName);

        Mockito.verify(playlistDAO).getPlaylistByName(notUniqueName);
        Mockito.verifyNoMoreInteractions(playlistDAO);
        assertFalse(validationResult);
    }
}
