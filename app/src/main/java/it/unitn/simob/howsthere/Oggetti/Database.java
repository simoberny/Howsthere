package it.unitn.simob.howsthere.Oggetti;


import android.arch.persistence.room.RoomDatabase;

/**
 * Created by matteo on 17/05/18.
 */

@android.arch.persistence.room.Database(entities = {Panorama_Database.class},version = 1)
public abstract class Database extends RoomDatabase {
    public abstract Panorama_dao daoAccess();

}