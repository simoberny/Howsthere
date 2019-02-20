package it.unitn.lpmt.howsthere.Oggetti;

/**
 * Created by simob on 02/04/2018.
 */

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe per la gestione dei feed, poi andrà completata con il database usando le Room
 */
public class Feed {
    private String ID, uid, name, location, imageUrl, panoramaID, descrizione, file_name;
    private long timeStamp;
    private String p;
    private Integer likes;
    private List<String> likes_id;

    public Feed(){}

    public Feed(String UID, String name, String location, String imageUrl, long timeStamp, String filename, String descrizione) {
        this.uid = UID;
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
        this.timeStamp = timeStamp;
        this.likes = 0;
        this.likes_id = new ArrayList<String>();
        this.file_name = filename;
        this.descrizione = descrizione;
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

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
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

    public void remove_user_to_like(String id){
        this.likes_id.remove(id);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFile_name() {
        return file_name;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }
}