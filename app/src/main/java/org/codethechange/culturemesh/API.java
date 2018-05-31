package org.codethechange.culturemesh;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.codethechange.culturemesh.data.CMDatabase;
import org.codethechange.culturemesh.data.CityDao;
import org.codethechange.culturemesh.data.CountryDao;
import org.codethechange.culturemesh.data.EventDao;
import org.codethechange.culturemesh.data.EventSubscription;
import org.codethechange.culturemesh.data.EventSubscriptionDao;
import org.codethechange.culturemesh.data.LanguageDao;
import org.codethechange.culturemesh.data.NetworkDao;
import org.codethechange.culturemesh.data.NetworkSubscription;
import org.codethechange.culturemesh.data.NetworkSubscriptionDao;
import org.codethechange.culturemesh.data.PostDao;
import org.codethechange.culturemesh.data.PostReplyDao;
import org.codethechange.culturemesh.data.RegionDao;
import org.codethechange.culturemesh.data.UserDao;
import org.codethechange.culturemesh.models.City;
import org.codethechange.culturemesh.models.Country;
import org.codethechange.culturemesh.models.DatabaseNetwork;
import org.codethechange.culturemesh.models.Event;
import org.codethechange.culturemesh.models.FromLocation;
import org.codethechange.culturemesh.models.Language;
import org.codethechange.culturemesh.models.Location;
import org.codethechange.culturemesh.models.NearLocation;
import org.codethechange.culturemesh.models.Network;
import org.codethechange.culturemesh.models.Place;
import org.codethechange.culturemesh.models.Point;
import org.codethechange.culturemesh.models.Post;
import org.codethechange.culturemesh.models.PostReply;
import org.codethechange.culturemesh.models.Region;
import org.codethechange.culturemesh.models.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/*
TODO: USE ALARMS FOR UPDATING DATA ON SUBSCRIBED NETWORKS
TODO: Figure out how we can handle trying to update data.
TODO: Figure out alternative to id's other than longs and ints, which cannot represent all numbers. (Maybe just use strings?)
     - Perhaps check if it comes from subscribed network, if not do network request instead of cache?
 */

/**
 * This API serves as the interface between the rest of the app and both the local database and the
 * CultureMesh servers. When another part of the app needs to request information, it calls API
 * methods to obtain it. Similarly, API methods should be used to store, send, and update
 * information. The API then handles searching local caches for the information, requesting it from
 * the CultureMesh servers, and updating the local cache. The local cache allows for offline access
 * to limited information.
 *
 * For simplicity, we store the id's of other model objects in the database, not the objects
 * themselves. Thus, when we return these objects, we need to instantiate them with the methods
 * provided in this class.
 *
 * IMPORTANT: If you want to use this class in your activity, make sure you run API.loadAppDatabase()
 * at the beginning of onPreExecute()/doInBackground(), and API.closeDatabase() in onPostExecute().
 * The app will crash otherwise.
 *
 */

class API {
    static final String SETTINGS_IDENTIFIER = "acmsi";
    static final String PERSONAL_NETWORKS = "pernet";
    static final String SELECTED_NETWORK = "selnet";
    final static String SELECTED_USER="seluser";
    final static String FIRST_TIME = "firsttime";
    static final boolean NO_JOINED_NETWORKS = false;
    static final String CURRENT_USER = "curruser";
    static final String API_URL_BASE = "https://www.culturemesh.com/api-dev/v1/";
    static CMDatabase mDb;
    //reqCounter to ensure that we don't close the database while another thread is using it.
    static int reqCounter;


