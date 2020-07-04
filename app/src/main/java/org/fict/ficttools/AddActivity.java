package org.fict.ficttools;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import org.fict.ficttools.Selection.ToolsActivity;
import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {
    Button addMemberBtn, tempFolder;
    TextView addID, addName;
    AutoCompleteTextView addCourse;
    Spinner addFee;
    String[] sections;
    FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        addMemberBtn = findViewById(R.id.addButton);
        addID = findViewById(R.id.addID);
        addName = findViewById(R.id.addName);
        addCourse = findViewById(R.id.addCourse);
        addFee = findViewById(R.id.addFee);
        tempFolder = findViewById(R.id.tempFolder);
        addFee.setSelection(1);
        sections = getResources().getStringArray(R.array.sections);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sections);
        addCourse.setAdapter(adapter);
        if (getIntent().hasExtra("sec")) {
            addCourse.setText(getIntent().getExtras().getString("sec"));
        }
        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("".equals(addName.getText().toString()) || "".equals(addCourse.getText().toString()) || "".equals(addID.getText().toString())){
                    Toast.makeText(AddActivity.this, "Empty Fields!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    final ProgressDialog progressDialog = new ProgressDialog(AddActivity.this);
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.setTitle("Checking ID Availability...");
                    progressDialog.show();
                    FireInstance.collection("Users")
                            .whereEqualTo("StudentID", ""+addID.getText().toString())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if(queryDocumentSnapshots.isEmpty()){
                                        progressDialog.setMessage("ID Available");
                                        progressDialog.setTitle("Adding To Database...");
                                        Map<String, String> addMap = new HashMap<>();
                                        addMap.put("StudentID", addID.getText().toString());
                                        addMap.put("Name",  addName.getText().toString());
                                        addMap.put("Course",  addCourse.getText().toString());
                                        addMap.put("Status",  addFee.getSelectedItem().toString());

                                        FireInstance.collection("Users")
                                                .add(addMap)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Toast.makeText(AddActivity.this, "Member added successfully!", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                        Intent go = new Intent(AddActivity.this, ToolsActivity.class);
                                                        go.putExtra("sec", addCourse.getText().toString());
                                                        startActivity(go);
                                                    }
                                                });
                                    }
                                    else{
                                        // ID already taken!!
                                        progressDialog.dismiss();
                                        Toast.makeText(AddActivity.this, "Student ID is already taken!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                }


            }
        });
    }
}
