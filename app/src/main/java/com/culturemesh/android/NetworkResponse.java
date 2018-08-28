package com.culturemesh.android;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Class to store responses after attempting networking tasks
 */
public class NetworkResponse<E> {

    /**
     * Tag to use for log statements. It is set dynamically so that if the class name is refactored,
     * the logging tag will be too.
     */
    private static final String TAG = NetworkResponse.class.getSimpleName();

    /**
     * Whether or not the network task failed.
     */
    private boolean fail;

    /**
     * String reference ID of message describing the error.
     */
    private int messageID;

    /**
     * Object returned by the network task.
     */
    private E payload;

    /**
     * Indicates that the operation failed due to an authentication issue. The user will be
     * directed to the login page after dismissing the error dialog.
     * @see NetworkResponse#genErrorDialog(Context, int, boolean, DialogTapListener)
     */
    private boolean isAuthFailed;

    /**
     * Create a new NetworkResponse of the type designated in {@code <>} from another NetworkResponse
     * of any other type. Any payload in the source object will not be transferred to the created one.
     * All other fields are copied.
     * @param toConvert Source to create new object from. All properties except payload will be
     *                  copied.
     */
    public NetworkResponse(NetworkResponse<?> toConvert) {
        if (!toConvert.fail()) {
            throw new IllegalArgumentException("The provided NetworkResponse Object to convert (" +
                    toConvert + ") is not failed. NetworkResponse(NetworkResponse<Object>) requires" +
                    "an argument NetworkResponse that is failed because the payload is dropped.");
        }
        Log.d(TAG, "Creating new NetworkResponse from " + toConvert.toString());
        fail = toConvert.fail();
        messageID = toConvert.getMessageID();
        isAuthFailed = toConvert.isAuthFailed();
    }

    /**
     * Constructor that creates a generic message based on "inFail"
     * @param inFail Failure state provided by user (true if failed)
     */
    public NetworkResponse(boolean inFail) {
        Log.d(TAG, "Creating new NetworkResponse<?> object with inFail=" + inFail);
        fail = inFail;
        if (inFail)
            messageID = R.string.genericFail;
        else
            messageID = R.string.genericSuccess;
    }

    /**
     * Constructor that sets message and failures state based on arguments
     * @param inFail Failure state provided by user (true if failed)
     * @param inMessageID ID for string resource containing message
     */
    public NetworkResponse(boolean inFail, int inMessageID) {
        Log.d(TAG, "Creating new NetworkResponse<?> object with inFail=" + inFail +
                ", messageID=" + inMessageID);
        fail = inFail;
        messageID = inMessageID;
    }

    /**
     * Constructor that stores a payload and sets the failure state to false
     * @param inPayload Payload returned by networking request
     */
    public NetworkResponse(E inPayload) {
        Log.d(TAG, "Creating new NetworkResponse<?> object with payload=" +
                payloadName(inPayload) + ", which is of type " + payloadType(inPayload));
        payload = inPayload;
        fail = false;
        messageID = R.string.genericSuccess;
    }

    /**
     * Constructor that both stores a payload and sets the failure state from parameters
     * @param inFail Whether or not the network operation failed
     * @param inPayload Payload returned by networking request
     */
    public NetworkResponse(boolean inFail, E inPayload) {
        Log.d(TAG, "Creating new NetworkResponse<?> object with inFail=" + inFail +
                ", payload=" + payloadName(inPayload) + ", which is of type " + payloadType(inPayload));
        payload = inPayload;
        fail = inFail;
        if (inFail)
            messageID = R.string.genericFail;
        else
            messageID = R.string.genericSuccess;
    }

    /**
     * Constructor that both stores a payload and sets the failure state from parameters
     * @param inFail Whether or not the network operation failed
     * @param inPayload Payload returned by networking request
     */
    public NetworkResponse(boolean inFail, E inPayload, int messageID) {
        Log.d(TAG, "Creating new NetworkResponse<?> object with inFail=" + inFail +
                ", payload=" + payloadName(inPayload) + ", which is of type " +
                payloadType(inPayload) + ", messageID=" + messageID);
        payload = inPayload;
        fail = inFail;
        this.messageID = messageID;
    }

    /**
     * Get a {@link String} describing the type of the provided payload
     * @param payload Payload whose type will be described. Can be null.
     * @return {@code null} if the payload is null, the name of the payload's class otherwise
     */
    private String payloadType(E payload) {
        String payloadType;
        if (payload == null) {
            payloadType = "null";
        } else {
            payloadType = payload.getClass().getSimpleName();
        }
        return payloadType;
    }

