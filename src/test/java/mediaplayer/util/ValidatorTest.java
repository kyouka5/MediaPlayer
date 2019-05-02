package mediaplayer.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mediaplayer.utils.Validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatorTest {
    private Validator validator;

    @BeforeEach
    public void setUp() {
        validator = new Validator();
    }

    public void tearDown() {
        validator = null;
    }

    @Test
    public void validationTest() {
        Boolean validName = validator.checkPlaylistName("Hybrid Theory (2000)");
        Boolean leadingWhitespaces = validator.checkPlaylistName("      Meteora");
        Boolean whitespacesBoth = validator.checkPlaylistName("      Minutes To Midnight      ");
        Boolean whitespacesOnly = validator.checkPlaylistName("          ");
        Boolean tooLong = validator.checkPlaylistName("And when I close my eyes tonight, To symphonies of blinding light!");

        assertTrue(validName);
        assertFalse(leadingWhitespaces);
        assertFalse(whitespacesBoth);
        assertFalse(whitespacesOnly);
        assertFalse(tooLong);
    }

    @Test
    public void lengthTest() {
        Boolean validName = validator.checkLength("Hybrid Theory (2000)");
        Boolean tooShort = validator.checkLength("a");
        Boolean tooLong = validator.checkLength("And when I close my eyes tonight, To symphonies of blinding light!");

        assertTrue(validName);
        assertFalse(tooShort);
        assertFalse(tooLong);
    }

    @Test
    public void whitespaceTest() {
        Boolean validName = validator.checkLength("A Thousand Suns");
        Boolean leadingWhitespaces = validator.checkWhitespaces("      Meteora");
        Boolean whitespacesBoth = validator.checkWhitespaces("      Minutes To Midnight      ");

        assertTrue(validName);
        assertFalse(leadingWhitespaces);
        assertFalse(whitespacesBoth);
    }
}
