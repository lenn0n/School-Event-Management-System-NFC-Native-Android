package org.fict.ficttools;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.fict.ficttools.Selection.ToolsActivity;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class generateActivity extends AppCompatActivity {
    Spinner spinME;
    String  studentCode;
    Button bumalikKa;
    NfcAdapter nfcAdapter;
    ArrayList<String> name;
    ArrayList<String> idstudent;
    TextView studentName, doneMessage;
    int min = 0, max=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        setTitle("NFC Tag Generator");
        spinME = findViewById(R.id.spinME);
        bumalikKa = findViewById(R.id.bumalikKa);
        studentName = findViewById(R.id.studentName);
        doneMessage = findViewById(R.id.doneMessage);
        name = new ArrayList<>();
        idstudent = new ArrayList<>();
        try{
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            loadSections();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        bumalikKa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(generateActivity.this, NavigationActivity.class);
                startActivity(go);
            }
        });

        spinME.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                disableForegroundDispatchSystem();
                name.clear();
                idstudent.clear();
                max = 0;
                min = 0;
                studentCode = "";
                studentName.setText("Place the NFC card atleast 10cm distance.");
                doneMessage.setText("["+spinME.getSelectedItem().toString()+"] is ready.");
                FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                FireInstance.collection("Users")
                        .whereEqualTo("Course", spinME.getSelectedItem().toString())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (DocumentSnapshot items : queryDocumentSnapshots) {
                                    name.add(items.getString("Name"));
                                    idstudent.add(ToolsActivity.encode(items.getString("StudentID")));
                                    max = max +1;
                                }
                                enableForegroundDispatchSystem();

                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void loadSections() {

        final ArrayList<String> sectionListArray2;
        FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
        sectionListArray2 = new ArrayList<>();
        FireInstance.collection("Users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){

                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                if(sectionListArray2.contains(document.getString("Course"))){
                                    //Skip. Already in spinner.
                                }
                                else{
                                    sectionListArray2.add(document.getString("Course"));
                                }
                            }
                            Collections.sort(sectionListArray2, String.CASE_INSENSITIVE_ORDER);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(generateActivity.this, android.R.layout.simple_spinner_item, sectionListArray2);
                            adapter.setDropDownViewResource(R.layout.simple_dropdown_white);
                            spinME.setAdapter(adapter);
                        }
                        //else queryDocumentSnapshots.isEmpty()
                    }
                    //onFailure
                });
    }
    public void populate(){
        if (min < max){
            studentName.setText(name.get(min));
            studentCode = ""+idstudent.get(min);
            min = min + 1;
            Toast.makeText(generateActivity.this, ""+studentName.getText().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)){
            if (min < max) {
                studentCode = "" + idstudent.get(min);
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                NdefMessage ndefMessage = createNdefMessage(studentCode);
                writeNFCtag(tag, ndefMessage);
            }
            else{
                Toast.makeText(generateActivity.this, "Cannot proceed anymore. Reason: Last student of the class.", Toast.LENGTH_SHORT).show();
            }
        }

    }
    @Override
    protected void onPause() {
        super.onPause();
        try{
            disableForegroundDispatchSystem();
        }
        catch (Exception e){e.printStackTrace();}

    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            enableForegroundDispatchSystem();
        }
        catch (Exception e){e.printStackTrace();}
    }

    private void enableForegroundDispatchSystem(){
        Intent intent = new Intent(this, generateActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }
    private void disableForegroundDispatchSystem(){

        nfcAdapter.disableForegroundDispatch(this);
    }

    private void writeNFCtag(Tag tag, NdefMessage ndefMessage){
        try{
            if (tag == null){
                Toast.makeText(this, "No object selected", Toast.LENGTH_SHORT).show();
                return;
            }
            Ndef ndef = Ndef.get(tag);

            if (ndef == null){
                formatNFCtag(tag, ndefMessage);
            }
            else{
                ndef.connect();
                if (!ndef.isWritable()){
                    Toast.makeText(this, "This NFC is not writable!", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                MediaPlayer okSound = MediaPlayer.create(this, R.raw.oksound);
                okSound.start();
                Toast.makeText(this, "Successfully written!", Toast.LENGTH_SHORT).show();
                doneMessage.setText("was written in the card.");
                populate();
            }
        }
        catch (Exception e){
            MediaPlayer okSound = MediaPlayer.create(this, R.raw.errorsound);
            okSound.start();
            Toast.makeText(this, "An error occured while writing tag, Exception E", Toast.LENGTH_SHORT).show();
        }
    }
    private void formatNFCtag(Tag tag, NdefMessage ndefMessage){
        try{
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if (ndefFormatable == null){
                Toast.makeText(this, "This NFC card cannot be formatted!", Toast.LENGTH_SHORT).show();
                return;
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
            MediaPlayer okSound = MediaPlayer.create(this, R.raw.oksound);
            okSound.start();
            Toast.makeText(this, "Successfully written!", Toast.LENGTH_SHORT).show();
            doneMessage.setText("was written in the card.");
            populate();
        }
        catch (Exception e){
            MediaPlayer okSound = MediaPlayer.create(this, R.raw.errorsound);
            okSound.start();
            Toast.makeText(this, "An error occured, while formatting. Exception E", Toast.LENGTH_SHORT).show();
        }
    }
    private NdefMessage createNdefMessage(String content){
        NdefRecord ndefRecord = createTextRecord(content);
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
        return  ndefMessage;
    }
    private NdefRecord createTextRecord(String content){
        try{
            byte[] language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");

            final byte[] text = content.getBytes("UTF-8");
            final int languageSize = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);

            payload.write((byte) (languageSize & 0x1F));
            payload.write(language, 0, languageSize);
            payload.write(text, 0, textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());

        }
        catch (UnsupportedEncodingException e){
            MediaPlayer okSound = MediaPlayer.create(this, R.raw.errorsound);
            okSound.start();
            Toast.makeText(this, "createTextRecord method error!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}
