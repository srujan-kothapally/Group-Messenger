package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import static android.content.ContentValues.TAG;
import static java.lang.Math.max;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    ArrayList<Type> msgcount = new ArrayList<Type>();
    Hashtable hs = new Hashtable();
    //static final String REMOTE_PORT0 = "11108";
    //static final String REMOTE_PORT1 = "11112";
    String[] remoteports= new String[]{"11108","11112","11116","11120","11124"};
    static final int SERVER_PORT = 10000;
    public int mcount = 0;
    public int s=0;
    public int si=0;
    private ContentValues[] myContentValues;
    Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));


        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            e.printStackTrace();
            Log.i(TAG, "Can't create a ServerSocket");

            return;
        }
        Log.i(TAG, "ss created");

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        final EditText editText = (EditText) findViewById(R.id.editText1);
        Button b4=(Button)findViewById(R.id.button4);
        Log.i("myportt", myPort);

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));


        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView localTextView = (TextView) findViewById(R.id.textView1);
                //      localTextView.append("\t" + msg); // This is one way to display a message
                Log.i("myTag",myPort);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg,myPort);
            }
        });
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {

            ServerSocket serverSocket = sockets[0];
            Log.e(TAG, "connection established");
            Object object = new Object();
            try {
                serverSocket.setSoTimeout(100000);

                while (true) {
                    try {
                        Log.e(TAG, "before accept");
                        Socket socket = serverSocket.accept();
                        Log.e(TAG, "after accept");

                        ArrayList<Integer> clal = null;
                        ArrayList<Integer> al = null;
                        clal = new ArrayList<Integer>();
                        al = new ArrayList<Integer>();
                        ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream()); //Error Line!

                        object = objectInput.readObject();
                        clal = (ArrayList<Integer>) object;
                        Log.i("recvd 1st msg frm clint", String.valueOf(clal));
                        Log.e(TAG, "readline");
                        si++;
                        clal.add(si);
                        clal.add(0);
                        Log.i("clientarrlistintoq", String.valueOf(clal));
                        PriorityQueue<ArrayList<Integer>> holdbackqueue = new PriorityQueue<ArrayList<Integer>>(10, new Comparator<ArrayList<Integer>>() {
                            @Override
                            public int compare(ArrayList<Integer> a, ArrayList<Integer> b) {
                                return (int) a.get(3) - b.get(3);
                            }
                        });
                        holdbackqueue.add(clal);
                        Iterator value1 = holdbackqueue.iterator();
                        while (value1.hasNext()) {
                            ArrayList<Integer> it = (ArrayList<Integer>) value1.next();
                            Log.i("arryinpque", String.valueOf(it));
                        }
                        ArrayList<Integer> seal = new ArrayList<Integer>();
                        seal.add(si);
                        int stw = Integer.parseInt(String.valueOf(clal.get(4)));
                        int mmid = Integer.parseInt(String.valueOf(clal.get(1)));
                        seal.add(stw);
                        seal.add(mmid);
                        ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                        objectOutput.writeObject(seal);
                        ObjectInputStream objectInput1 = new ObjectInputStream(socket.getInputStream());
                        Object object1 = objectInput1.readObject();
                        al = (ArrayList<Integer>) object1;
                        Log.i("arr received fromclnt", String.valueOf(al));
                        int mid = Integer.parseInt(String.valueOf(al.get(2)));
                        Iterator value = holdbackqueue.iterator();
                        while (value.hasNext()) {
                            ArrayList<Integer> it = (ArrayList<Integer>) value.next();
                            Log.i("arrinhodback", String.valueOf(it));

                                if (mid == Integer.parseInt(String.valueOf(it.get(1)))) {
                                int suggested_number = Integer.parseInt(String.valueOf(al.get(0)));
                                int suggested_process = Integer.parseInt(String.valueOf(al.get(1)));
                                int final_seq_number = max(it.get(5), suggested_number);
                                if(it.get(5)!=suggested_number){
                                    Log.i("finalcheck_num", String.valueOf(it));
                                }
                                if(Integer.parseInt(String.valueOf(it.get(4)))!=suggested_process){
                                    Log.i("finalcheck_suggproce", String.valueOf(it));
                                }
                                it.set(5, final_seq_number);
                                it.set(4, suggested_process);
                                it.set(6, 1);
                                Log.i("agreed", String.valueOf(it));
                            }
                            if (mid != Integer.parseInt(String.valueOf(it.get(1)))){
                                Log.i("finalcheckzerodel", String.valueOf(it));
                            }
                        }
                        ArrayList<ArrayList> fifo = new ArrayList<ArrayList>();
                        if (holdbackqueue.peek().get(6) == 1) {
                            fifo.add(holdbackqueue.peek());
                            publishProgress(String.valueOf(holdbackqueue.peek().get(0)));
                            holdbackqueue.remove();
                        }
                        Iterator fif = fifo.iterator();

                        while(fif.hasNext()){
                            ArrayList<Integer> iter = (ArrayList<Integer>) fif.next();

                            Log.i("fifo", String.valueOf(iter));
                        }
                        Log.i("myasiiii", String.valueOf(clal));
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "The title list has not come from the server");
                        e.printStackTrace();
                    } catch (StreamCorruptedException e) {
                        Log.e(TAG, "Stream exception");
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.e(TAG, "The socket for reading the object has problem");
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "File axax");
                    }

                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            ContentResolver cr = getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put("key", Integer.toString(s));
            cv.put("value", strReceived);
            cr.insert(mUri, cv);
            Cursor resultCursor = cr.query(mUri, null, Integer.toString(s), null, null);
            s=s+1;
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");

        }
    }
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            String myPort = msgs[1];
            Socket[] socketobj = new Socket[5];
            // Object stream has been referred from stack overflow: https://stackoverflow.com/questions/12895450/sending-an-arrayliststring-from-the-server-side-to-the-client-side-over-tcp-us
            ObjectOutputStream[] objOutput = new ObjectOutputStream[5];
            ObjectInputStream[] objInput = new ObjectInputStream[5];
            mcount++;
            String mc = Integer.toString(mcount);
            String mid = msgs[1]+mc;
            ArrayList<String> mal = new ArrayList<String>();
            ArrayList<ArrayList<Integer>> seq =
                    new ArrayList<ArrayList<Integer>>();
            mal.add(msgs[0]);
            mal.add(mid);
            mal.add(myPort);
            mal.add(mc);

            Log.i("myportt", myPort);
            try {
                for (int i = 0; i < remoteports.length; i++) {
                    String remotePort = remoteports[i];
                    Thread.sleep(01);
                    socketobj[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));
                    String msgToSend = msgs[0];
                    mal.add(remotePort);
                    Log.i("mal sent to server", String.valueOf(mal));
                    try {
                        objOutput[i] = new ObjectOutputStream(socketobj[i].getOutputStream());
                        objOutput[i].writeObject(mal);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mal.remove(4);
                    try {
                        objInput[i] = new ObjectInputStream(socketobj[i].getInputStream()); //Error Line!
                        try {
                            Object object = objInput[i].readObject();
                            ArrayList rec = new ArrayList();
                            rec = (ArrayList) object;
                            seq.add(rec);
                            Log.i("received from server", String.valueOf(rec));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Collections.sort(seq, new Comparator<ArrayList<Integer>>() {
                    @Override
                    public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
                        return (int) (o2.get(1) - o1.get(1));
                    }
                });
                Log.i("recei all seq frm serv", String.valueOf(seq));
                int max = 0;
                ArrayList asi = null;
                for (int l = 0; l < seq.size(); l++) {
                    ArrayList a = seq.get(l);
                    if ((Integer) a.get(0) >= max) {
                        asi = seq.get(l);
                    }
                }
                asi.add(mid);
                asi.add(myPort);
                for(int j=0; j<5; j++) {
                    try {
                        ObjectOutputStream objectOutput = new ObjectOutputStream(socketobj[j].getOutputStream());
                        objectOutput.writeObject(asi);
                        Log.i("sent prop seq to server", String.valueOf(asi));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.e(TAG, "sleeperror");
                e.printStackTrace();
            }
            return null;
        }
    }
}
