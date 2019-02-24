package org.codethechange.culturemesh;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.codethechange.culturemesh.models.Event;
import org.codethechange.culturemesh.models.FeedItem;
import org.codethechange.culturemesh.models.Post;
import org.codethechange.culturemesh.models.PostReply;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

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
    String maxEventId = API.NO_MAX_PAGINATION;
    //The post with the lowest id that we have fetched. We use this for paginating future posts.
    String maxPostId = API.NO_MAX_PAGINATION;

    /**
     * The textview that is shown if no feed items have been created for this network.
     */
    TextView noPosts;


    /**
     * {@inheritDoc}
     * Also initialize {@link PostsFrag#settings} and {@link PostsFrag#queue}
     * @param savedInstanceState {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        settings = getActivity().getSharedPreferences(API.SETTINGS_IDENTIFIER, MODE_PRIVATE);
        queue = Volley.newRequestQueue(getContext());
        super.onCreate(savedInstanceState);
    }

    /**
     * Create user interface and handle clicks on posts by launching {@link SpecificPostActivity},
     * which displays more detailed information.
     * @param inflater Inflates {@link R.layout#fragment_posts} into a full user interface that is
     *                 a child of {@code container}
     * @param container Parent of created user interface
     * @param savedInstanceState Not used
     * @return Inflated user interface
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);
        mRecyclerView = rootView.findViewById(R.id.postsRV);
        noPosts = rootView.findViewById(R.id.no_feed_items);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //Get network id
        selectedNetwork = settings.getLong(API.SELECTED_NETWORK, 1);
        //We generalize posts/events to be feed items for polymorphism.
        final ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();
        mAdapter = new RVAdapter(feedItems, new RVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FeedItem item) {
                Intent intent = new Intent(getActivity(), SpecificPostActivity.class);
                if (item instanceof Post) {
                    try {
                        long id = ((Post) item).id;
                        intent.putExtra("postID", id);
                        intent.putExtra("networkID", selectedNetwork);
                        getActivity().startActivity(intent);
                    } catch (NullPointerException e) {
                        NetworkResponse.genErrorDialog(getActivity(), R.string.error_opening_post);
                    }
                } else if (item instanceof Event) {
                    // If they aren't currently attending the event, we'll ask them if they want to attend.
                    // If are currently attending, we'll ask if they want to leave.
                    final Event event = (Event) item;
                    boolean attending = mAdapter.getUserAttendingEvents().contains(event.id);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    final Resources resources = getResources();
                    String titleMessage, message,
                            posButtonMessage = resources.getString(R.string.ok),
                            negButtonMessage = resources.getString(R.string.cancel);
                    DialogInterface.OnClickListener posButtonListener,
                            negButtonListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            };
                    if (attending) {
                        titleMessage = resources.getString(R.string.leave_event);
                        message = resources.getString(R.string.would_you_like_to_leave_this_event);
                        posButtonListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                API.Post.leaveEvent(queue, event.id, settings, new Response.Listener<NetworkResponse<String>>() {
                                    @Override
                                    public void onResponse(NetworkResponse<String> response) {
                                        if (response.fail()) {
                                            response.showErrorDialog(getActivity());
                                        } else {
                                            NetworkResponse.genSuccessDialog(getActivity(),
                                                    R.string.left_event).show();
                                        }

                                    }
                                });
                            }
                        };
                    } else {
                        titleMessage = resources.getString(R.string.join_event);
                        message = resources.getString(R.string.would_you_like_to_join_this_event);
                        posButtonListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                API.Post.joinEvent(queue, event.id, settings, new Response.Listener<NetworkResponse<String>>() {
                                    @Override
                                    public void onResponse(NetworkResponse<String> response) {
                                        if (response.fail()) {
                                            response.showErrorDialog(getActivity());
                                        } else {
                                            NetworkResponse.genSuccessDialog(getActivity(),
                                                    R.string.joined_event).show();
                                        }
                                    }
                                });
                            }
                        };
                    }
                    builder.setTitle(titleMessage)
                    .setMessage(message)
                    .setPositiveButton(posButtonMessage, posButtonListener)
                    .setNegativeButton(negButtonMessage, negButtonListener)
                    .create().show();
                }
            }
        }, getActivity().getApplicationContext());
        // We will want to know which of these events the user is attending, which affects how
        // the view is displayed.
        API.Get.userEventsForNetwork(queue,
                getActivity().getSharedPreferences(API.SETTINGS_IDENTIFIER, MODE_PRIVATE),
                selectedNetwork, new Response.Listener<NetworkResponse<ArrayList<Event>>>() {
                    @Override
                    public void onResponse(NetworkResponse<ArrayList<Event>> response) {
                        if (response.fail()){
                            response.showErrorDialog(getActivity());
                        } else {
                            for (Event event: response.getPayload()){
                                mAdapter.getUserAttendingEvents().add(event.id);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }
        });
        mRecyclerView.setAdapter(mAdapter);
        fetchNewPage(new Response.Listener<Void>() {
            @Override
            public void onResponse(Void response) {
                //This is really meant for TimelineActivity. We don't do anything here.
            }
        });

        return rootView;
    }

    /**
     * If the user has exhausted the list of fetched posts/events, this will fetch another batch of
     * posts.
     * @param listener the listener that will be called when we finish fetching the stuffs.
     */
    public void fetchNewPage(final Response.Listener<Void> listener){
        final List<FeedItem> feedItems = mAdapter.getNetPosts();
        if (settings.getBoolean(TimelineActivity.FILTER_CHOICE_EVENTS, true)) {
            //If events aren't filtered out, add them to arraylist.
            API.Get.networkEvents(queue, selectedNetwork, maxEventId, new Response.Listener<NetworkResponse<List<Event>>>() {
                @Override
                public void onResponse(NetworkResponse<List<Event>> response) {
                    if (!response.fail()) {
                        List<Event> events = response.getPayload();
                        feedItems.addAll(events);
                        if (events.size() > 0) {
                            long newMaxEventId = events.get(events.size() - 1).id - 1 ;
                            if (maxEventId.equals(API.NO_MAX_PAGINATION) || Long.parseLong(maxEventId) > newMaxEventId) {
                                maxEventId = newMaxEventId + "";
                            }
                            mAdapter.notifyDataSetChanged();
                            noPosts.setVisibility(View.GONE);
                        } else if (mAdapter.getNetPosts().size() == 0) {
                            noPosts.setVisibility(View.VISIBLE);
                        }
                        //TODO: CACHE - add to cache, pass feedItems
                    } else {
                        response.showErrorDialog(getActivity());
                    }
                    listener.onResponse(null);
                }
            });
        }
        if (settings.getBoolean(TimelineActivity.FILTER_CHOICE_NATIVE, true)) {
            // If posts aren't filtered out, add them to array list.
            // We also need to get the post replies.
            // Deal with pagination. The first batch has no limit on posts ids.
            API.Get.networkPosts(queue, selectedNetwork, maxPostId, new Response.Listener<NetworkResponse<List<Post>>>() {
                @Override
                public void onResponse(NetworkResponse<List<Post>> response) {
                    if (response.fail()) {
                        response.showErrorDialog(getContext());
                    } else {
                        // We need to sort these posts by date. Oh wait, they're already sorted!
                        ArrayList<Post> posts = (ArrayList<Post>) response.getPayload();
                        // Let's assume that the smallest id is the last item.
                        if (posts.size() > 0) {
                            long newMaxPostId = posts.get(posts.size() - 1).id - 1;
                            if (maxPostId.equals(API.NO_MAX_PAGINATION) || Long.parseLong(maxPostId) > newMaxPostId) {
                                maxPostId = newMaxPostId + "";
                            }
                            for (final Post post : posts) {
                                feedItems.add(post);
                                //Get comments
                                API.Get.postReplies(queue, post.id, new Response.Listener<NetworkResponse<ArrayList<PostReply>>>() {
                                    @Override
                                    public void onResponse(NetworkResponse<ArrayList<PostReply>> response) {
                                        if (!response.fail()) {
                                            post.comments = response.getPayload();
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
                            mAdapter.notifyDataSetChanged();
                            noPosts.setVisibility(View.GONE);
                        } else if (mAdapter.getNetPosts().size() == 0) {
                            noPosts.setVisibility(View.VISIBLE);
                        }
                    }
                    listener.onResponse(null);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     * @param context {@inheritDoc}
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    /**
     * {@inheritDoc}
     */
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