    /**
     * Get a {@link String} describing the provided payload
     * @param payload Payload which will be described. Can be null.
     * @return {@code null} if the payload is null, the {@code payload.toString()} otherwise.
     */
    private String payloadName(E payload) {
        String payloadName;
        if (payload == null) {
            payloadName = "null";
        } else {
            payloadName = payload.toString();
        }
        return payloadName;
    }

    /**
     * Get a NetworkResponse object with {@link NetworkResponse#isAuthFailed} is {@code true}. This
     * means that when the user dismisses the error dialog generated by
     * {@link NetworkResponse#getErrorDialog(Context, DialogTapListener)} or
     * {@link NetworkResponse#showErrorDialog(Context)}, {@link LoginActivity} will be launched.
     * @param messageID String reference to the message describing the error. Will be shown to user
     * @return NetworkResponse object to describe an authentication failure.
     */
    public static NetworkResponse<API.Get.LoginResponse> getAuthFailed(int messageID) {
        Log.d(TAG, "Creating new authFailed NetworkResponse<API.Get.LoginResponse> object with " +
                "messageID=" + messageID);
        NetworkResponse<API.Get.LoginResponse> nr = new NetworkResponse<>(true, messageID);
        nr.isAuthFailed = true;
        return nr;
    }

    /**
     * Get whether the current object represents a failed authentication
     * @return {@code true} if object represents an authentication failure, {@code false} otherwise
     */
    public boolean isAuthFailed() {
        Log.d(TAG, "Returning isAuthFailed=" + isAuthFailed);
        return isAuthFailed;
    }

    /**
     * Set whether the current object represents a failed authentication
     * @param isAuthFailed {@code true} if object represents an authentication failure, {@code false}
     *                                 otherwise
     */
    public void setAuthFailed(boolean isAuthFailed) {
        Log.d(TAG, "Setting isAuthFailed to be " + isAuthFailed);
        this.isAuthFailed = isAuthFailed;
    }

    /**
     * Check whether the network request failed
     * @return true if the request failed, false if it succeeded
     */
    public boolean fail() {
        Log.d(TAG, "Returning fail=" + fail);
        return fail;
    }

    /**
     * Get the resource ID of the message to display to the user
     * @return Resource ID of message
     */
    public int getMessageID() {
        Log.d(TAG, "Returning messageID=" + messageID);
        return messageID;
    }

    /**
     * Get an error dialog that can be displayed to show message from messageID to user
     * @param context Context upon which to display error dialog (Should be {@code someClass.this})
     * @param listener A {@link DialogTapListener} to be  called when they dismiss the dialog.
     * @return Dialog that can be shown
     */
    public AlertDialog getErrorDialog(Context context, DialogTapListener listener) {
        if (isAuthFailed) {
            Log.d(TAG, "Returning an authFailed Error Dialog for context=" + context.getPackageName());
            return genErrorDialog(context, messageID, true, listener);
        } else {
            Log.d(TAG, "Returning a non-authFailed Error Dialog for context=" + context.getPackageName());
            return genErrorDialog(context, messageID, listener);
        }
    }

    /**
     * Get an error dialog that can be displayed to the user
     * @param context Context upon which to display error dialog (Should be {@code someClass.this})
     * @param messageID String resource ID of message to display
     * @return {@link AlertDialog} with specified alert message.
     */
    public static AlertDialog genErrorDialog(Context context, int messageID) {
        Log.d(TAG, "Generating an Error Dialog for context=" + context.getPackageName() + ", " +
                "messageID=" + messageID + ". authFail not specified, so it is false");
        return genErrorDialog(context, messageID, false, new DialogTapListener() {
            @Override
            public void onDismiss() {
                // Do nothing.
            }
        });
    }


    /**
     * Get an error dialog that can be displayed to the user
     * @param context Context upon which to display error dialog (Should be {@code someClass.this})
     * @param messageID String resource ID of message to display
     * @param listener A {@link DialogTapListener} for when the user dismisses the dialog.
     * @return {@link AlertDialog} with specified alert message.
     */
    public static AlertDialog genErrorDialog(Context context, int messageID, DialogTapListener listener) {
        Log.d(TAG, "Generating an Error Dialog for context=" + context.getPackageName() + ", " +
                "messageID=" + messageID + ". authFail not specified, so it is false");
        return genErrorDialog(context, messageID, false, listener);
    }

