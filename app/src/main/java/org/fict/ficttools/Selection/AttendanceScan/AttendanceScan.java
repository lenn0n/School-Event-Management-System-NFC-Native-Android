package org.fict.ficttools.Selection.AttendanceScan;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.support.constraint.Constraints.TAG;

public class AttendanceScan extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    public String getTitle;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("Scanning...");

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        mScannerView.stopCamera();
        if(getIntent().hasExtra("title")){
            getTitle = getIntent().getExtras().getString("title");
        }
        Intent go = new Intent(this, GetDataActivity.class);

        go.putExtra("codeID", ""+rawResult.getText());
        go.putExtra("title", ""+getTitle);
        startActivity(go);
        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }


}