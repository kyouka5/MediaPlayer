package sample.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sample.utils.Validator;

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
        Boolean validName1 = validator.checkPlaylistName("Hybrid Theory (2000)");
        Boolean invalidName1 = validator.checkPlaylistName("      Meteora");
        Boolean invalidName2 = validator.checkPlaylistName("      Minutes To Midnight      ");
        Boolean invalidName3 = validator.checkPlaylistName("          ");
        Boolean invalidName4 = validator.checkPlaylistName("And when I close my eyes tonight, To symphonies of blinding light!");

        assertTrue(validName1);
        assertFalse(invalidName1);
        assertFalse(invalidName2);
        assertFalse(invalidName3);
        assertFalse(invalidName4);
    }

    @Test
    public void lengthTest() {
        Boolean validName1 = validator.checkLength("Hybrid Theory (2000)");
        Boolean invalidName1 = validator.checkLength("a");
        Boolean invalidName2 = validator.checkLength("And when I close my eyes tonight, To symphonies of blinding light!");

        assertTrue(validName1);
        assertFalse(invalidName1);
        assertFalse(invalidName2);
    }

    @Test
    public void whitespaceTest() {
        Boolean validName1 = validator.checkLength("A Thousand Suns");
        Boolean invalidName1 = validator.checkWhitespaces("      Meteora");
        Boolean invalidName2 = validator.checkWhitespaces("      Minutes To Midnight      ");

        assertTrue(validName1);
        assertFalse(invalidName1);
        assertFalse(invalidName2);
    }
}
