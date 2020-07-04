package org.fict.ficttools.Selection;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.fict.ficttools.R;
import org.fict.ficttools.Selection.AttendanceScan.AttendanceScan;
import org.fict.ficttools.Selection.AttendanceScan.GetDataActivity;
import org.fict.ficttools.Selection.AttendanceScan.ManageAttendanceActivity;
import org.fict.ficttools.Selection.MembersScan.CheckFee;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

public class Attendance extends AppCompatActivity {
    public String status;
    AutoCompleteTextView titleTv;
    TextView txtName;
    ImageView imageScan;
    NfcAdapter nfcAdapter;
    CardView nFCview;
    boolean switchScanner = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        setTitle("Attendance via NFC Tag");
        titleTv = findViewById(R.id.txtTitle);
        txtName = findViewById(R.id.txtName);
        imageScan = findViewById(R.id.imageScan);
        nFCview = findViewById(R.id.nFCView);
        try{
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            loadEvents();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if(getIntent().hasExtra("title")){
            titleTv.setText(getIntent().getExtras().getString("title"));
        }
        if(getIntent().hasExtra("name")){
            txtName.setText(getIntent().getExtras().getString("name"));
        }
        if(getIntent().hasExtra("status")){
            status = getIntent().getExtras().getString("status");
            if("OK".equals(status)){
                MediaPlayer okSound = MediaPlayer.create(this, R.raw.oksound);
                okSound.start();
            }
        }

        imageScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if ("".equals(titleTv.getText().toString())) {
                        Toast.makeText(Attendance.this, "Please enter title of the event before you scan. Eg: FICT Day, Congress 2019.", Toast.LENGTH_LONG).show();
                    } else {
                        if (nfcAdapter.isEnabled()) {
                            if (!switchScanner) {
                                //Turn ON.
                                nFCview.setCardBackgroundColor(getResources().getColor(R.color.green));
                                switchScanner = true;
                                enableForegroundDispatchSystem();
                                Toast.makeText(Attendance.this, "Now Scanning!", Toast.LENGTH_SHORT).show();
                            } else {
                                //Turn OFF.
                                nFCview.setCardBackgroundColor(getResources().getColor(R.color.buttonColor));
                                switchScanner = false;
                                disableForegroundDispatchSystem();
                                Toast.makeText(Attendance.this, "Action Stopped", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Attendance.this, "Please enable NFC on your phone settings!", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
                catch (Exception e){e.printStackTrace();}
            }

        });



    }
    public String getTextFromNdefRecord(NdefRecord ndefRecord)
    {
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1,
                    payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "Cannot read from getTextFromNdefRecord method!", Toast.LENGTH_SHORT).show();
        }
        return tagContent;
    }

    private void readTextFromMessage(NdefMessage ndefMessage) {

        NdefRecord[] ndefRecords = ndefMessage.getRecords();

        if(ndefRecords != null && ndefRecords.length>0){
            NdefRecord ndefRecord = ndefRecords[0];
            String tagContent = getTextFromNdefRecord(ndefRecord);
            switchScanner = false;
            nFCview.setCardBackgroundColor(getResources().getColor(R.color.buttonColor));
            Intent go = new Intent(this, GetDataActivity.class);
            go.putExtra("codeID", ""+tagContent);
            go.putExtra("title", ""+titleTv.getText().toString());
            startActivity(go);
        }

    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if(parcelables != null && parcelables.length > 0)
            {readTextFromMessage((NdefMessage) parcelables[0]); }
            else{Toast.makeText(this, "No data/File corrupted", Toast.LENGTH_SHORT).show();}
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

            enableForegroundDispatchSystem();


    }

    @Override
    protected void onPause() {
        super.onPause();

        disableForegroundDispatchSystem();


}

    private void enableForegroundDispatchSystem() {

        Intent intent = new Intent(this, Attendance.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    private void disableForegroundDispatchSystem() {
        nfcAdapter.disableForegroundDispatch(this);
    }


    private void loadEvents() {

        final ArrayList<String> eventListArray;
        FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
        eventListArray = new ArrayList<>();
        FireInstance.collection("Attendance")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){

                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                if(eventListArray.contains(document.getString("Event"))){
                                    //Skip. Already in spinner.
                                }
                                else{
                                    eventListArray.add(document.getString("Event"));
                                }
                            }
                            Collections.sort(eventListArray, String.CASE_INSENSITIVE_ORDER);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(Attendance.this, android.R.layout.simple_list_item_1, eventListArray);
                            titleTv.setAdapter(adapter);
                        }
                        //else queryDocumentSnapshots.isEmpty()
                    }
                    //onFailure
                });
    }

    public void manage(View view){
        Intent go = new Intent(this, ManageAttendanceActivity.class);
        startActivity(go);
    }


}
