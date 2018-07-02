package org.codethechange.culturemesh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.codethechange.culturemesh.models.Event;
import org.codethechange.culturemesh.models.FeedItem;
import org.codethechange.culturemesh.models.Post;
import org.codethechange.culturemesh.models.PostReply;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

//TODO: If no posts, show text view saying add a post!
/**
 * Created by Dylan Grosz (dgrosz@stanford.edu) on 11/10/17.
 */
public class PostsFrag extends Fragment {

    private RecyclerView mRecyclerView;
    private RVAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    long selectedNetwork;
    SharedPreferences settings;
    RequestQueue queue;
    //To figure out params that would be passed in

    public PostsFrag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        settings = getActivity().getSharedPreferences(API.SETTINGS_IDENTIFIER, MODE_PRIVATE);
        queue = Volley.newRequestQueue(getContext());
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);

        mRecyclerView = rootView.findViewById(R.id.postsRV);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //Get network id
        selectedNetwork = settings.getLong(API.SELECTED_NETWORK, 1);
        SharedPreferences settings = getActivity().getSharedPreferences(API.SETTINGS_IDENTIFIER,
                MODE_PRIVATE);
        //We generalize posts/events to be feed items for polymorphism.
        //TODO: Consider error checking for when getPayload is null.
        final ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();
        mAdapter = new RVAdapter(feedItems, new RVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FeedItem item) {
                Intent intent = new Intent(getActivity(), SpecificPostActivity.class);
                long id;
                try {
                    id = ((Post) item).id;
                    intent.putExtra("postID", id);
                    intent.putExtra("networkID", selectedNetwork);
                    getActivity().startActivity(intent);
                } catch(ClassCastException e) {
                    //I don't think we have commenting support for events??
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity(), "Cannot open post", Toast.LENGTH_LONG).show();
                }
            }
        }, getActivity().getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);
        if (settings.getBoolean(TimelineActivity.FILTER_CHOICE_EVENTS, true)) {
            //If events aren't filtered out, add them to arraylist.
            API.Get.networkEvents(queue, selectedNetwork, new Response.Listener<NetworkResponse<List<Event>>>() {
                @Override
                public void onResponse(NetworkResponse<List<Event>> response) {
                    if (!response.fail())
                        feedItems.addAll(response.getPayload());
                }
            });
        }
        if (settings.getBoolean(TimelineActivity.FILTER_CHOICE_NATIVE, true)) {
            //If posts aren't filtered out, add them to arraylist.
            //We also need to get the post replies.
            Log.i("Try this!", "About to run getnetposts");
            API.Get.networkPosts(queue, selectedNetwork, new Response.Listener<NetworkResponse<List<Post>>>() {
                @Override
                public void onResponse(NetworkResponse<List<Post>> response) {
                    if (response.fail()) {
                        response.showErrorDialog(getContext());
                    } else {
                        // We need to sort these posts by date. Oh wait, they're already sorted!
                        ArrayList<Post> posts = (ArrayList<Post>) response.getPayload();
                        Log.i("Size", posts.size() + "");
                        for (final Post post : posts) {
                            Log.i("Caught posts","in response listen");
                            feedItems.add(post);
                            //Get comments
                            API.Get.postReplies(queue, post.id, new Response.Listener<NetworkResponse<ArrayList<PostReply>>>() {
                                @Override
                                public void onResponse(NetworkResponse<ArrayList<PostReply>> response) {
                                    if (!response.fail()) {
                                        Log.i("Adding comments", "Hello");
                                        post.comments = response.getPayload();
                                        Log.i("Comments", "Adding " + post.comments.size() + "comments to post " + post.id);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                        mAdapter.notifyDataSetChanged();
                    }

                }
            });

        }
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
