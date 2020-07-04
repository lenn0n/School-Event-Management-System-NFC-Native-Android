package org.fict.ficttools;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.fict.ficttools.Selection.Attendance;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HelpActivity extends AppCompatActivity {
    public String codeID;
    public String action;
    public String status;
    public String date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Button proceed = findViewById(R.id.proceedBTN);
        Button getBack = findViewById(R.id.goBackBTN);
        TextView tv = findViewById(R.id.txtID);
        final FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();

        if(getIntent().hasExtra("status")){
            status = getIntent().getExtras().getString("status");
        }
            if("FAILED".equals(status)){
                MediaPlayer errorSound = MediaPlayer.create(this, R.raw.errorsound);
                errorSound.start();
        }
        setTitle("Error: Student not Found.");
        if(getIntent().hasExtra("codeID")){
            codeID = getIntent().getExtras().getString("codeID");
        }

        tv.setText(codeID);

        if(getIntent().hasExtra("do")){
            action = getIntent().getExtras().getString("do");
        }
        if(getIntent().hasExtra("date")){
            date = getIntent().getExtras().getString("date");
        }

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(HelpActivity.this);
                progressDialog.setMessage("Processing Data...");
                progressDialog.setTitle("Saving "+codeID);
                progressDialog.show();

                FireInstance.collection("Temp")
                        .whereEqualTo("StudentID",""+codeID)
                        .whereEqualTo("Event", ""+action) //remove for more secure.TODO SECURE2
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if(queryDocumentSnapshots.isEmpty()){
                                    Map<String, String> addMap = new HashMap<>();
                                    addMap.put("StudentID", codeID);
                                    addMap.put("Event", action);
                                    addMap.put("Date", date);

                                    FireInstance.collection("Temp")
                                            .add(addMap)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressDialog.dismiss();
                                                    Intent go = new Intent(HelpActivity.this, Attendance.class);
                                                    go.putExtra("title", action);
                                                    go.putExtra("name", "[StudentID]: "+codeID+" was sent to database.");
                                                    go.putExtra("status", "OK");
                                                    go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(go);
                                                }
                                            });
                                }
                                else{
                                    progressDialog.dismiss();
                                    Intent go = new Intent(HelpActivity.this, Attendance.class);
                                    go.putExtra("title", action);
                                    go.putExtra("name", "[StudentID]: "+codeID+" was sent to database.");
                                    go.putExtra("status", "OK");
                                    go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(go);
                                }
                            }
                        });


            }
        });

        getBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(HelpActivity.this, Attendance.class);
                go.putExtra("title", action);
                go.putExtra("name", "Status will be updated here.");
                go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(go);
            }
        });

    }
}
