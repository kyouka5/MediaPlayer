# MediaPlayer
Simple media player application written in Java. It allows users to organize their music/video files into playlists and also gives them the ability to play these files. The application keeps track of the number of times a file has been opened, so it can present the most played files to the user.

Supported file formats are `.mp3` and `.mp4`

## Usage
The application uses a MySQL database to store data about playlists and music/video files. I recommend creating a [RemoteMySQL database](https://remotemysql.com/) for this purpose. After creating a database, you need to add your login credentials to the `db.properties` file in the following way:
```
db.url = jdbc:mysql://remotemysql.com:3306/DATABASE_NAME
db.user = USERNAME
db.password = PASSWORD
```

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
* JDK 15 or above
* Maven 3.0 or above

## Credits
* [Typicons Icon Pack](https://icon-icons.com/pack/Typicons/1144)
* [Jaudiotagger](http://www.jthink.net/jaudiotagger)
