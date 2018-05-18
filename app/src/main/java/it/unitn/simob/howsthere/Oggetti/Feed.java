package it.unitn.simob.howsthere.Oggetti;

/**
 * Created by simob on 02/04/2018.
 */

/**
 * Classe per la gestione dei feed, poi andrà completata con il database usando le Room
 */
public class Feed {
    private String ID;
    private String name, location, imageUrl, panoramaID, timeStamp;
    private Integer likes;

    public Feed(){}

    public Feed(String name, String location, String imageUrl, String timeStamp) {
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
        this.timeStamp = timeStamp;
        this.likes = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPanoramaID() {
        return panoramaID;
    }

    public void setPanoramaID(String panoramaID) {
        this.panoramaID = panoramaID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }
}