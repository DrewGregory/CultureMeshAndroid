package org.codethechange.culturemesh;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.codethechange.culturemesh.models.Network;
import org.codethechange.culturemesh.models.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SettingsActivity extends DrawerActivity implements NetworkSummaryAdapter.OnNetworkTapListener {

    RecyclerView rv;
    TextView emptyText;
    EditText bio, firstName, lastName, userName, email;
    ImageView profilePicture;
    Button updateProfile;
    User user;
    RequestQueue queue;
    private static final String TAG = SettingsActivity.class.getName();
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            Uri imageURI = data.getData();
            //Check to see if image is too big.
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                System.out.println(imageURI);
                // Encode image as a string
                String image = Base64.encodeToString(bytes, Base64.DEFAULT);
                API.Post.uploadImage(queue, image, getSharedPreferences(API.SETTINGS_IDENTIFIER, MODE_PRIVATE),
                        new Response.Listener<NetworkResponse<String>>() {
                            @Override
                            public void onResponse(NetworkResponse<String> response) {
                                if (response.fail()) {
                                    response.showErrorDialog(SettingsActivity.this);
                                } else {
                                    Log.i("FINISHED UPLOADING", response.getPayload());
                                }
                            }
                });
                bitmap.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final SharedPreferences settings = getSharedPreferences(API.SETTINGS_IDENTIFIER, MODE_PRIVATE);
        rv = findViewById(R.id.rv);
        bio = findViewById(R.id.bio);
        firstName = findViewById(R.id.first_name_field);
        lastName = findViewById(R.id.last_name_field);
        emptyText = findViewById(R.id.empty_text);
        email = findViewById(R.id.email);
        userName = findViewById(R.id.user_name);
        profilePicture = findViewById(R.id.user_profile);
        updateProfile = findViewById(R.id.update_profile_button);
        queue = Volley.newRequestQueue(getApplicationContext());
        profilePicture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.selectPicture)), PICK_IMAGE);
            }
        });
        queue = Volley.newRequestQueue(getApplicationContext());
        //TODO: Add ability to change profile picture.
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    user.setFirstName(firstName.getText().toString());
                    user.setLastName(lastName.getText().toString());
                    user.setBio(bio.getText().toString());
                    user.setUsername(userName.getText().toString());
                    SharedPreferences.Editor editor = settings.edit();
                    String emailText = email.getText().toString();
                    editor.putString(API.USER_EMAIL, emailText);
                    editor.apply();
                    API.Put.user(queue, user, emailText, getSharedPreferences(API.SETTINGS_IDENTIFIER, MODE_PRIVATE),
                            new Response.Listener<NetworkResponse<String>>() {
                        @Override
                        public void onResponse(NetworkResponse<String> response) {
                            if (response.fail()) {
                                response.showErrorDialog(SettingsActivity.this);
                            } else {
                                NetworkResponse.genSuccessDialog(SettingsActivity.this,
                                        R.string.updated_profile).show();
                            }
                        }
                    });
                } catch(NullPointerException e) {
                    //TODO: User is null. We should handle that.
                    e.printStackTrace();
                }

            }
        });
        //Load User Info.
        API.Get.user(queue, currentUser, new Response.Listener<NetworkResponse<User>>() {
            @Override
            public void onResponse(NetworkResponse<User> response) {
                if (!response.fail()) {
                    user = response.getPayload();
                    bio.setText(user.aboutMe);
                    firstName.setText(user.firstName);
                    lastName.setText(user.lastName);
                    userName.setText(user.username);
                    email.setText(settings.getString(API.USER_EMAIL, getString(R.string.missingEmail)));
                    Picasso.with(getApplicationContext()).load(user.getImgURL()).into(profilePicture);
                    Log.i(TAG, "User info loaded");
                    rv.getAdapter().notifyDataSetChanged();
                    Log.i(TAG, "Adapter notified of new user info");

                } else {
                    response.showErrorDialog(getApplicationContext());
                }
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        resetAdapter();
        ItemTouchHelper.SimpleCallback listener = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                AlertDialog success = new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(R.string.leave_network_question)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Wow! We are removing this network! Sad..
                                long networkID = ((NetworkSummaryAdapter) rv.getAdapter()).getNetworks()
                                        .get(viewHolder.getAdapterPosition()).id;
                                API.Post.leaveNetwork(queue, networkID,
                                        getSharedPreferences(API.SETTINGS_IDENTIFIER, MODE_PRIVATE),
                                        new Response.Listener<NetworkResponse<String>>() {
                                    @Override
                                    public void onResponse(NetworkResponse<String> response) {
                                        rv.getAdapter().notifyDataSetChanged();
                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Nothing here.
                            }
                        })
                        .create();
                success.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //Even if we aren't changing anything, the swipe motion removes
                        //the item from the recycler. We need to include it again.
                        rv.getAdapter().notifyDataSetChanged();
                    }
                });
                success.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(listener);
        itemTouchHelper.attachToRecyclerView(rv);


    }

    @Override
    public void onItemClick(View v, Network network) {
        //TODO: Figure out what you want here. Perhaps view network?
    }

    void resetAdapter(){
        ArrayList<Network> networks = new ArrayList<>();
        HashMap<String, Integer> counts = new HashMap<>();
        HashMap<String, Integer> users = new HashMap<>();
        final NetworkSummaryAdapter adapter = new NetworkSummaryAdapter(networks, counts, users, SettingsActivity.this);
        rv.setAdapter(adapter);
        //Fetch Data off UI thread.
        API.Get.userNetworks(queue, currentUser, new Response.Listener<NetworkResponse<ArrayList<Network>>>() {
            @Override
            public void onResponse(NetworkResponse<ArrayList<Network>> response) {
                // Cool! Now, for each network, we need to find the number of posts and the
                // number of users.
                if (!response.fail()) {
                    ArrayList<Network> nets = response.getPayload();
                    if (rv.getAdapter().getItemCount() > 0) {
                        //Hide empty text.
                        emptyText.setVisibility(View.GONE);
                    } else {
                        emptyText.setText(getResources().getString(R.string.no_networks));
                    }
                    for (final Network net : nets) {
                        Log.d(TAG, "Loaded network: " + net);
                        API.Get.networkUserCount(queue, net.id, new Response.Listener<NetworkResponse<Long>>() {
                            @Override
                            public void onResponse(NetworkResponse<Long> response) {
                                if (!response.fail()) {
                                    /* getUserCounts() returns HashMap<network_id, user_count> */
                                    // This prevents possibility that the user counts are added in
                                    // wrong order.
                                    adapter.getUserCounts().put(net.id + "", response.getPayload().intValue());
                                    if (adapter.getUserCounts().containsKey(net.id +"") &&
                                            adapter.getPostCounts().containsKey(net.id + "")) {
                                        //The network is ready to be added.
                                        adapter.getNetworks().add(net);
                                        adapter.notifyDataSetChanged();
                                    }
                                } else {
                                    response.showErrorDialog(SettingsActivity.this);
                                    adapter.getUserCounts().put(net.id + "", 0);
                                }
                            }
                        });
                        API.Get.networkPostCount(queue, net.id, new Response.Listener<NetworkResponse<Long>>() {
                            @Override
                            public void onResponse(NetworkResponse<Long> response) {
                                if (!response.fail()) {
                                    adapter.getPostCounts().put(net.id + "", response.getPayload().intValue());
                                    if (adapter.getUserCounts().containsKey(net.id +"") &&
                                            adapter.getPostCounts().containsKey(net.id + "")) {
                                        //The network is ready to be added.
                                        adapter.getNetworks().add(net);
                                        adapter.notifyDataSetChanged();
                                    }
                                } else {
                                    response.showErrorDialog(SettingsActivity.this);
                                    adapter.getPostCounts().put(net.id + "", 0);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    response.showErrorDialog(SettingsActivity.this);
                }
            }
        });
    }

    /**
     * This ensures that we are canceling all network requests if the user is leaving this activity.
     * We use a RequestFilter that accepts all requests (meaning it cancels all requests)
     */
    @Override
    public void onStop() {
        super.onStop();
        if (queue != null)
            queue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
    }
}
