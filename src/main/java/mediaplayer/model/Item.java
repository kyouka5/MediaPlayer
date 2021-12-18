package mediaplayer.model;

import com.google.common.base.Objects;
import lombok.*;
import mediaplayer.util.jpa.YearConverter;
import javax.persistence.*;
import java.time.Year;

/**
 * Class representing an item of a {@link Playlist}.
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Item {

    @Id
    @GeneratedValue
    private int id;

    /**
     * The absolute file path of the item.
     */
    private String path;

    /**
     * The name of the item.
     */
    private String name;

    /**
     * The title of the item.
     */
    private String title;

    /**
     * The artist of the item.
     */
    private String artist;

    /**
     * The album of the item.
     */
    private String album;

    /**
     * The release year of the item.
     */
    @Convert(converter = YearConverter.class)
    private Year year;

    /**
     * The genre of the item.
     */
    private String genre;

    /**
     * The number of views of the item.
     */
    private int numberOfViews;

    /**
     * The {@link Playlist} in which the item belongs to.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    /**
     * Increment the item's number of views by one.
     */
    public void incrementViews() {
        numberOfViews++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id == item.id && numberOfViews == item.numberOfViews && Objects.equal(path, item.path) && Objects.equal(name, item.name) && Objects.equal(title, item.title) && Objects.equal(artist, item.artist) && Objects.equal(album, item.album) && Objects.equal(year, item.year) && Objects.equal(genre, item.genre) && Objects.equal(playlist, item.playlist);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, path, name, title, artist, album, year, genre, numberOfViews, playlist);
    }
}
