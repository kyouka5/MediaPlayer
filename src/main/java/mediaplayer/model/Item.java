package mediaplayer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Class representing an item of a {@link Playlist}.
 */
@Data
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
    private int year;

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

}
