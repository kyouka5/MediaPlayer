package mediaplayer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.*;

/**
 * Class representing a playlist.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Playlist {

    @Id
    @GeneratedValue
    private int id;

    /**
     * The name of the playlist.
     */
    @Column(length = 50, unique = true)
    @Min(2)
    @Max(50)
    private String name;

    /**
     * The {@link Item}s of a playlist.
     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "playlist", orphanRemoval = true)
    private List<Item> contents;

    /**
     * The index of the currently playing {@link Item}.
     */
    @Transient
    private int currentIndex;

    public Playlist(String name, List<Item> contents) {
        this.name = name;
        this.contents = contents;
    }

    /**
     * Gets the next {@link Item} of the playlist.
     *
     * @param currentlyPlaying the {@link Item} which is currently playing
     * @return the next {@link Item} of the playlist
     */
    public Item getNextItem(Item currentlyPlaying) {
        currentIndex = contents.indexOf(currentlyPlaying);
        if (currentIndex < contents.size() - 1) {
            return contents.get(currentIndex + 1);
        } else {
            return null;
        }
    }

    /**
     * Gets the previous {@link Item} of the playlist.
     *
     * @param currentlyPlaying the {@link Item} which is currently playing
     * @return the previous {@link Item} of the playlist
     */
    public Item getPreviousItem(Item currentlyPlaying) {
        currentIndex = contents.indexOf(currentlyPlaying);
        if (currentIndex > 0) {
            return contents.get(currentIndex - 1);
        } else {
            return null;
        }
    }

    /**
     * Shuffles the playlist.
     */
    public void shufflePlaylist() {
        Collections.shuffle(contents);
    }

    /**
     * Restores the original order of the playlist.
     */
    public void unshufflePlaylist() {
        Collections.sort(contents, Comparator.comparingInt(Item::getId));
    }

}
