package org.fict.ficttools;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.fict.ficttools.Selection.ToolsActivity;

import java.util.ArrayList;

public class TempActivity extends AppCompatActivity {
    RecyclerView tempRecyclerView;
    ArrayList<String> studId;
    ArrayList<String> date;
    ArrayList<String> event;
    ArrayList<String> documentID;
    TempAdapter tempAdapter;
    Button gBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        studId = new ArrayList<>();
        date = new ArrayList<>();
        event = new ArrayList<>();
        documentID = new ArrayList<>();

        gBack = findViewById(R.id.gBack);
        tempRecyclerView = findViewById(R.id.tempRecyclerView);
        tempRecyclerView.setHasFixedSize(true);
        tempRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tempRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        setTitle("Failed Participants");
        loadTemp();

        gBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(TempActivity.this, ToolsActivity.class);
                startActivity(go);
            }
        });
    }

    private void loadTemp() {
        FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
        FireInstance.collection("Temp")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        studId.clear();
                        date.clear();
                        event.clear();
                        documentID.clear();
                        tempRecyclerView.removeAllViews();
                        for (DocumentSnapshot items : queryDocumentSnapshots) {
                            studId.add(items.getString("StudentID"));
                            date.add(items.getString("Date"));
                            event.add(items.getString("Event"));
                            documentID.add(items.getId());
                        }

                        tempAdapter = new TempAdapter(TempActivity.this, studId, date, event, documentID);
                        tempRecyclerView.setAdapter(tempAdapter);
                    }
                });
    }
}
