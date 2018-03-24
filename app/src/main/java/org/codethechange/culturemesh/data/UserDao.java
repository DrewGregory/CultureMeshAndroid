package org.codethechange.culturemesh.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.codethechange.culturemesh.models.User;

/**
 * Created by Drew Gregory on 2/19/18.
 */
@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addUser(User user);

    @Query("SELECT * FROM user WHERE id = :id")
    public User getUser(long id);

}
