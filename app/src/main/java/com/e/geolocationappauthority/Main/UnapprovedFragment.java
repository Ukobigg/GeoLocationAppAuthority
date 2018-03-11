package com.e.geolocationappauthority.Main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.e.geolocationappauthority.MS_SQL.Adapter;
import com.e.geolocationappauthority.MS_SQL.ConnectionClass;
import com.e.geolocationappauthority.MS_SQL.EmergencyModel;
import com.e.geolocationappauthority.MS_SQL.RecyclerItemClickListener;
import com.e.geolocationappauthority.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static android.content.ContentValues.TAG;

public class UnapprovedFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<EmergencyModel> emergencyModelArrayList;
    private ConnectionClass connectionClass;
    private boolean success = false; // boolean
    SwipeController swipeController = null;
    private Adapter adapter;
    String EmgergencyID;
    String EmergencyTpe;
    String EmergencyLocation;

    public static UnapprovedFragment mainActivity;

    private GoogleCloudMessaging gcm;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static Boolean isVisible = false;
    private static final String TAG = "MainActivity";

    private String HubEndpoint = null;
    private String HubSasKeyName = null;
    private String HubSasKeyValue = null;

    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_EMERGENCYID = "EXTRA_QUOTE";
    private static final String EXTRA_EMERGENCYTYPE = "EXTRA_ATTR";
    private static final String EXTRA_EMERGENCYSTATUS = "EXTRA_ATTR";
    private static final String EXTRA_LONGITUDE = "EXTRA_ATTR";
    private static final String EXTRA_LATITUDE = "EXTRA_ATTR";
    private static final String EXTRA_DATETIME = "EXTRA_ATTR";
    private static final String EXTRA_USERID = "EXTRA_ATTR";
    private static final String EXTRA_EMERGENCYLOCATION = "EXTRA_ATTR";


    public UnapprovedFragment() {
        // Required empty public constructor
    }

    public static UnapprovedFragment newInstance() {
        UnapprovedFragment fragment = new UnapprovedFragment();
        return fragment;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_unapproved, container, false);

        recyclerView = view.findViewById(R.id.RecyclerviewUnapproved);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        mainActivity = this;
        NotificationsManager.handleNotifications(getContext(), NotificationSettings.SenderId, MyHandler.class);
        registerWithNotificationHubs();

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
                        int itemPosition = recyclerView.getChildLayoutPosition(view);
                        String latitude_recycler = emergencyModelArrayList.get(itemPosition).getLatitude();
                        String longitude_recycler = emergencyModelArrayList.get(itemPosition).getLongitude();
                        String EmergencyID_recycler = String.valueOf(emergencyModelArrayList.get(itemPosition).getEmergencyid());
                        String EmergencyType_recycler = emergencyModelArrayList.get(itemPosition).getEmergencytype();
                        String EmergencyStatus_recycler = emergencyModelArrayList.get(itemPosition).getEmergencystatus();
                        String UserID_recycler = emergencyModelArrayList.get(itemPosition).getUserid();
                        String EmergencyLocation_recycler = emergencyModelArrayList.get(itemPosition).getEmergencylocation();

                        Intent intent = new Intent(getActivity().getBaseContext(), ViewMapActivity.class);
                        intent.putExtra("EXTRA_LATITUDE", latitude_recycler);
                        intent.putExtra("EXTRA_LONGITUDE", longitude_recycler);
                        intent.putExtra("EXTRA_EMERGENCYID", EmergencyID_recycler);
                        intent.putExtra("EXTRA_EMERGENCY_TYPE", EmergencyType_recycler);
                        intent.putExtra("EXTRA_USERID", UserID_recycler);
                        intent.putExtra("EXTRA_EMERGENCY_STATUS", EmergencyStatus_recycler);
                        intent.putExtra("EXTRA_EMERGENCYLOCATION",EmergencyLocation_recycler);
                        startActivity(intent);

                        //Include Open Map
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

        emergencyModelArrayList = new ArrayList<EmergencyModel>();
        connectionClass = new ConnectionClass(); // Connection Class Initialization
        SyncData Unapprovedemergency = new SyncData(); // Calling Async Task
        Unapprovedemergency.execute("");
        Log.d(TAG, "onCreateView");
        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override

            public void onRightClicked(int position) {
                EmgergencyID = String.valueOf(emergencyModelArrayList.get(position).getEmergencyid());
                EmergencyTpe = emergencyModelArrayList.get(position).getEmergencytype();
                EmergencyLocation = emergencyModelArrayList.get(position).getEmergencylocation();
                UpdateDataApprove approvedemergency = new UpdateDataApprove(); // Calling Async Task
                approvedemergency.execute("");
                adapter.emergencyModelArrayList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, adapter.getItemCount());
                sendNotification();
            }

            public void onLeftClicked(int position) {
                EmgergencyID = String.valueOf(emergencyModelArrayList.get(position).getEmergencyid());
                adapter.emergencyModelArrayList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, adapter.getItemCount());
                connectionClass = new ConnectionClass();
                UpdateDataArchive archiveemergency = new UpdateDataArchive(); // Calling Async Task
                archiveemergency.execute("");


            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
        return view;
    }

    // Select data from database
    private class SyncData extends AsyncTask<String, String, String>
    {
        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";
        ProgressDialog progress;

        @Override
        protected void onPreExecute() //Starts the progress dailog
        {
            progress = ProgressDialog.show(getActivity(), "Synchronising",
                    "Emergencies Loading! Please Wait...", true);
        }

        @Override
        protected String doInBackground(String... strings)  // Connect to the database, write query and add items to array list
        {
            try
            {
                Connection conn = connectionClass.CONN(); //Connection Object
                if (conn == null)
                {
                    success = false;
                }
                else {
                    // Change below query according to your own database.
                    String query = "SELECT * FROM Emergency WHERE COLUMN_EMERGENCYSTATUS = 1";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    if (rs != null) // if resultset not null, I add items to itemArraylist using class created
                    {
                        while (rs.next())
                        {
                            try {
                                emergencyModelArrayList.add(new EmergencyModel(rs.getString("COLUMN_USERID"),rs.getInt("_ID"),rs.getString("COLUMN_EMERGENCYTYPE"),rs.getString("COLUMN_EMERGENCYSTATUS"), rs.getString("COLUMN_EMERGENCYLOCATION"),rs.getString("COLUMN_LATITUDE"),rs.getString("COLUMN_LONGITUDE"),rs.getString("COLUMN_DATETIME")));

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        msg = "Found";
                        success = true;
                    } else {
                        msg = "No Data found!";
                        success = false;
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                Writer writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                msg = writer.toString();
                success = false;
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) // dismissing progress dialogue, showing error and setting up my listview
        {
            adapter = new Adapter(emergencyModelArrayList);
            recyclerView.setAdapter(adapter);
            Toast.makeText(getActivity(), msg + "", Toast.LENGTH_LONG).show();

            progress.dismiss();
        }
    }

    //Update Approved Emergencies
    private class UpdateDataApprove extends AsyncTask<String, String, String>
    {
        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";
        ProgressDialog progress;

        @Override
        protected void onPreExecute() //Starts the progress dailog
        {
            progress = ProgressDialog.show(getActivity(), "Synchronising",
                    "Emergencies Loading! Please Wait...", true);
        }

        @Override
        protected String doInBackground(String... strings)  // Connect to the database, write query and add items to array list
        {
            try
            {
                Connection conn = connectionClass.CONN(); //Connection Object
                if (conn == null)
                {
                    success = false;
                }
                else {
                    // Change below query according to your own database.
                    String query = "Update Emergency set COLUMN_EMERGENCYSTATUS='2' where _ID="+EmgergencyID;

                    Statement stmt = null;
                    try {
                        stmt = conn.createStatement();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    stmt.executeUpdate(query);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                Writer writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                msg = writer.toString();
                success = false;
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) // dismissing progress dialogue, showing error and setting up my listview
        {
            Toast.makeText(getActivity(), msg + "", Toast.LENGTH_LONG).show();
            progress.dismiss();
        }
    }

    //Update Archived Emergencies
    private class UpdateDataArchive extends AsyncTask<String, String, String>
    {
        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";
        ProgressDialog progress;

        @Override
        protected void onPreExecute() //Starts the progress dailog
        {
            progress = ProgressDialog.show(getActivity(), "Synchronising",
                    "Emergencies Loading! Please Wait...", true);
        }

        @Override
        protected String doInBackground(String... strings)  // Connect to the database, write query and add items to array list
        {
            try
            {
                Connection conn = connectionClass.CONN(); //Connection Object
                if (conn == null)
                {
                    success = false;
                }
                else {
                    // Change below query according to your own database.
                    String query = "Update Emergency set COLUMN_EMERGENCYSTATUS='3' where _ID="+EmgergencyID;

                    Statement stmt = null;
                    try {
                        stmt = conn.createStatement();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    stmt.executeUpdate(query);}
            } catch (Exception e)
            {
                e.printStackTrace();
                Writer writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                msg = writer.toString();
                success = false;
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) // dismissing progress dialogue, showing error and setting up my listview
        {
            Toast.makeText(getActivity(), msg + "", Toast.LENGTH_LONG).show();

            progress.dismiss();
        }
    }
    private void approveItem(final int position){

        // update emergency dstatus
        //Refreshpage
    }

    public void onItemClick(int p) {
        EmergencyModel item = (EmergencyModel) emergencyModelArrayList.get(p);
        Intent i = new Intent(getActivity(),ViewMapActivity.class);
        Bundle selectediteminformation = new Bundle();
        selectediteminformation.putString(EXTRA_EMERGENCYID,String.valueOf(item.getEmergencyid()));
        selectediteminformation.putString(EXTRA_EMERGENCYTYPE,item.getEmergencytype());
        selectediteminformation.putString(EXTRA_EMERGENCYSTATUS,item.getEmergencystatus());
        selectediteminformation.putString(EXTRA_EMERGENCYLOCATION,item.getEmergencylocation());
        selectediteminformation.putString(EXTRA_DATETIME,item.getDatetime());
        selectediteminformation.putString(EXTRA_LATITUDE,item.getLatitude());
        selectediteminformation.putString(EXTRA_LONGITUDE,item.getLongitude());

        i.putExtra(BUNDLE_EXTRAS,selectediteminformation);
        startActivity(i);
    }

    public void registerWithNotificationHubs()
    {
        Log.i(TAG, " Registering with Notification Hubs");

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
            getActivity().startService(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(getActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.");
                getActivity().finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Example code from http://msdn.microsoft.com/library/azure/dn495627.aspx
     * to parse the connection string so a SaS authentication token can be
     * constructed.
     *
     * @param connectionString This must be the DefaultFullSharedAccess connection
     *                         string for this example.
     */
    private void ParseConnectionString(String connectionString)
    {
        String[] parts = connectionString.split(";");
        if (parts.length != 3)
            throw new RuntimeException("Error parsing connection string: "
                    + connectionString);

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("Endpoint")) {
                this.HubEndpoint = "https" + parts[i].substring(11);
            } else if (parts[i].startsWith("SharedAccessKeyName")) {
                this.HubSasKeyName = parts[i].substring(20);
            } else if (parts[i].startsWith("SharedAccessKey")) {
                this.HubSasKeyValue = parts[i].substring(16);
            }
        }
    }

    /**
     * Example code from http://msdn.microsoft.com/library/azure/dn495627.aspx to
     * construct a SaS token from the access key to authenticate a request.
     *
     * @param uri The unencoded resource URI string for this operation. The resource
     *            URI is the full URI of the Service Bus resource to which access is
     *            claimed. For example,
     *            "http://<namespace>.servicebus.windows.net/<hubName>"
     */
    private String generateSasToken(String uri) {

        String targetUri;
        String token = null;
        try {
            targetUri = URLEncoder
                    .encode(uri.toString().toLowerCase(), "UTF-8")
                    .toLowerCase();

            long expiresOnDate = System.currentTimeMillis();
            int expiresInMins = 60; // 1 hour
            expiresOnDate += expiresInMins * 60 * 1000;
            long expires = expiresOnDate / 1000;
            String toSign = targetUri + "\n" + expires;

            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = HubSasKeyValue.getBytes("UTF-8");
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(toSign.getBytes("UTF-8"));

            // Using android.util.Base64 for Android Studio instead of
            // Apache commons codec
            String signature = URLEncoder.encode(
                    Base64.encodeToString(rawHmac, Base64.NO_WRAP).toString(), "UTF-8");

            // Construct authorization string
            token = "SharedAccessSignature sr=" + targetUri + "&sig="
                    + signature + "&se=" + expires + "&skn=" + HubSasKeyName;
        } catch (Exception e) {
            if (isVisible) {
            }
        }

        return token;
    }

    /**
     * Send Notification button click handler. This method parses the
     * DefaultFullSharedAccess connection string and generates a SaS token. The
     * token is added to the Authorization header on the POST request to the
     * notification hub. The text in the editTextNotificationMessage control
     * is added as the JSON body for the request to add a GCM message to the hub.
     *
     * @param
     */
    public void sendNotification() {

        String notificationText = EmergencyTpe+": "+"There is a new Emergency at "+EmergencyLocation;

                final String json = "{\"data\":{\"message\":\"" + notificationText + "\"}}";

        new Thread()
        {
            public void run()
            {
                try
                {
                    // Based on reference documentation...
                    // http://msdn.microsoft.com/library/azure/dn223273.aspx
                    ParseConnectionString(NotificationSettings.HubFullAccess);
                    URL url = new URL(HubEndpoint + NotificationSettings.HubName +
                            "/messages/?api-version=2015-01");

                    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();

                    try {
                        // POST request
                        urlConnection.setDoOutput(true);

                        // Authenticate the POST request with the SaS token
                        urlConnection.setRequestProperty("Authorization", generateSasToken(url.toString()));

                        // Notification format should be GCM
                        urlConnection.setRequestProperty("ServiceBusNotification-Format", "gcm");

                        // Include any tags
                        // Example below targets 3 specific tags
                        // Refer to : https://azure.microsoft.com/en-us/documentation/articles/notification-hubs-routing-tag-expressions/
                        // urlConnection.setRequestProperty("ServiceBusNotification-Tags", "tag1 || tag2 || tag3");

                        // Send notification message
                        urlConnection.setFixedLengthStreamingMode(json.getBytes().length);
                        OutputStream bodyStream = new BufferedOutputStream(urlConnection.getOutputStream());
                        bodyStream.write(json.getBytes());
                        bodyStream.close();

                        // Get reponse
                        urlConnection.connect();
                        int responseCode = urlConnection.getResponseCode();
                        if ((responseCode != 200) && (responseCode != 201)) {
                            if (isVisible) {
                                BufferedReader br = new BufferedReader(new InputStreamReader((urlConnection.getErrorStream())));
                                String line;
                                StringBuilder builder = new StringBuilder("Send Notification returned " +
                                        responseCode + " : ")  ;
                                while ((line = br.readLine()) != null) {
                                    builder.append(line);
                                }

                                ToastNotify(builder.toString());
                            }
                        }
                    } finally {
                        urlConnection.disconnect();
                    }
                }
                catch(Exception e)
                {
                    if (isVisible) {
                        ToastNotify("Exception Sending Notification : " + e.getMessage().toString());
                    }
                }
            }
        }.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        isVisible = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isVisible = false;
    }

    public void ToastNotify(final String notificationMessage)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), notificationMessage, Toast.LENGTH_LONG).show();

            }
        });
    }
}