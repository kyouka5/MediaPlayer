package mediaplayer.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatorTest {
    private Validator validator;

    @BeforeEach
    public void setUp() {
        validator = new Validator();
    }

    @AfterEach
    public void tearDown() {
        validator = null;
    }

    @Test
    public void validationTest() {
        boolean validName = validator.checkPlaylistName("Hybrid Theory (2000)");
        boolean leadingWhitespaces = validator.checkPlaylistName("      Meteora");
        boolean whitespacesBoth = validator.checkPlaylistName("      Minutes To Midnight      ");
        boolean whitespacesOnly = validator.checkPlaylistName("          ");
        boolean tooLong = validator.checkPlaylistName("And when I close my eyes tonight, To symphonies of blinding light");

        assertTrue(validName);
        assertFalse(leadingWhitespaces);
        assertFalse(whitespacesBoth);
        assertFalse(whitespacesOnly);
        assertFalse(tooLong);
    }

    @Test
    public void lengthTest() {
        boolean validName = validator.checkLength("Hybrid Theory (2000)");
        boolean tooShort = validator.checkLength("a");
        boolean tooLong = validator.checkLength("And when I close my eyes tonight, To symphonies of blinding light!");

        assertTrue(validName);
        assertFalse(tooShort);
        assertFalse(tooLong);
    }

    @Test
    public void whitespaceTest() {
        boolean validName = validator.checkLength("A Thousand Suns");
        boolean leadingWhitespaces = validator.checkWhitespaces("      Meteora");
        boolean whitespacesBoth = validator.checkWhitespaces("      Minutes To Midnight      ");

        assertTrue(validName);
        assertFalse(leadingWhitespaces);
        assertFalse(whitespacesBoth);
    }

    @Test
    public void uniquenessTest() {
        boolean uniqueName = validator.checkUniqueness("      Meteora");
        boolean uniqueName2 = validator.checkUniqueness("a");
        assertTrue(uniqueName);
        assertTrue(uniqueName2);
    }
}
