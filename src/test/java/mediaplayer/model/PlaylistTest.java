package mediaplayer.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
    }

    @Test
    public void testGetNextItemShouldReturnTheNextItemWhenItExists() {
        Optional<Item> next = playlist.getNextItem(item2);
        next.ifPresentOrElse(nextItem -> assertEquals(item3, nextItem), Assertions::fail);
    }

    @Test
    public void testGetNextItemShouldReturnEmptyWhenItDoesNotExist() {
        Optional<Item> emptyItem = playlist.getNextItem(item3);
        assertTrue(emptyItem.isEmpty());
    }

    @Test
    public void testGetPreviousItemShouldReturnThePreviousItemWhenItExists() {
        Optional<Item> previous = playlist.getPreviousItem(item2);
        previous.ifPresentOrElse(previousItem -> assertEquals(item1, previousItem), Assertions::fail);
    }

    @Test
    public void testGetPreviousItemShouldReturnEmptyWhenItDoesNotExist() {
        Optional<Item> emptyItem = playlist.getPreviousItem(item1);
        assertTrue(emptyItem.isEmpty());
    }

    @Test
    public void testShufflePlaylistShouldShuffleTheItems() {
        var originalListOfItems = List.copyOf(playlist.getContents());
        playlist.shufflePlaylist();
        assertNotEquals(originalListOfItems, playlist.getContents());
    }

    @Test
    public void testUnshufflePlaylistShouldUnshuffleTheItems() {
        var originalListOfItems = List.copyOf(playlist.getContents());
        playlist.shufflePlaylist();
        playlist.unshufflePlaylist();
        assertEquals(originalListOfItems, playlist.getContents());
    }
}

