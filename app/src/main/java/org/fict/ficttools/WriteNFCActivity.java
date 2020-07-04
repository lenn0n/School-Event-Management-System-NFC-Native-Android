package org.fict.ficttools;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.LocaleList;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class WriteNFCActivity extends AppCompatActivity {
    NfcAdapter nfcAdapter;
    String studentCode; //TODO: This will be dynamic!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_nfc);

        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            setTitle("TAP NFC TO PROCEED");
            disableForegroundDispatchSystem();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)){
            Toast.makeText(this, "NFC spotted!", Toast.LENGTH_SHORT).show();
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefMessage ndefMessage =createNdefMessage(studentCode);
            writeNFCtag(tag, ndefMessage);
        }

    }
    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatchSystem();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatchSystem();
    }

    private void enableForegroundDispatchSystem(){
        Intent intent = new Intent(this, WriteNFCActivity.class);
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
                Toast.makeText(this, "Successfully written!", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
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
            Toast.makeText(this, "Successfully written!", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(this, "An error occured, Exception E", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "createTextRecord method error!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }


}
