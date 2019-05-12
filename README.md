# MediaPlayer
A simple media player written in Java. It allows users to organize their music/video files into playlists and also gives them the ability to play these files. The application keeps track of the number of times a file has been opened so it can present the most played files to the user.

The supported file formats are .mp3 and .mp4

## Usage
Execute either of the following commands in the main directory:
```
mvn package
java -jar target/mediaplayer-1.0.jar
```
or
```
mvn compile
mvn exec:java 
```

## Requirements
* JDK 11 or above
* Maven 3.0 or above

## Credits
* <a href=https://www.flaticon.com/authors/stephen-hutchings>Stephen Hutchings'</a> Typicons Icon Pack from <a href=https://www.flaticon.com> Flaticon</a>
* <a href=http://www.jthink.net/jaudiotagger>Jaudiotagger</a>