    /**
     * Add users to the database by parsing the JSON stored in {@code rawDummy}. In case of any
     * errors, the stack trace is printed to the console.
     */
    static void addUsers(){
        String rawDummy = "\n" +
                "[\n" +
                "  {\n" +
                "    \"username\": \"boonekathryn\",\n" +
                "    \"fp_code\": \"IDK\",\n" +
                "    \"confirmed\": false,\n" +
                "    \"user_id\": 1,\n" +
                "    \"firstName\": \"Jonathan\",\n" +
                "    \"act_code\": \"IDK\",\n" +
                "    \"lastName\": \"Simpson\",\n" +
                "    \"about_me\": \"Adipisci ad molestiae vel fugit dolor in. Dolore ipsa libero. Doloremque dolor itaque enim. Saepe nam odit.\\nSimilique commodi ex quam quae vel in rerum. Esse nesciunt sunt sed magnam nihil.\",\n" +
                "    \"img_link\": \"https://lorempixel.com/200/200/\",\n" +
                "    \"role\": 1,\n" +
                "    \"gender\": \"female\",\n" +
                "    \"last_login\": \"2017-11-21 13:21:47\",\n" +
                "    \"registerDate\": \"2017-02-21 11:53:30\",\n" +
                "    \"email\": \"kristina00@reynolds.com\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"username\": \"williamosborne\",\n" +
                "    \"fp_code\": \"IDK\",\n" +
                "    \"confirmed\": true,\n" +
                "    \"user_id\": 2,\n" +
                "    \"firstName\": \"Abigail\",\n" +
                "    \"act_code\": \"IDK\",\n" +
                "    \"lastName\": \"Long\",\n" +
                "    \"about_me\": \"Doloremque maiores veritatis neque itaque earum molestias. Ab quos reprehenderit suscipit qui repellat maxime natus. Animi sit necessitatibus dicta magnam.\",\n" +
                "    \"img_link\": \"https://dummyimage.com/882x818\",\n" +
                "    \"role\": 1,\n" +
                "    \"gender\": \"female\",\n" +
                "    \"last_login\": \"2017-11-25 08:22:44\",\n" +
                "    \"registerDate\": \"2017-11-09 00:46:13\",\n" +
                "    \"email\": \"williamsjade@watkins.com\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"username\": \"rscott\",\n" +
                "    \"fp_code\": \"IDK\",\n" +
                "    \"confirmed\": false,\n" +
                "    \"user_id\": 3,\n" +
                "    \"firstName\": \"Emily\",\n" +
                "    \"act_code\": \"IDK\",\n" +
                "    \"lastName\": \"Miller\",\n" +
                "    \"about_me\": \"Quae iste eum labore. Harum in deleniti.\\nMagni distinctio non repellendus accusantium accusantium corporis. Cum provident perspiciatis molestias dolore voluptate reiciendis.\",\n" +
                "    \"img_link\": \"https://picsum.photos/400\",\n" +
                "    \"role\": 1,\n" +
                "    \"gender\": \"male\",\n" +
                "    \"last_login\": \"2017-11-23 15:31:51\",\n" +
                "    \"registerDate\": \"2015-08-09 13:56:37\",\n" +
                "    \"email\": \"camposjohn@weber.com\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"username\": \"ihudson\",\n" +
                "    \"fp_code\": \"IDK\",\n" +
                "    \"confirmed\": false,\n" +
                "    \"user_id\": 4,\n" +
                "    \"firstName\": \"Mindy\",\n" +
                "    \"act_code\": \"IDK\",\n" +
                "    \"lastName\": \"Beltran\",\n" +
                "    \"about_me\": \"Eos libero eveniet sit tempore accusantium. Eveniet voluptates quos excepturi.\\nSaepe ut exercitationem sunt porro laborum. Doloremque quas assumenda cumque tenetur distinctio quis nobis.\",\n" +
                "    \"img_link\": \"https://picsum.photos/200\",\n" +
                "    \"role\": 1,\n" +
                "    \"gender\": \"male\",\n" +
                "    \"last_login\": \"2017-11-21 07:32:34\",\n" +
                "    \"registerDate\": \"2016-05-08 21:44:02\",\n" +
                "    \"email\": \"nicholas32@hotmail.com\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"username\": \"oellis\",\n" +
                "    \"fp_code\": \"IDK\",\n" +
                "    \"confirmed\": false,\n" +
                "    \"user_id\": 5,\n" +
                "    \"firstName\": \"Kristin\",\n" +
                "    \"act_code\": \"IDK\",\n" +
                "    \"lastName\": \"Carey\",\n" +
                "    \"about_me\": \"Aut tenetur fugiat voluptas tenetur eos ducimus. Aut officiis aliquam ab voluptatem.\\nIpsum et aperiam totam voluptas voluptas. Dolor nulla voluptatem molestiae.\",\n" +
                "    \"img_link\": \"https://picsum.photos/400\",\n" +
                "    \"role\": 1,\n" +
                "    \"gender\": \"male\",\n" +
                "    \"last_login\": \"2017-11-21 03:03:05\",\n" +
                "    \"registerDate\": \"2015-06-26 12:19:38\",\n" +
                "    \"email\": \"christopher18@allen.com\"\n" +
                "  }\n" +
                "]";
        try {
            UserDao uDAo = mDb.userDao();
            JSONArray usersJSON = new JSONArray(rawDummy);
            for (int i = 0; i < usersJSON.length(); i++) {
                JSONObject userJSON = usersJSON.getJSONObject(i);
                User user = new User(userJSON.getLong("user_id"), userJSON.getString("firstName"),
                        userJSON.getString("lastName"), userJSON.getString("email"),
                        userJSON.getString("username"), userJSON.getString("img_link"),
                        userJSON.getString("about_me"));
                uDAo.addUser(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add networks to the database by parsing the JSON stored in {@code rawDummy}. In case of any
     * errors, the stack trace is printed to the console. The JSON is split apart into the JSON
     * fragments for each city. Each of those JSONs are then passed to a {@link City}
     * constructor (specifically {@link DatabaseNetwork#DatabaseNetwork(JSONObject)}), which
     * extracts the necessary information and initializes itself. Those objects are then added to
     * the database via {@link NetworkDao}.
     */
    static void addNetworks() {
        String rawDummy = "[\n" +
                "    {\n" +
                "      \"id\": 0,\n" +
                "      \"location_cur\": {\n" +
                "        \"country_id\": 1,\n" +
                "        \"region_id\": 1,\n" +
                "        \"city_id\": 2\n" +
                "      },\n" +
                "      \"location_origin\": {\n" +
                "        \"country_id\": 1,\n" +
                "        \"region_id\": 2,\n" +
                "        \"city_id\": 3\n" +
                "      },\n" +
                "      \"language_origin\": {\n" +
                "        \"id\": 2,\n" +
                "        \"name\": \"entish\",\n" +
                "        \"num_speakers\": 10,\n" +
                "        \"added\": 0\n" +
                "      },\n" +
                "      \"network_class\": 1,\n" +
                "      \"date_added\": \"1990-11-23 15:31:51\",\n" +
                "      \"img_link\": \"img1.png\"\n" +
                "    },\n" +
                "\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"location_cur\": {\n" +
                "        \"country_id\": 2,\n" +
                "        \"region_id\": 4,\n" +
                "        \"city_id\": 6\n" +
                "      },\n" +
                "      \"location_origin\": {\n" +
                "        \"country_id\": 1,\n" +
                "        \"region_id\": 1,\n" +
                "        \"city_id\": 1\n" +
                "      },\n" +
                "      \"language_origin\": {\n" +
                "        \"id\": 3,\n" +
                "        \"name\": \"valarin\",\n" +
                "        \"num_speakers\": 1000,\n" +
                "        \"added\": 0\n" +
                "      },\n" +
                "      \"network_class\": 0,\n" +
                "      \"date_added\": \"1995-11-23 15:31:51\",\n" +
                "      \"img_link\": \"img2.png\"\n" +
                "    },\n" +
                "\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"location_cur\": {\n" +
                "        \"country_id\": 2,\n" +
                "        \"region_id\": null,\n" +
                "        \"city_id\": null\n" +
                "      },\n" +
                "      \"location_origin\": {\n" +
                "        \"country_id\": 1,\n" +
                "        \"region_id\": null,\n" +
                "        \"city_id\": 1\n" +
                "      },\n" +
                "      \"language_origin\": {\n" +
                "        \"id\": 3,\n" +
                "        \"name\": \"valarin\",\n" +
                "        \"num_speakers\": 1000,\n" +
                "        \"added\": 0\n" +
                "      },\n" +
                "      \"network_class\": 0,\n" +
                "      \"date_added\": \"1995-11-23 15:31:51\",\n" +
                "      \"img_link\": \"img2.png\"\n" +
                "    }\n" +
                "]\n";
        try {
            NetworkDao nDAo = mDb.networkDao();
            JSONArray usersJSON = new JSONArray(rawDummy);
            for (int i = 0; i < usersJSON.length(); i++) {
                Log.i("API.addNetworks()", "Start adding network number (not ID) " + i +
                        " from JSON to Dao");
                JSONObject netJSON = usersJSON.getJSONObject(i);
                DatabaseNetwork dn = new DatabaseNetwork(netJSON);
                Log.i("API.addNetworks()", "Adding network " + dn + " to Dao");
                nDAo.insertNetworks(dn);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add regions to the database by parsing the JSON stored in {@code rawDummy}. In case of any
     * errors, the stack trace is printed to the console. The JSON is split apart into the JSON
     * fragments for each region. Each of those JSONs are then passed to a {@link Region}
     * constructor (specifically {@link Region#Region(JSONObject)}), which extracts the necessary
     * information and initializes itself. Those objects are then added to the database via
     * {@link RegionDao}.
     */
    static void addRegions(){
        String rawDummy = "[\n" +
                "  {\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"north\",\n" +
                "    \"latitude\": 5000.4321,\n" +
                "    \"longitude\": 1000.1234,\n" +
                "    \"country_id\": 1,\n" +
                "    \"country_name\": \"corneria\",\n" +
                "    \"population\": 400000,\n" +
                "    \"feature_code\": \"string\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 2,\n" +
                "    \"name\": \"south\",\n" +
                "    \"latitude\": 4000.4321,\n" +
                "    \"longitude\": 1000.1234,\n" +
                "    \"country_id\": 1,\n" +
                "    \"country_name\": \"corneria\",\n" +
                "    \"population\": 600000,\n" +
                "    \"feature_code\": \"string\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 3,\n" +
                "    \"name\": \"east\",\n" +
                "    \"latitude\": 50.100,\n" +
                "    \"longitude\": 250.200,\n" +
                "    \"country_id\": 2,\n" +
                "    \"country_name\": \"rohan\",\n" +
                "    \"population\": 300,\n" +
                "    \"feature_code\": \"idk\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 4,\n" +
                "    \"name\": \"west\",\n" +
                "    \"latitude\": 150.100,\n" +
                "    \"longitude\": 150.200,\n" +
                "    \"country_id\": 2,\n" +
                "    \"country_name\": \"rohan\",\n" +
                "    \"population\": 50,\n" +
                "    \"feature_code\": \"idk\"\n" +
                "  }\n" +
                "]";
        RegionDao rDao = mDb.regionDao();
        try {
            JSONArray regionsJSON = new JSONArray(rawDummy);
            for (int i = 0; i < regionsJSON.length(); i++) {
                JSONObject regionJSON = regionsJSON.getJSONObject(i);
                Region region = new Region(regionJSON);
                rDao.insertRegions(region);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add cities to the database by parsing the JSON stored in {@code rawDummy}. In case of any
     * errors, the stack trace is printed to the console. The JSON is split apart into the JSON
     * fragments for each city. Each of those JSONs are then passed to a {@link City}
     * constructor (specifically {@link City#City(JSONObject)}), which extracts the necessary
     * information and initializes itself. Those objects are then added to the database via
     * {@link CityDao}.
     */
    static void addCities() {
        String rawDummy = "[\n" +
                "  {\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"City A\",\n" +
                "    \"latitude\": 1.1,\n" +
                "    \"longitude\": 2.2,\n" +
                "    \"region_id\": 1,\n" +
                "    \"region_name\": \"north\",\n" +
                "    \"country_id\": 1,\n" +
                "    \"country_name\": \"corneria\",\n" +
                "    \"population\": 350000,\n" +
                "    \"feature_code\": \"idk\",\n" +
                "    \"tweet_terms\": \"idk\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 2,\n" +
                "    \"name\": \"City B\",\n" +
                "    \"latitude\": 3.3,\n" +
                "    \"longitude\": 4.4,\n" +
                "    \"region_id\": 1,\n" +
                "    \"region_name\": \"north\",\n" +
                "    \"country_id\": 1,\n" +
                "    \"country_name\": \"corneria\",\n" +
                "    \"population\": 100000,\n" +
                "    \"feature_code\": \"idk\",\n" +
                "    \"tweet_terms\": \"idk\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 3,\n" +
                "    \"name\": \"City C\",\n" +
                "    \"latitude\": 5.5,\n" +
                "    \"longitude\": 6.6,\n" +
                "    \"region_id\": 2,\n" +
                "    \"region_name\": \"south\",\n" +
                "    \"country_id\": 1,\n" +
                "    \"country_name\": \"corneria\",\n" +
                "    \"population\": 20000,\n" +
                "    \"feature_code\": \"idk\",\n" +
                "    \"tweet_terms\": \"idk\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 4,\n" +
                "    \"name\": \"City D\",\n" +
                "    \"latitude\": 7.7,\n" +
                "    \"longitude\": 8.8,\n" +
                "    \"region_id\": 3,\n" +
                "    \"region_name\": \"east\",\n" +
                "    \"country_id\": 2,\n" +
                "    \"country_name\": \"rohan\",\n" +
                "    \"population\": 280,\n" +
                "    \"feature_code\": \"idk\",\n" +
                "    \"tweet_terms\": \"idk\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 5,\n" +
                "    \"name\": \"City E\",\n" +
                "    \"latitude\": 9.9,\n" +
                "    \"longitude\": 10.10,\n" +
                "    \"region_id\": 4,\n" +
                "    \"region_name\": \"west\",\n" +
                "    \"country_id\": 2,\n" +
                "    \"country_name\": \"rohan\",\n" +
                "    \"population\": 20,\n" +
                "    \"feature_code\": \"idk\",\n" +
                "    \"tweet_terms\": \"idk\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 6,\n" +
                "    \"name\": \"City F\",\n" +
                "    \"latitude\": 11.11,\n" +
                "    \"longitude\": 12.12,\n" +
                "    \"region_id\": 4,\n" +
                "    \"region_name\": \"west\",\n" +
                "    \"country_id\": 2,\n" +
                "    \"country_name\": \"rohan\",\n" +
                "    \"population\": 25,\n" +
                "    \"feature_code\": \"idk\",\n" +
                "    \"tweet_terms\": \"idk\"\n" +
                "  }\n" +
                "]";
        CityDao cDao = mDb.cityDao();
        try {
            JSONArray citiesJSON = new JSONArray(rawDummy);
            for (int i = 0; i < citiesJSON.length(); i++) {
                JSONObject cityJSON = citiesJSON.getJSONObject(i);
                City city = new City(cityJSON);
                cDao.insertCities(city);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add countries to the database by parsing the JSON stored in {@code rawDummy}. In case of any
     * errors, the stack trace is printed to the console. The JSON is split apart into the JSON
     * fragments for each country. Each of those JSONs are then passed to a {@link Country}
     * constructor (specifically {@link Country#Country(JSONObject)}), which extracts the necessary
     * information and initializes itself. Those objects are then added to the database via
     * {@link CountryDao}.
     */
    static void addCountries() {
        String rawDummy = "[\n" +
                "  {\n" +
                "    \"id\": 1,\n" +
                "    \"iso_a2\": 0,\n" +
                "    \"name\": \"corneria\",\n" +
                "    \"latitude\": 4321.4321,\n" +
                "    \"longitude\": 1234.1234,\n" +
                "    \"population\": 1000000,\n" +
                "    \"feature_code\": \"idk\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 2,\n" +
                "    \"iso_a2\": 0,\n" +
                "    \"name\": \"rohan\",\n" +
                "    \"latitude\": 100.100,\n" +
                "    \"longitude\": 200.200,\n" +
                "    \"population\": 350,\n" +
                "    \"feature_code\": \"idk\"\n" +
                "  }\n" +
                "]";
        CountryDao cDao = mDb.countryDao();
        try {
            JSONArray countriesJSON = new JSONArray(rawDummy);
            for (int i = 0; i < countriesJSON.length(); i++) {
                JSONObject countryJSON = countriesJSON.getJSONObject(i);
                Country country = new Country(countryJSON);
                cDao.insertCountries(country);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add posts to the database by parsing the JSON stored in {@code rawDummy}. In case of any
     * errors, the stack trace is printed to the console. The JSON is interpreted, and its values
     * are used to instantiate {@link Post} objects. Those objects are then added to the database via
     * {@link PostDao}.
     */
    static void addPosts(){
        String rawDummy = "[\n" +
                "  {\n" +
                "    \"user_id\": 4,\n" +
                "    \"post_text\": \"Ex excepturi quos vero nesciunt autem. Ipsum voluptates quaerat rerum praesentium modi.\\nEos culpa fuga maxime atque exercitationem nemo. Repellendus officiis et. Explicabo eveniet quibusdam magnam minima.\",\n" +
                "    \"network_id\": 1,\n" +
                "    \"img_link\": \"https://www.lorempixel.com/370/965\",\n" +
                "    \"vid_link\": \"https://dummyimage.com/803x720\",\n" +
                "    \"post_date\": \"2017-02-12 08:53:43\",\n" +
                "    \"post_class\": 0,\n" +
                "    \"id\": 1,\n" +
                "    \"post_original\": \"Not sure what this field is\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"user_id\": 3,\n" +
                "    \"post_text\": \"Minus cumque corrupti porro natus tenetur delectus illum. Amet aut molestias eaque autem ea odio.\\nAsperiores sed officia. Similique accusantium facilis sed. Eligendi tempora nisi sint tempora incidunt perferendis.\",\n" +
                "    \"network_id\": 1,\n" +
                "    \"img_link\": \"https://www.lorempixel.com/556/586\",\n" +
                "    \"vid_link\": \"https://dummyimage.com/909x765\",\n" +
                "    \"post_date\": \"2017-02-01 05:49:35\",\n" +
                "    \"post_class\": 0,\n" +
                "    \"id\": 2,\n" +
                "    \"post_original\": \"Not sure what this field is\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"user_id\": 1,\n" +
                "    \"post_text\": \"Veritatis illum occaecati est error magni nesciunt. Voluptate cum odio voluptatum quasi natus. Illo vel tempora pariatur tempore.\",\n" +
                "    \"network_id\": 0,\n" +
                "    \"img_link\": \"https://dummyimage.com/503x995\",\n" +
                "    \"vid_link\": \"https://dummyimage.com/796x497\",\n" +
                "    \"post_date\": \"2017-04-03 18:27:27\",\n" +
                "    \"post_class\": 0,\n" +
                "    \"id\": 3,\n" +
                "    \"post_original\": \"Not sure what this field is\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"user_id\": 3,\n" +
                "    \"post_text\": \"Dolorem ad ducimus laboriosam veritatis id quam rerum. Nostrum voluptatum mollitia modi.\\nVoluptas aut mollitia in perferendis blanditiis eaque eius. Recusandae similique ratione perspiciatis assumenda.\",\n" +
                "    \"network_id\": 1,\n" +
                "    \"img_link\": \"https://placeholdit.imgix.net/~text?txtsize=55&txt=917x558&w=917&h=558\",\n" +
                "    \"vid_link\": \"https://www.lorempixel.com/1016/295\",\n" +
                "    \"post_date\": \"2016-08-29 15:27:28\",\n" +
                "    \"post_class\": 0,\n" +
                "    \"id\": 4,\n" +
                "    \"post_original\": \"Not sure what this field is\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"user_id\": 4,\n" +
                "    \"post_text\": \"Ab voluptates omnis unde voluptas. Molestiae ipsam quis sapiente.\\nProvident illum consectetur deserunt. Nisi vero minus non corrupti impedit.\\nEaque dolor facilis iusto excepturi non. Sunt possimus modi animi.\",\n" +
                "    \"network_id\": 0,\n" +
                "    \"img_link\": \"https://www.lorempixel.com/231/204\",\n" +
                "    \"vid_link\": \"https://dummyimage.com/720x577\",\n" +
                "    \"post_date\": \"2017-07-29 18:52:43\",\n" +
                "    \"post_class\": 0,\n" +
                "    \"id\": 5,\n" +
                "    \"post_original\": \"Not sure what this field is\"\n" +
                "  }\n" +
                "]";
        PostDao pDao = mDb.postDao();
        try {
            JSONArray postsJSON = new JSONArray(rawDummy);
            for (int i = 0; i < postsJSON.length(); i++) {
                JSONObject postJSON = postsJSON.getJSONObject(i);
                Log.i("Network Id\'s", postJSON.getInt("network_id") + "");
                org.codethechange.culturemesh.models.Post post = new org.codethechange.culturemesh.models.Post(postJSON.getInt("id"), postJSON.getInt("user_id"),
                        postJSON.getInt("network_id"),postJSON.getString("post_text"),
                        postJSON.getString("img_link"),postJSON.getString("vid_link"),
                        postJSON.getString("post_date"));
                pDao.insertPosts(post);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add events to the database by parsing the JSON stored in {@code rawDummy}. In case of any
     * errors, the stack trace is printed to the console. The JSON is interpreted, and its values
     * are used to instantiate {@link Event} objects. Those objects are then added to the database via
     * {@link EventDao}.
     */
    static void addEvents() {
        String rawDummy = "[\n" +
                "  {\n" +
                "    \"description\": \"Ad officia impedit necessitatibus. Explicabo consequuntur commodi.\\nId id nostrum doloremque ab minus magnam. Ipsa placeat quasi dolores libero laboriosam.\\nNam tenetur ullam eius officia. Asperiores maiores soluta.\",\n" +
                "    \"title\": \"Centralized motivating encoding\",\n" +
                "    \"network_id\": 0,\n" +
                "    \"date_created\": \"2017-10-13 01:49:21\",\n" +
                "    \"address_1\": \"157 Stacy Drive\\nMercerfort, IA 59281\",\n" +
                "    \"address_2\": \"PSC 0398, Box 9876\\nAPO AE 17620\",\n" +
                "    \"event_date\": \"2017-11-03 17:28:51\",\n" +
                "    \"host_id\": 5,\n" +
                "    \"id\": 1,\n" +
                "    \"location\": [\n" +
                "      \"Uzbekistan\",\n" +
                "      \"Dunnmouth\",\n" +
                "      \"Chavezbury\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"description\": \"Laudantium sequi quisquam necessitatibus fugit eligendi. Rem blanditiis quibusdam molestias. Quis voluptate consequatur magnam nemo est magnam explicabo. A ipsam ipsum esse id quos.\",\n" +
                "    \"title\": \"Reverse-engineered 6th Generation\",\n" +
                "    \"network_id\": 1,\n" +
                "    \"date_created\": \"2017-09-09 17:12:57\",\n" +
                "    \"address_1\": \"1709 Fuller Freeway\\nChungland, PR 67499-3841\",\n" +
                "    \"address_2\": \"916 David Green\\nLake Adamville, MA 66822\",\n" +
                "    \"event_date\": \"2017-11-16 04:38:29\",\n" +
                "    \"host_id\": 2,\n" +
                "    \"id\": 2,\n" +
                "    \"location\": [\n" +
                "      \"Malaysia\",\n" +
                "      \"New John\",\n" +
                "      \"Kyliefort\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"description\": \"Doloremque natus cupiditate ratione sint eveniet. Vitae provident sapiente adipisci.\\nEt inventore quis quos deleniti numquam. Voluptate ipsam totam quas. Ea minima consequuntur consequuntur quaerat facere.\",\n" +
                "    \"title\": \"Adaptive fault-tolerant hardware\",\n" +
                "    \"network_id\": 1,\n" +
                "    \"date_created\": \"2017-10-11 01:13:33\",\n" +
                "    \"address_1\": \"7874 Bowman Port Suite 466\\nPattonhaven, PR 63278-5184\",\n" +
                "    \"address_2\": \"7436 William Village\\nRichardchester, NM 28208\",\n" +
                "    \"event_date\": \"2017-11-10 22:35:03\",\n" +
                "    \"host_id\": 3,\n" +
                "    \"id\": 3,\n" +
                "    \"location\": [\n" +
                "      \"Macao\",\n" +
                "      \"South Jillian\",\n" +
                "      \"New Jenniferfort\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"description\": \"Expedita non nam minima aperiam explicabo. Dicta sunt incidunt optio quae. Quam quibusdam dolorum voluptate corrupti ullam sequi. Amet at repellat iusto fuga voluptates aliquam.\",\n" +
                "    \"title\": \"Quality-focused asynchronous Graphic Interface\",\n" +
                "    \"network_id\": 0,\n" +
                "    \"date_created\": \"2017-11-10 17:48:56\",\n" +
                "    \"address_1\": \"28452 Rivera Pike\\nGambletown, SC 33593-8719\",\n" +
                "    \"address_2\": \"0314 Escobar Burgs\\nLake Bradburgh, CO 06768\",\n" +
                "    \"event_date\": \"2017-11-04 21:46:16\",\n" +
                "    \"host_id\": 5,\n" +
                "    \"id\": 4,\n" +
                "    \"location\": [\n" +
                "      \"Norfolk Island\",\n" +
                "      \"New Tara\",\n" +
                "      \"Johnton\"\n" +
                "    ]\n" +
                "  }\n" +
                "]";
        EventDao cDao = mDb.eventDao();
        try {
            JSONArray eventsJSON = new JSONArray(rawDummy);
            for (int i = 0; i < eventsJSON.length(); i++) {
                JSONObject eventJSON = eventsJSON.getJSONObject(i);
                Event event = new Event(eventJSON.getLong("id"), eventJSON.getLong("network_id"), eventJSON.getString("title"), eventJSON.getString("description"),
                        eventJSON.getString("date_created"), eventJSON.getLong("host_id"),
                        eventJSON.getString("address_1") + eventJSON.getString("address_2"));
                cDao.addEvent(event);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Store user subscriptions to networks in the database by adding {@link NetworkSubscription}
     * objects to the {@link NetworkSubscriptionDao}
     */
    static void subscribeUsers() {
        NetworkSubscription networkSubscription1 = new NetworkSubscription(3,1);
        NetworkSubscription networkSubscription2 = new NetworkSubscription(1,1);
        NetworkSubscription networkSubscription3 = new NetworkSubscription(2,1);
        NetworkSubscription networkSubscription4 = new NetworkSubscription(4,1);
        NetworkSubscription networkSubscription5 = new NetworkSubscription(4,0);
        NetworkSubscriptionDao netSubDao = mDb.networkSubscriptionDao();
        netSubDao.insertSubscriptions(networkSubscription1, networkSubscription2, networkSubscription3,
                networkSubscription4, networkSubscription5);
    }

    /**
     * Add replies to the database by parsing the JSON stored in {@code rawDummy}. In case of any
     * errors, the stack trace is printed to the console. The JSON is interpreted, and its values
     * are used to instantiate {@link PostReply} objects. Those objects are then added to the database via
     * {@link PostReplyDao}.
     */
    static void addReplies() {
        String rawDummy = "[\n" +
                "  {\n" +
                "    \"id\": 1,\n" +
                "    \"parent_id\": 1,\n" +
                "    \"user_id\": 2,\n" +
                "    \"network_id\": 1,\n" +
                "    \"reply_date\": \"2017-03-12 08:53:43\",\n" +
                "    \"reply_text\": \"Test reply 1.\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 2,\n" +
                "    \"parent_id\": 1,\n" +
                "    \"user_id\": 3,\n" +
                "    \"network_id\": 1,\n" +
                "    \"reply_date\": \"2017-03-13 08:53:43\",\n" +
                "    \"reply_text\": \"Test reply 2.\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 3,\n" +
                "    \"parent_id\": 3,\n" +
                "    \"user_id\": 4,\n" +
                "    \"network_id\": 0,\n" +
                "    \"reply_date\": \"2017-04-04 18:27:27\",\n" +
                "    \"reply_text\": \"Test reply 3.\"\n" +
                "  }\n" +
                "]";
        PostReplyDao postReplyDao = mDb.postReplyDao();
        try {
            JSONArray pRJSON = new JSONArray(rawDummy);
            for (int i = 0; i < pRJSON.length(); i++) {
                JSONObject pRObj = pRJSON.getJSONObject(i);
                PostReply pr = new PostReply(pRObj.getLong("id"), pRObj.getLong("parent_id"),
                        pRObj.getLong("user_id"), pRObj.getLong("network_id"),
                        pRObj.getString("reply_date"), pRObj.getString("reply_text"));
                postReplyDao.insertPostReplies(pr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // TODO: These two methods seem really redundant. Why not have instantiatePosts use instantiatePost?
    /**
     * Instantiates the posts in the provided list. Instantiation is done by getting
     * the author of each post as a User object via an API call and then setting the post's author
     * to that object.
     * @param posts Posts to instantiate
     */
    static void instantiatePosts(List<org.codethechange.culturemesh.models.Post> posts) {
        for (org.codethechange.culturemesh.models.Post post : posts) {
            //Get the user
            post.author = API.Get.user(post.userId).getPayload();
        }
    }

    /**
     * Instantiates the provided post by getting the author from the author ID as a {@link User}
     * object and storing that in {@link org.codethechange.culturemesh.models.Post#author}
     * @param post Post to instantiate
     */
    static void instantiatePost(org.codethechange.culturemesh.models.Post post) {
            //Get the user
            post.author = API.Get.user(post.userId).getPayload();
    }

    /**
     * Instantiates the {@link PostReply} objects in the provided list by getting the authors
     * of each comment and storing it in {@link PostReply#author}
     * @param comments List of PostReplies with which we will get comment authors for
     */
    static void instantiatePostReplies(List<PostReply> comments) {
        for (PostReply comment : comments) {
            //Get the user
            comment.author = API.Get.user(comment.userId).getPayload();
        }
    }

    /**
     * The protocol for GET requests is as follows...
     *      1. Check if cache has relevant data. If so, return it.
     *      2. Send network request to update data.
     */
    static class Get {

        /**
         * Get a {@link User} object from it's ID
         * @param id ID of user to find
         * @return If such a user was found, it will be the payload. Otherwise, the request will be
         * marked as failed.
         */
        static NetworkResponse<User> user(long id) {
            UserDao uDao = mDb.userDao();
            User user = uDao.getUser(id);
            //TODO: Send network request.
            return new NetworkResponse<>(user == null, user);
        }

        /**
         * Get the networks a user belongs to by searching all subscriptions in
         * {@link NetworkSubscriptionDao} and then getting {@link Network} objects for each ID found
         * in those subscriptions.
         * @param id ID of {@link User} whose networks are being requested
         * @return List of {@link Network}s the user is in
         */
        static NetworkResponse<ArrayList<Network>> userNetworks(long id) {
            //TODO: Send network request for all subscriptions.
            NetworkSubscriptionDao nSDao  = mDb.networkSubscriptionDao();
            List<Long> netIds = nSDao.getUserNetworks(id);
            Log.i("API.Get.userNetworks(" + id + ")", "Network IDs from Database: " + netIds.toString());
            ArrayList<Network> nets = new ArrayList<>();
            for (Long netId : netIds) {
                NetworkResponse res = network(netId);
                if (!res.fail()) {
                    nets.add((Network) res.getPayload());
                    Log.i("API.Get.userNetworks(" + id + ")", "Received network from " +
                            "database: " + res.getPayload());
                } else {
                    Log.i("API.Get.userNetworks(" + id + ")", "Failure in getting network from " +
                            "database for ID " + netId);
                }
            }
            Log.i("API.Get.userNetworks(" + id + ")", "Networks from Database: " + nets.toString());
            return new NetworkResponse<>(nets);
        }

        /**
         * Get the {@link org.codethechange.culturemesh.models.Post}s a {@link User} has made. This
         * is done by asking {@link PostDao} for all posts with the user's ID, as performed by
         * {@link PostDao#getUserPosts(long)}.
         * @param id ID of the {@link User} whose {@link org.codethechange.culturemesh.models.Post}s
         *           are being requested
         * @return List of the {@link org.codethechange.culturemesh.models.Post}s the user has made
         */
        // TODO: When will we ever use this? Perhaps viewing a user profile?
        static void userPosts(final RequestQueue queue, long id, final Response.Listener<NetworkResponse<ArrayList<org.codethechange.culturemesh.models.Post>>> listener) {
            PostDao pDao = mDb.postDao();
            List<org.codethechange.culturemesh.models.Post> posts = pDao.getUserPosts(id);
            instantiatePosts(posts);
            JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, API_URL_BASE + "user/" +
                    id + "/networks?" + getCredentials(), null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    final ArrayList<org.codethechange.culturemesh.models.Post> posts = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {

                            JSONObject res = (JSONObject) response.get(i);
                            posts.add(new org.codethechange.culturemesh.models.Post(res.getInt("id"), res.getInt("id_user"),
                                    res.getInt("id_network"), res.getString("post_text"),
                                    res.getString("img_link"), res.getString("vid_link"),
                                    res.getString("post_date")));
                            // Here's the tricky part. We need to fetch user information for each post,
                            // but we only want to call the listener once, after all the requests are
                            // finished.
                            // Let's make a counter for the number of requests finished. This will be
                            // a wrapper so that we can pass it by reference.
                            final AtomicInteger numReqFin = new AtomicInteger();
                            numReqFin.set(0);
                            //Next, we will call instantiate postUser, but we will have a special
                            // listener.
                            instantiatePostUser(queue, posts.get(i), new Response.Listener<NetworkResponse<org.codethechange.culturemesh.models.Post>>() {
                                @Override
                                public void onResponse(NetworkResponse<org.codethechange.culturemesh.models.Post> response) {
                                    // Update the numReqFin counter that we have another finished post
                                    // object.
                                    if (numReqFin.addAndGet(1) == posts.size()) {
                                        // We finished!! Call the listener at last.
                                        listener.onResponse(new NetworkResponse<ArrayList<org.codethechange.culturemesh.models.Post>>(false, posts));
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(req);
        }

        /**
         * Get the {@link Event}s a {@link User} is subscribed to. This is done by searching for
         * {@link EventSubscription}s with the user's ID (via
         * {@link EventSubscriptionDao#getUserEventSubscriptions(long)}) and then inflating each
         * event from it's ID into a full {@link Event} object using {@link API.Get#event(long)}.
         * @param id ID of the {@link User} whose events are being searched for
         * @return List of {@link Event}s to which the user is subscribed
         */
        static NetworkResponse<ArrayList<Event>> userEvents(long id) {
            //TODO: Check for event subscriptions with network request.
            EventSubscriptionDao eSDao = mDb.eventSubscriptionDao();
            List<Long> eventIds = eSDao.getUserEventSubscriptions(id);
            ArrayList<Event> events = new ArrayList<>();
            for (Long eId : eventIds) {
                NetworkResponse res = event(eId);
                if (!res.fail()) {
                    events.add((Event) res.getPayload());
                }
            }
            return new NetworkResponse<>(events);
        }

        static NetworkResponse<Network> network(long id) {
            //TODO: Send network request if not found.
            NetworkDao netDao = mDb.networkDao();
            List<DatabaseNetwork> nets = netDao.getNetwork(id);

            if (nets == null || nets.size() == 0 || nets.get(0) == null) {
                if (nets == null) {
                    Log.i("API.Get.network(" + id + ")", "Failure getting network IDs from " +
                            "database because of null response.");
                } else if (nets.size() == 0) {
                    Log.i("API.Get.network(" + id + ")", "Failure getting network IDs from " +
                            "database because an empty list was received: " + nets.toString());
                } else {
                    Log.i("API.Get.network(" + id + ")", "Failure getting network IDs from " +
                            "database because of null first item in list: " + nets.toString());
                }
                return new NetworkResponse<>(true);
            } else {
                Log.i("API.Get.network(" + id + ")", "Networks from database: " + nets.toString());
                DatabaseNetwork dn = nets.get(0);
                Log.i("API.Get.network(" + id + ")", "Inflating network: " + dn);
                Network net = expandDatabaseNetwork(dn);
                Log.i("API.Get.network(" + id + ")", "Inflated network with ID " + dn.id);
                return new NetworkResponse<>(net);
            }
        }

        static NetworkResponse<List<org.codethechange.culturemesh.models.Post>> networkPosts(long id) {
            //TODO: Send network request.
            PostDao pDao = mDb.postDao();
            List<org.codethechange.culturemesh.models.Post> posts = pDao.getNetworkPosts((int) id);
            instantiatePosts(posts);
            return new NetworkResponse<>(posts);
        }

        static NetworkResponse<List<Event>> networkEvents(long id) {
            //TODO:Send network request.... Applies to subsequent methods too.
            EventDao eDao = mDb.eventDao();
            List<Event> events = eDao.getNetworkEvents(id);
            Log.i("Getting events" , events.size() + "");
            return new NetworkResponse<>(events == null, events);
        }

        static NetworkResponse<ArrayList<User>> networkUsers(long id) {
            NetworkSubscriptionDao nSDao = mDb.networkSubscriptionDao();
            List<Long> userIds = nSDao.getNetworkUsers(id);
            ArrayList<User> users = new ArrayList<>();
            for (long uId: userIds) {
                NetworkResponse<User> res = user(uId);
                if (!res.fail()) {
                    users.add(res.getPayload());
                }
            }
            return new NetworkResponse<>(users);
        }

        /**
         * IMPORTANT: GUIDE TO NETWORK REQUESTS
         * EXAMPLE NETWORK REQUEST CALL -- IMPORTANT!!
         * The format for API method calls will mimic more of a callback. We are basically
         * abstracting out doInBackground in the API methods now. View the Response.Listener<> as
         * the new onPostExecute() for ASync Tasks.
         * Notice that we now pass the Activity's RequestQueue for EVERY method call as the first
         * parameter. I made the id # 100 only so you can see a valid post id (1 is null). It should be postID.
         * Also notice that we are not handling caching or working with the database AT ALL.
         * We'll try to tackle that later.
         *
         * Link: https://developer.android.com/training/volley/simple
         *
         * Migration Workflow:
         * - Figure out how to do network request independent of Android client. First, look at the
         * swagger documentation by going to https://editor.swagger.io/ and copying and pasting
         * the code from https://github.com/alanefl/culturemesh-api/blob/master/spec_swagger.yaml.
         * Notice that you will have to prefix each of your endpoints with "https://www.culturemesh.com/api-dev/v1"
         * Also notice that you will have to suffix each of your endpoints with a key parameter:
         * "key=" + Credentials.APIKey (off of source control, check Slack channel for file to
         * manually import into your project)
         * - Test that you can do the request properly on your own. For most GET requests, you can
         * test within your own browser, or you can Postman [https://www.getpostman.com/]
         * (which I personally recommend, esp. if you need a JSON request body i.e. POST requests)
         * - Write the new API method with this signature:
         * API.[GET/POST/PUT].[method_name] ([RequestQueue], [original params], [Response.Listener<NetworkResponse<[Object_You_Want_To_Return]>>])
         * - The general format will be making a request. They will either be a JsonObjectRequest
         * (if you get an object returned from API) or JsonArrayRequest (if you get array of
         * json objects returned from API). Follow this example for the parameters. The meat of the
         * task will be in the Response.Listener<> parameter for the constructor.
         * - In this listener, you will have to convert the JSON object into our Java objects. Make
         * sure you handle errors with JSON formats. If you get stuck on this part, make sure your keys
         * conform to the actual keys returned on your manual requests tests with Postman.
         * - If the API returns an ERROR status code (somewhere in the 400's), the Response.ErrorListener()
         * will be called. I still call the passed callback function, but set NetworkResponse's 'fail'
         * param to true.
         * - Sometimes you will need to have multiple requests. For example, we need to get user data
         * for each post, but we only get user id's from the first post request. Thus, just nest
         * another request inside the listener of the first one if you need data from the first to pass
         * into the second (i.e. id_user from post to get user)
         */
        static void post(final RequestQueue queue, long id, final Response.Listener<NetworkResponse<org.codethechange.culturemesh.models.Post>> callback) {
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                    API_URL_BASE + "post/" + id + "?" + getCredentials(),
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject res) {
                    org.codethechange.culturemesh.models.Post post = null;
                    try {
                        //Make post object out of JSON object.
                        post = new org.codethechange.culturemesh.models.Post(res.getInt("id"), res.getInt("id_user"),
                                res.getInt("id_network"), res.getString("post_text"),
                                res.getString("img_link"), res.getString("vid_link"),
                                res.getString("post_date"));
                        // Now, get author.
                        // For generalizing the logic when getting multiple posts, we will have to
                        // add the post to an ArrayList to pass it onto instantiate postUsers
                        instantiatePostUser(queue, post, callback);

                                            } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onResponse(new NetworkResponse<org.codethechange.culturemesh.models.Post>(true, null));
                    }
            });
            queue.add(req);
        }

        static NetworkResponse<Event> event(long id) {
            EventDao eDao = mDb.eventDao();
            Event event = eDao.getEvent(id);
            return new NetworkResponse<>(event == null, event);
        }

        static NetworkResponse<ArrayList<User>> eventAttendance(long id) {
            EventSubscriptionDao eSDao = mDb.eventSubscriptionDao();
            List<Long> uIds = eSDao.getEventUsers(id);
            ArrayList<User> users = new ArrayList<>();
            for (long uid : uIds) {
                NetworkResponse res = user(uid);
                if (!res.fail()) {
                    users.add((User) res.getPayload());
                }
            }
            return new NetworkResponse<>(users);
        }

        static NetworkResponse<List<PostReply>> postReplies(long id){
            PostReplyDao dao = mDb.postReplyDao();
            List<PostReply> replies = dao.getPostReplies(id);
            instantiatePostReplies(replies);
            return new NetworkResponse<>(replies == null, replies);
        }

        static NetworkResponse<List<Place>> autocompletePlace(String text) {
            // TODO: Take argument for maximum number of locations to return?
            List<Place> locations = new ArrayList<>();
            //Get any related cities, countries, or regions.
            CityDao cityDao = mDb.cityDao();
            locations.addAll(cityDao.autoCompleteCities(text));
            RegionDao regionDao = mDb.regionDao();
            locations.addAll(regionDao.autoCompleteRegions(text));
            CountryDao countryDao = mDb.countryDao();
            locations.addAll(countryDao.autoCompleteCountries(text));
            return new NetworkResponse<>(locations == null, locations);
        }

        static NetworkResponse<List<Language>> autocompleteLanguage(String text) {
            // TODO: Take argument for maximum number of languages to return?
            // TODO: Use Database for autocompleteLanguage and use instead of dummy data
            List<Language> matches = new ArrayList<>();
            matches.add(new Language(0, "Sample Language 0", 10));
            return new NetworkResponse(matches);
        }

        static NetworkResponse<Network> netFromLangAndNear(Language lang, NearLocation near) {
            NetworkDao netDao = mDb.networkDao();
            DatabaseNetwork dn = netDao.netFromLangAndHome(lang.language_id, near.near_city_id, near.near_region_id,
                    near.near_country_id);
            if (dn == null) {
                // TODO: Distinguish between the network not existing and the lookup failing
                return new NetworkResponse<>(true, R.string.noNetworkExist);
            } else {
                return network(dn.id);
            }
        }

        static NetworkResponse<Network> netFromFromAndNear(FromLocation from, NearLocation near) {
            NetworkDao netDao = mDb.networkDao();
            DatabaseNetwork dn = netDao.netFromLocAndHome(from.from_city_id, from.from_region_id,
                    from.from_country_id, near.near_city_id, near.near_region_id, near.near_country_id);
            if (dn == null) {
                // TODO: Distinguish between the network not existing and the lookup failing
                return new NetworkResponse<>(true, R.string.noNetworkExist);
            } else {
                return network(dn.id);
            }
        }

        /**
         * The API will return Post JSON Objects with id's for the user. Often, we will want to get
         * the user information associated with a post, such as the name and profile picture. This
         * method allows us to instantiate this user information for each post.
         * @param queue The Volley RequestQueue object that handles all the request queueing.
         * @param post An already instantiated Post object that has a null author field but a defined
         *             userId field.
         * @param listener the UI listener that will be called when we complete the task at hand.
         */
        static void instantiatePostUser(RequestQueue queue, final org.codethechange.culturemesh.models.Post post,
                                         final Response.Listener<NetworkResponse<org.codethechange.culturemesh.models.Post>> listener) {
            JsonObjectRequest authReq = new JsonObjectRequest(Request.Method.GET,
                    "https://www.culturemesh.com/api-dev/v1/user/" + post.userId + getCredentials(),
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject res) {
                    try {
                        //make User object out of user JSON.
                        post.author = new User(res.getInt("id"),
                                res.getString("first_name"),
                                res.getString("last_name"),
                                res.getString("email"), res.getString("username"),
                                "https://www.culturemesh.com/user_images/" + res.getString("img_link"),
                                res.getString("about_me"));
                        listener.onResponse(new NetworkResponse<org.codethechange.culturemesh.models.Post>(false, post));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.onResponse(new NetworkResponse<org.codethechange.culturemesh.models.Post>(true, null));
                }
            });
            queue.add(authReq);

        }
    }

    static class Post {
        /*
            TODO: During production time, we will send it off to server (as well as update locally?).
         */
        static NetworkResponse<EventSubscription> addUserToEvent(long userId, long eventId) {
            EventSubscriptionDao eSDao = mDb.eventSubscriptionDao();
            EventSubscription es = new EventSubscription(userId, eventId);
            eSDao.insertSubscriptions(es);
            return new NetworkResponse<>(es);
        }

        static NetworkResponse addUserToNetwork(long userId, long networkId) {
            NetworkSubscriptionDao nSDao = mDb.networkSubscriptionDao();
            NetworkSubscription ns = new NetworkSubscription(userId, networkId);
            nSDao.insertSubscriptions(ns);
            return new NetworkResponse<>(ns);
        }

        static NetworkResponse removeUserFromNetwork(long userId, long networkId) {
            NetworkSubscriptionDao nSDao = mDb.networkSubscriptionDao();
            NetworkSubscription ns = new NetworkSubscription(userId, networkId);
            nSDao.deleteNetworkSubscriptions(ns);
            return new NetworkResponse<>(ns);
        }

        static NetworkResponse user(User user) {
            UserDao uDao = mDb.userDao();
            uDao.addUser(user);
            return new NetworkResponse<>(user);
        }

        static NetworkResponse network(Network network) {
            NetworkDao nDao = mDb.networkDao();
            nDao.insertNetworks(network.getDatabaseNetwork());
            return new NetworkResponse<>(network);
        }

        static NetworkResponse post(org.codethechange.culturemesh.models.Post post) {
            PostDao pDao = mDb.postDao();
            pDao.insertPosts(post);
            return new NetworkResponse<>(false, post);
        }

        static NetworkResponse reply(PostReply comment) {
            PostReplyDao prDao = mDb.postReplyDao();
            prDao.insertPostReplies(comment);
            return new NetworkResponse(false, comment);
        }

        static NetworkResponse event(Event event) {
            EventDao eDao = mDb.eventDao();
            eDao.addEvent(event);
            return new NetworkResponse<>(false, event);
        }
    }

    static class Put {
        static NetworkResponse<User> user(User user) {
            UserDao uDao = mDb.userDao();
            uDao.addUser(user);
            return new NetworkResponse<User>(user == null, user);
        }

        static NetworkResponse event(Event event) {
            return new NetworkResponse();
        }
    }

    private static Network expandDatabaseNetwork(DatabaseNetwork dn) {
        Place near = locationToPlace(dn.nearLocation);
        Log.i("API.expandDBNetwork", "Converted the DatabaseNetwork " + dn + " to" +
                " the Place " + near + " for the near location");

        if (dn.isLanguageBased()) {
            Log.i("API.expandDBNetwork", "The DatabaseNetwork " + dn + " is language based");
            LanguageDao langDao = mDb.languageDao();
            Language lang =langDao.getLanguage(dn.languageId);
            Log.i("API.expandDBNetwork", "Converted the ID " + dn.languageId + " to" +
                    " the Langauge " + lang + " for the language spoken");
            Network net = new Network(near, lang, dn.id);
            Log.i("API.expandDBNetwork", "Expanded the DatabaseNetwork " + dn + " to " +
                    "the Network " + net);
            return net;
        } else {
            Log.i("API.expandDBNetwork", "The DatabaseNetwork " + dn + " is location based");
            Place from = locationToPlace(dn.fromLocation);
            Log.i("API.expandDBNetwork", "Converted the FromLocation " + dn.fromLocation + " to" +
                    " the Place " + from + " for the from location.");
            Network net = new Network(near, from, dn.id);
            Log.i("API.expandDBNetwork", "Expanded the DatabaseNetwork " + dn + " to " +
                    "the Network " + net);
            return net;
        }
    }

    private static Place locationToPlace(Location loc) {
        CityDao cityDao = mDb.cityDao();
        RegionDao regionDao = mDb.regionDao();
        CountryDao countryDao = mDb.countryDao();

        Place place;
        // TODO: Is this right? If so, DatabaseLocation only really needs to store type and ID
        if (loc.getType() == Location.CITY) {
            Log.i("API.locationToPlace", "Location " + loc + " is a city");
            place = cityDao.getCity(loc.getCityId());
        } else if (loc.getType() == Location.REGION) {
            Log.i("API.locationToPlace", "Location " + loc + " is a region");
            place = regionDao.getRegion(loc.getRegionId());
        } else {
            Log.i("API.locationToPlace", "Location " + loc + " is a country");
            place = countryDao.getCountry(loc.getCountryId());
        }

        Log.i("API.locationToPlace", "Converted the location " + loc + " into the place " +
                place);

        return place;
    }

    public static void loadAppDatabase(Context context) {
        reqCounter++;
        if (mDb == null) {
            mDb = Room.databaseBuilder(context.getApplicationContext(), CMDatabase.class, "cmdatabase").
                    fallbackToDestructiveMigration().build();
        }
    }

    static void closeDatabase() {
        if (--reqCounter <= 0) {
            mDb.close();
            mDb = null;
        }
    }

    /**
     * Use this method to append our credentials to our server requests. For now, we are using a
     * static API key. In the future, we are going to want to pass session tokens.
     * @return credentials string to be appended to request url as a param.
     */
    static String getCredentials(){
        return "&key=" + Credentials.APIKey;
    }

}
