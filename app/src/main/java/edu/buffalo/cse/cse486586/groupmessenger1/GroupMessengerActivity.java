package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

import static android.content.ContentValues.TAG;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

        static final String TAG = GroupMessengerActivity.class.getSimpleName();
        static final String[] REMOTE_PORT = {"11108","11112", "11116", "11120", "11124"};
        static final int SERVER_PORT = 10000;
        int counter = 0;
        int flag;

        {
            flag = 0;
        }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);


        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
                * port.
                */
        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        }

        catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        final EditText editText = (EditText) findViewById(R.id.editText1);

        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    /*
                     * If the key is pressed (i.e., KeyEvent.ACTION_DOWN) and it is an enter key
                     * (i.e., KeyEvent.KEYCODE_ENTER), then we display the string. Then we create
                     * an AsyncTask that sends the string to the remote AVD.
                     */
                    String msg = editText.getText().toString() + "\n";
                    editText.setText(""); // This is one way to reset the input box.

                    tv.append("\t" + msg); // This is one way to display a string.
                    tv.setMovementMethod(new ScrollingMovementMethod());

                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                    return true;
                }
                return false;
            }
        });

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */


        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /* Registers OnSendClickListener for "button4" in the layout, which is the "Send" button.
            */

        findViewById(R.id.button4).setOnClickListener(
                new OnSendClickListener(tv, editText, counter, myPort));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        /* References:-
            https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
           */
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            Socket clientSocket = null;

            String file;
            ContentValues values = new ContentValues();
            String scheme="content";
            String authority="edu.buffalo.cse.cse486586.groupmessenger1.provider";
            Uri mUri;
            ContentResolver contentResolver = getContentResolver();

            try {

                while(true)
                {
                    clientSocket = serverSocket.accept();
                    BufferedReader readObj = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));

                    String msgReceived = readObj.readLine();

                    //file = Integer.toString(counter);
                    values.put("key", Integer.toString(counter));
                    values.put("value", msgReceived);

                    Uri.Builder uriBuilder = new Uri.Builder();
                    uriBuilder.authority(authority);
                    uriBuilder.scheme(scheme);
                    mUri = uriBuilder.build();

                    contentResolver.insert(mUri, values);

                    counter++;
                    //if (!msgReceived.equals("\0"))
                       // flag = 1;
                    publishProgress(msgReceived);

                }

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView textView1 = (TextView) findViewById(R.id.textView1);
            textView1.append(strReceived + "\t\n");
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    static class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            String[] remotePort = new String[5];
            try {

                for (int i = 0; i < 5; i++)
                {
                    remotePort[i] = REMOTE_PORT[i];

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort[i]));
                    String msgToSend = msgs[0];
                    PrintWriter writeObj =
                            new PrintWriter(socket.getOutputStream(), true);

                    writeObj.println(msgToSend);

                    /*if (flag == 1) {
                        flag = 0;
                        socket.close();
                    } else {
                        System.err.println(); */
                    }
                }


                catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                }

            return null;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
}
