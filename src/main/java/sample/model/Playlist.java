package sample.model;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@XmlRootElement(name = "playlist")
//@XmlType(propOrder = {"name", "contents"})
@Entity
public class Playlist {

//    @Column(name = "playlist_pk")
    private int id;


    private String name;

//    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "playlist_id")
//@OneToMany(
//        mappedBy = "playlist",
//        cascade = CascadeType.ALL,
//        orphanRemoval = true
//)
    private List<Item> contents;


    public Playlist() {
    }

    public Playlist(String name, List<Item> contents) {
        this.name = name;
        this.contents = contents;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(length = 50, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "playlist", orphanRemoval=true)
    public List<Item> getContents() {
        return contents;
    }

//    @XmlElementWrapper(name = "contents")
//    @XmlElement(name = "item")
    public void setContents(List<Item> contents) {
        this.contents = contents;
    }


    public void addContent(List<Item> contents) {
        if (getContents() == null) {
            setContents(contents);
        } else {
            setContents(Stream.concat(getContents().stream(), contents.stream()).collect(Collectors.toList()));
        }
    }

}
