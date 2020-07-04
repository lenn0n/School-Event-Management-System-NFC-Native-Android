package org.fict.ficttools.Selection.AttendanceScan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.bouncycastle.util.encoders.Hex;
import org.fict.ficttools.HelpActivity;
import org.fict.ficttools.R;
import org.fict.ficttools.Selection.Attendance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GetDataActivity extends AppCompatActivity {
    String codeID; //we will store decoded qr code here
    String title; //we will store inputted title here
    String name;
    String course;
    FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_data);


        if(getIntent().hasExtra("codeID")){
            codeID = getIntent().getExtras().getString("codeID");
        }
        if(getIntent().hasExtra("title")){
            title = getIntent().getExtras().getString("title");
            setTitle("Event: "+title);
        }
        Calendar calendar = Calendar.getInstance();
        final String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        final String currentTime = new SimpleDateFormat("hh:mm:ss a", Locale.US).format(new Date());
        final ProgressDialog progressDialog = new ProgressDialog(GetDataActivity.this);
        progressDialog.setMessage("Reading NFC Tag");
        progressDialog.setTitle("Fetching Data From "+codeID);
        progressDialog.show();


        //SHA 256 Encryption

        final int[] check = new int[1];
        final String[] id = new String[1];
        final String finalCode = codeID;
        FireInstance.collection("Users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String decodedID;
                        for (DocumentSnapshot items : queryDocumentSnapshots) {
                            try{
                                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                                byte[] hash = digest.digest(
                                        items.getString("StudentID").getBytes(StandardCharsets.UTF_8));
                                decodedID = new String(Hex.encode(hash));
                                if(finalCode.equals(decodedID)){
                                    check[0] = 1;
                                    id[0] = items.getString("StudentID");
                                    name = items.getString("Name");
                                    course = items.getString("Course");
                                }
                            }
                            catch (Exception e){
                                Log.d("Bug","Error");
                            }
                        }
                        //Check now...
                        if(check[0] == 1) {
                            codeID = id[0];
                            FireInstance.collection("Attendance")
                                    .whereEqualTo("StudentID",""+codeID)
                                    .whereEqualTo("Event", ""+title) //remove for more secure.TODO SECURE1
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if(queryDocumentSnapshots.isEmpty()){
                                                Map<String, String> addMap = new HashMap<>();
                                                addMap.put("StudentID", codeID);
                                                addMap.put("Name", name);
                                                addMap.put("Course", course);
                                                addMap.put("Event", title);
                                                addMap.put("Date", currentDate+" "+currentTime);

                                                //student is not in the list, Let's add! :3
                                                FireInstance.collection("Attendance")
                                                        .add(addMap)
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                progressDialog.dismiss();
                                                                Intent goBacks = new Intent(GetDataActivity.this, Attendance.class);
                                                                goBacks.putExtra("title", ""+title);
                                                                goBacks.putExtra("name", "[Member]: "+name+" - OK");
                                                                goBacks.putExtra("status", "OK");
                                                                goBacks.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(goBacks);
                                                            }
                                                        });
                                            }
                                            else{
                                                //Student already in the list!
                                                progressDialog.dismiss();
                                                Intent goBack = new Intent(GetDataActivity.this, Attendance.class);
                                                goBack.putExtra("title", ""+title);
                                                goBack.putExtra("name", "Student already on the list.");
                                                goBack.putExtra("status", "OK");
                                                goBack.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(goBack);
                                            }

                                        }
                                    });
                        }
                        else{
                            //student is not in the DATABASE! Generate?
                            progressDialog.dismiss();
                            Intent getHelp = new Intent(GetDataActivity.this, HelpActivity.class);
                            getHelp.putExtra("codeID", codeID);
                            getHelp.putExtra("do", title);
                            getHelp.putExtra("date", currentDate+" "+currentTime);
                            getHelp.putExtra("status", "FAILED");
                            getHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(getHelp);
                        }

                    }
                });





    }





}
