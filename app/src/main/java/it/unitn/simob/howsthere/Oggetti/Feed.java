package it.unitn.simob.howsthere.Oggetti;

/**
 * Created by simob on 02/04/2018.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Classe per la gestione dei feed, poi andrà completata con il database usando le Room
 */
public class Feed {
    private String ID;
    private String uid;
    private String name, location, imageUrl, panoramaID, timeStamp;
    private Integer likes;
    private List<String> likes_id;

    public Feed(){}

    public Feed(String UID, String name, String location, String imageUrl, String timeStamp) {
        this.uid = UID;
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
        this.timeStamp = timeStamp;
        this.likes = 0;
        this.likes_id = new ArrayList<String>();
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

    public List<String> getLikes_id() {
        return likes_id;
    }

    public void setLikes_id(List<String> likes_id) {
        this.likes_id = likes_id;
    }

    public void add_user_to_like(String id){
        this.likes_id.add(id);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}