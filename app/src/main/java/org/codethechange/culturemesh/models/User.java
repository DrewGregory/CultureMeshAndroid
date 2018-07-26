package org.codethechange.culturemesh.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created by nathaniel on 11/10/17.
 */
// TODO: Document User
@Entity
public class User implements Serializable, Putable, Postable {
    @PrimaryKey
    public long id;
    public int role;
    public String email;
    public String username;

    public String firstName;
    public String lastName;
    public String gender;

    public String aboutMe;
    public String imgURL;

    @Ignore
    private String password;

    public User(long id, String firstName, String lastName, String email, String username,
                String imgURL, String aboutMe, String gender) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.imgURL = imgURL;
        this.aboutMe = aboutMe;
        this.gender = gender;
    }

    public User(JSONObject res) throws JSONException{
        this(res.getInt("id"),
                res.getString("first_name"),
                res.getString("last_name"),
                res.getString("email"), res.getString("username"),
                "https://www.culturemesh.com/user_images/" + res.getString("img_link"),
                res.getString("about_me"), res.getString("gender"));
    }
    public User() {

    }

    public String getImgURL() {
        return imgURL;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getBio() {
        return aboutMe;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBio(String bio) {
        this.aboutMe = bio;
    }

    /**
     * Create a JSON representation of the object that conforms to the following format:
     * <pre>
     *     {@code
     *       {
               "id": 0,
               "username": "string",
               "first_name": "string",
               "last_name": "string",
               "email": "string",
               "role": 0,
               "gender": "string",
               "about_me": "string",
               "img_link": "string"
             }
     *     }
     * </pre>
     * This is intended to be the format used by the {@code /user/users} PUT endpoint
     * @return JSON representation of the object
     * @throws JSONException Unclear when this would be thrown
     */
    public JSONObject getPutJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("username", username);
        json.put("first_name", firstName);
        json.put("last_name", lastName);
        json.put("email", email);
        json.put("role", role);
        json.put("gender", gender);
        json.put("about_me", aboutMe);
        json.put("img_link", imgURL);
        return json;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Create a JSON representation of the object that conforms to the following format:
     * <pre>
     *     {@code
     *       {
                "username": "string",
                "password": "string",
                "first_name": "string",
                "last_name": "string",
                "email": "string",
                "role": 0
             }
     *     }
     * </pre>
     * This is intended to be the format used by the {@code /user/users} POST endpoint.
     * <string>NOTE: The user's password must be set using {@link User#setPassword(String)}</string>
     * @return JSON representation of the object
     * @throws JSONException Unclear when this would be thrown
     */
    public JSONObject getPostJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", password);
        json.put("first_name", firstName);
        json.put("last_name", lastName);
        json.put("email", email);
        json.put("role", role);
        return json;
    }

}