    /**
     * Get an error dialog that can be displayed to the user
     * @param context Context upon which to display error dialog (Should be {@code someClass.this})
     * @param messageID String resource ID of message to display
     * @param authFail Whether or not the user should be directed to {@link LoginActivity} upon
     *                 dismissing the dialog
     * @param mListener A {@link DialogTapListener} for when the user dismisses the dialog.
     * @return {@link AlertDialog} with specified alert message and which directs the user to
     * {@link LoginActivity} upon dismissal if {@code authFail} is true.
     */
    public static AlertDialog genErrorDialog(final Context context, int messageID, final boolean authFail,
                                             final DialogTapListener mListener) {
        Log.d(TAG, "Generating an Error Dialog for context=" + context.getPackageName() + ", " +
                "messageID=" + messageID + ", authFail=" + authFail);
        // SOURCE: https://stackoverflow.com/questions/26097513/android-simple-alert-dialog
        AlertDialog errDialog = new AlertDialog.Builder(context).create();
        errDialog.setTitle(context.getString(R.string.error));
        if (messageID != 0) {
            errDialog.setMessage(context.getString(messageID));
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (authFail) {
                    Log.d(TAG, "Error dialog dismissed, so launching Intent to LoginActivity " +
                            "and signing user out");
                    Intent toSignIn = new Intent(context, LoginActivity.class);
                    SharedPreferences settings = context.getSharedPreferences(
                            API.SETTINGS_IDENTIFIER, Context.MODE_PRIVATE);
                    LoginActivity.setLoggedOut(settings);
                    context.startActivity(toSignIn);
                }
                //Call listener in case client wants to do something specific.
                mListener.onDismiss();
                Log.d(TAG, "Error dialog dismissed");
                dialog.dismiss();
            }
        };
        errDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.ok), listener);
        return errDialog;
    }

    /**
     * Get a confirmation dialog that can be displayed to the user to reflect a successful operation
     * @param context Context upon which to display dialog (Should be {@code someClass.this})
     * @param messageID String resource ID of message to display
     * @return {@link AlertDialog} with specified alert message
     */
    public static AlertDialog genSuccessDialog(Context context, int messageID) {
        Log.d(TAG, "Generating a Success Dialog for context=" + context.getPackageName() + ", " +
                "messageID=" + messageID);
        // SOURCE: https://stackoverflow.com/questions/26097513/android-simple-alert-dialog
        AlertDialog errDialog = new AlertDialog.Builder(context).create();
        errDialog.setTitle(context.getString(R.string.success));
        if (messageID != 0) {
            errDialog.setMessage(context.getString(messageID));
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        errDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.ok), listener);
        return errDialog;
    }

    /**
     * Show an error dialog that can be displayed to show message from messageID to user
     * @param context Context upon which to display error dialog
     * @param listener A {@link DialogTapListener} object which allows you control behavior
     *                 after they dismiss the dialog.
     */
    public void showErrorDialog(Context context, DialogTapListener listener) {
        Log.d(TAG, "Showing error dialog for context=" + context.getPackageName());
        AlertDialog errDialog = getErrorDialog(context, listener);
        errDialog.show();
    }

    /**
     * Show an error dialog that can be displayed to show message from messageID to user
     * @param context Context upon which to display error dialog
     */
    public void showErrorDialog(Context context) {
        Log.d(TAG, "Showing error dialog for context=" + context.getPackageName());
        AlertDialog errDialog = getErrorDialog(context, new DialogTapListener() {
            @Override
            public void onDismiss() {
                //Do nothing
            }
        });
        errDialog.show();
    }

    /**
     * Get the payload returned by the network operation
     * @return Payload returned by network operation
     */
    public E getPayload() {
        String type = "";
        if (payload == null) {
            type = "null";
        } else {
            type = payload.getClass().getSimpleName();
        }
        Log.d(TAG, "Returning payload=" + payload + ", which is of type=" + type);
        // TODO: This should throw an exception if payload is undefined
        return payload;
    }

    /**
     * Get a String representation of the object that conveys the current state of all instance fields
     * @return String representation of the form {@code NetworkResponse<?>[field1=value1, ...]}
     */
    public String toString() {
        return "NetworkResponse<?>[fail=" + fail + ", messageID=" + messageID + ", payload=" +
                payloadName(payload) + ", payloadType=" + payloadType(payload) + ", authFail=" +
                isAuthFailed + "]";
    }

    public interface DialogTapListener {
        public void onDismiss();
    }
}
