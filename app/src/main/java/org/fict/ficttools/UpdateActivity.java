package org.fict.ficttools;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.fict.ficttools.Selection.AttendanceScan.GetDataActivity;
import org.fict.ficttools.Selection.ToolsActivity;

import java.util.HashMap;
import java.util.Map;

public class UpdateActivity extends AppCompatActivity {
    TextView nameTB, courseTB;
    Spinner feeSpinner;
    Button memUpdateBTN;
    String name, course, status, documentID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        nameTB = findViewById(R.id.nameTB);
        courseTB = findViewById(R.id.courseTB);
        memUpdateBTN = findViewById(R.id.updateMemBtn);
        feeSpinner = findViewById(R.id.feeSpinner);
        if(getIntent().hasExtra("name")){
            name = getIntent().getExtras().getString("name");
            course = getIntent().getExtras().getString("course");
            status = getIntent().getExtras().getString("status");
            documentID = getIntent().getExtras().getString("documentID");


            setTitle(name);
            nameTB.setText(name);
            courseTB.setText(course);
            if("Membership Fee: PAID".equals(status)){
                feeSpinner.setSelection(0);
            }
            else{
                feeSpinner.setSelection(1);
            }

            memUpdateBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ProgressDialog progressDialog = new ProgressDialog(UpdateActivity.this);
                    progressDialog.setMessage("Working.. Please Wait...");
                    progressDialog.setTitle("Updating...");
                    progressDialog.show();
                    FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                    DocumentReference contact = FireInstance.collection("Users").document(documentID);
                    contact.update("Name", nameTB.getText().toString());
                    contact.update("Course", courseTB.getText().toString());
                    contact.update("Status", feeSpinner.getSelectedItem().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(UpdateActivity.this, "Updated Successfully!", Toast.LENGTH_LONG).show();
                                    Intent go = new Intent(UpdateActivity.this, ToolsActivity.class);
                                    startActivity(go);
                                }
                            });

                }
            });



        }
    }
}
