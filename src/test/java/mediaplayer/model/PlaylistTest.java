package mediaplayer.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlaylistTest {
    private Playlist playlist;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    public void setUp() {
        playlist = new Playlist();

        item1 = new Item();
        item2 = new Item();
        item3 = new Item();

        item1.setId(1);
        item2.setId(2);
        item3.setId(3);

        List<Item> contents = new ArrayList<>();
        contents.add(item1);
        contents.add(item2);
        contents.add(item3);
        playlist.setContents(contents);
    }

    @AfterEach
    public void tearDown() {
        playlist = null;
        item1 = null;
        item2 = null;
        item3 = null;
    }

    @Test
    public void testNextItem() {
        Item next = playlist.getNextItem(item2);
        assertEquals(item3, next);

        Item shouldBeNull = playlist.getNextItem(item3);
        assertEquals(null, shouldBeNull);
    }

    @Test
    public void testPreviousItem() {
        Item previous = playlist.getPreviousItem(item2);
        assertEquals(item1, previous);

        Item shouldBeNull = playlist.getPreviousItem(item1);
        assertEquals(null, shouldBeNull);
    }

    @Test
    public void testShuffle() {
        Playlist shouldReturnOriginal = playlist;
        shouldReturnOriginal.shufflePlaylist();
        shouldReturnOriginal.unshufflePlaylist();
        assertEquals(playlist, shouldReturnOriginal);
    }
}

