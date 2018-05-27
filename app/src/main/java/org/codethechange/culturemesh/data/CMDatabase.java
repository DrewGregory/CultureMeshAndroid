package org.codethechange.culturemesh.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import org.codethechange.culturemesh.models.City;
import org.codethechange.culturemesh.models.Country;
import org.codethechange.culturemesh.models.DatabaseNetwork;
import org.codethechange.culturemesh.models.Event;
import org.codethechange.culturemesh.models.Language;
import org.codethechange.culturemesh.models.Place;
import org.codethechange.culturemesh.models.Post;
import org.codethechange.culturemesh.models.PostReply;
import org.codethechange.culturemesh.models.Region;
import org.codethechange.culturemesh.models.User;

/**
 * Created by Drew Gregory on 2/19/18.
 * TODO: Check out LiveData: https://developer.android.com/reference/android/arch/lifecycle/LiveData.html
 */
@Database(version = 16, entities = {DatabaseNetwork.class, User.class, Post.class, Event.class,
        EventSubscription.class, NetworkSubscription.class, Region.class, City.class, Country.class,
        Place.class, Language.class, PostReply.class})
abstract public class CMDatabase extends RoomDatabase{


    abstract public NetworkDao networkDao();
    abstract public UserDao userDao();
    abstract public EventDao eventDao();
    abstract public PostDao postDao();
    abstract public EventSubscriptionDao eventSubscriptionDao();
    abstract public NetworkSubscriptionDao networkSubscriptionDao();
    abstract public CountryDao countryDao();
    abstract public RegionDao regionDao();
    abstract public CityDao cityDao();
    abstract public PostReplyDao postReplyDao();
    abstract public LanguageDao languageDao();
}
