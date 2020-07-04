package org.fict.ficttools.Selection;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.fict.ficttools.NavigationActivity;
import org.fict.ficttools.R;
import org.fict.ficttools.Selection.MembersScan.CheckFee;
import org.fict.ficttools.Selection.MembersScan.MembersScan;

import java.io.UnsupportedEncodingException;

public class Members extends AppCompatActivity {
    ImageView scanCodeIMG;
    TextView status, memName, memCourse;
    Button memBack;
    NfcAdapter nfcAdapter;
    CardView nfc_background;
    Spinner spinner;
    boolean switchScanner = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        setTitle(R.string.members_title);
        memBack = findViewById(R.id.memBack);
        nfc_background = findViewById(R.id.nfc_background);
        memName = findViewById(R.id.memName);
        memCourse = findViewById(R.id.memCourse);
        scanCodeIMG = findViewById(R.id.scanCodeIMG);
        spinner = findViewById(R.id.spinner);

        status = findViewById(R.id.status);


        if(getIntent().hasExtra("Found")){
            if("NO".equals(getIntent().getExtras().getString("Found"))){
                status.setText("Unknown Member: "+getIntent().getExtras().getString("codeID"));
                status.setBackgroundColor(getResources().getColor(R.color.red));
                memName.setText("???");
                memCourse.setText("???");
                memName.setTextColor(getResources().getColor(R.color.gray));
                memCourse.setTextColor(getResources().getColor(R.color.gray));
                MediaPlayer okSound = MediaPlayer.create(this, R.raw.errorsound);
                okSound.start();
            }
            else if ("YES".equals(getIntent().getExtras().getString("Found"))){
                if ("PAID".equals(getIntent().getExtras().getString("Status"))){
                    status.setText("STATUS: PAID.");
                    status.setBackgroundColor(getResources().getColor(R.color.green));
                    memName.setTextColor(getResources().getColor(R.color.green));
                    memCourse.setTextColor(getResources().getColor(R.color.green));
                    MediaPlayer okSound = MediaPlayer.create(this, R.raw.oksound);
                    okSound.start();
                }
                else
                {
                    status.setText("STATUS: NOT PAID.");
                    status.setBackgroundColor(getResources().getColor(R.color.red));
                    memName.setTextColor(getResources().getColor(R.color.red));
                    memCourse.setTextColor(getResources().getColor(R.color.red));
                    MediaPlayer okSound = MediaPlayer.create(this, R.raw.errorsound);
                    okSound.start();

                }
                memName.setText(getIntent().getExtras().getString("Name"));
                memCourse.setText(getIntent().getExtras().getString("Course"));


            }
        }

        scanCodeIMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (nfcAdapter.isEnabled()){
                        if (!switchScanner){
                            //Turn ON.
                            nfc_background.setCardBackgroundColor(getResources().getColor(R.color.green));
                            switchScanner = true;
                            enableForegroundDispatchSystem();
                            Toast.makeText(Members.this, "Now Scanning!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            //Turn OFF.
                            nfc_background.setCardBackgroundColor(getResources().getColor(R.color.buttonColor));
                            switchScanner = false;
                            disableForegroundDispatchSystem();
                            Toast.makeText(Members.this, "Action Stopped", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(Members.this, "Please enable NFC on your phone settings!", Toast.LENGTH_SHORT).show();
                    }

                 // TODO: SCAN WITH QR CODE
                // Intent go = new Intent(Members.this, MembersScan.class);
               // startActivity(go);
            }
        });


        memBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goBack = new Intent(Members.this, NavigationActivity.class);
                startActivity(goBack);
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
            nfc_background.setCardBackgroundColor(getResources().getColor(R.color.buttonColor));
            Intent go = new Intent(this, CheckFee.class);
            go.putExtra("codeID", ""+tagContent);
            go.putExtra("action", spinner.getSelectedItem().toString());
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
        try{
            enableForegroundDispatchSystem();
        }
        catch (Exception e){e.printStackTrace();}


    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatchSystem();


    }
    public void enableForegroundDispatchSystem() {

        Intent intent = new Intent(this, Members.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    public void disableForegroundDispatchSystem() {
        nfcAdapter.disableForegroundDispatch(this);
    }

}
