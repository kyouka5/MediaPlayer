package mediaplayer.model;

import com.google.common.base.Objects;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.*;

import javax.persistence.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Class representing a playlist.
 */
@Getter
@Setter
@ToString
@Builder
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

    public Playlist(@NonNull String name, List<Item> contents) {
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
        }
        return null;
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
        }
        return null;
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
        contents.sort(Comparator.comparingInt(Item::getId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return id == playlist.id && currentIndex == playlist.currentIndex && Objects.equal(name, playlist.name) && Objects.equal(contents, playlist.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, contents, currentIndex);
    }
}
