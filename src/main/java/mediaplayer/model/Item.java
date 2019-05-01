package mediaplayer.model;
import javax.persistence.*;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    private String path;
    private String name;
    private String title;
    private String artist;
    private String album;
    private int year;
    private String genre;
    private int numberOfViews;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    public Item() {
    }

    public Item(String path, String name, String title, String artist, String album, int year, String genre, Playlist playlist, int numberOfViews) {
        this.path = path;
        this.name = name;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.genre = genre;
        this.playlist = playlist;
        this.numberOfViews = numberOfViews;
    }

    public static class Builder {
        private String path;
        private String name;
        private String title;
        private String artist;
        private String album;
        private int year;
        private String genre;
        private Playlist playlist;
        private int numberOfViews;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder artist(String artist) {
            this.artist = artist;
            return this;
        }

        public Builder album(String album) {
            this.album = album;
            return this;
        }

        public Builder year(int year) {
            this.year = year;
            return this;
        }

        public Builder genre(String genre) {
            this.genre = genre;
            return this;
        }

        public Builder playlist(Playlist playlist) {
            this.playlist = playlist;
            return this;
        }

        public Builder numberOfViews(int numberOfViews) {
            this.numberOfViews = numberOfViews;
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }

    private Item(Builder builder) {
        path = builder.path;
        name = builder.name;
        title = builder.title;
        artist = builder.artist;
        album = builder.album;
        year = builder.year;
        genre = builder.genre;
        playlist = builder.playlist;
        numberOfViews = builder.numberOfViews;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getNumberOfViews() {
        return numberOfViews;
    }

    public void setNumberOfViews(int numberOfViews) {
        this.numberOfViews = numberOfViews;
    }

    public void incrementViews() {
        numberOfViews++;
    }

}
