package it.unitn.simob.howsthere.Oggetti;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by matteo on 17/05/18.
 */

@Dao
public interface Panorama_dao {

    @Insert
    void insert_panorama(Panorama_Database p);

    @Query("SELECT * FROM Panorama_Database")
    List<Panorama_Database> get_tutti();

    @Query("SELECT * FROM Panorama_Database WHERE ID =:id")
    Panorama_Database get_byID(int id);

    @Update
    void updateRecord(Panorama_Database p);

    @Delete
    void deleteRecord(Panorama_Database p);
}