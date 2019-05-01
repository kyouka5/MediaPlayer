package sample.model;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.*;

@Entity
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(length = 50, unique = true)
    @Min(2)
    @Max(50)
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "playlist", orphanRemoval=true)
    private List<Item> contents;

    @Transient
    private int currentIndex;

    public Playlist() {
    }

    public Playlist(String name, List<Item> contents) {
        this.name = name;
        this.contents = contents;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Item> getContents() {
        return contents;
    }

    public void setContents(List<Item> contents) {
        this.contents = contents;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public Item getNextItem(Item currentlyPlaying) {
        currentIndex = contents.indexOf(currentlyPlaying);
        if (currentIndex < contents.size() - 1) {
            return contents.get(currentIndex + 1);
        } else {
            return null;
        }
    }

    public Item getPreviousItem(Item currentlyPlaying) {
        currentIndex = contents.indexOf(currentlyPlaying);
        if (currentIndex > 0) {
            return contents.get(currentIndex - 1);
        } else {
            return null;
        }
    }

    public Item getItemByPath(String path) {
        return contents.stream().filter(e -> e.getPath().equals(path)).findFirst().orElse(null);
    }

    public void shufflePlaylist() {
        Collections.shuffle(contents);
    }

    public void unshufflePlaylist() {
        Collections.sort(contents, Comparator.comparingInt(Item::getId));
    }

}
