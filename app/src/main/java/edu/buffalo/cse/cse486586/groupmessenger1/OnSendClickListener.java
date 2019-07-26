package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class OnSendClickListener implements OnClickListener {

    private static final String TAG = OnSendClickListener.class.getName();

    private String msg;
    private final EditText editText;
    private int counter;
    private String myPort;
    private final TextView mTextView ;
   // private final Uri mUri;
    private final ContentValues mContentValues;

    public OnSendClickListener(TextView _tv, EditText _et, int ctr, String myport) {
        myPort=myport;
        mContentValues = new ContentValues();
        counter = ctr;
        editText = _et;
        mTextView = _tv;

       // mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }





    @Override
    public void onClick(View v) {

        msg = editText.getText().toString() + "\n";

        editText.setText(""); // This is one way to reset the input box.
        mTextView.append("\t" + msg);

        Log.i(TAG, "insert Succesful\n");
        new GroupMessengerActivity.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

       }

    /*private boolean testInsert()
    {
        try {

            mContentResolver.insert(mUri, mContentValues);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }

        return true;
    } */

}
