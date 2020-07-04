package org.fict.ficttools.Selection.MembersScan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.bouncycastle.util.encoders.Hex;
import org.fict.ficttools.R;
import org.fict.ficttools.Selection.Members;
import org.fict.ficttools.Selection.ToolsActivity;
import org.fict.ficttools.UpdateActivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CheckFee extends AppCompatActivity {
    String codeID;
    String documentID;
    String action;
    String name;
    String course;
    String status;
    FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_fee);

        if(getIntent().hasExtra("codeID")){
            codeID = getIntent().getExtras().getString("codeID");
        }

        if(getIntent().hasExtra("action")){
            action = getIntent().getExtras().getString("action");
        }

        final ProgressDialog progressDialog = new ProgressDialog(CheckFee.this);
        progressDialog.setMessage("Reading NFC..");
        progressDialog.setTitle("Fetching Data From "+codeID);
        progressDialog.show();


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
                                    status = items.getString("Status");
                                    documentID = items.getId();
                                }
                            }
                            catch (Exception e){
                                Log.d("Bug","Error");
                            }
                        }
                        //Check now...
                        if (action.equals(getString(R.string.chooseAction1))){
                            if(check[0] == 1){
                                progressDialog.dismiss();
                                codeID = id[0];
                                Intent getHelp = new Intent(CheckFee.this, Members.class);
                                getHelp.putExtra("Name", name);
                                getHelp.putExtra("Course", course);
                                getHelp.putExtra("Status", status);
                                getHelp.putExtra("Found", "YES");
                                getHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(getHelp);
                            }
                            else{
                                progressDialog.dismiss();
                                Intent get = new Intent(CheckFee.this, Members.class);
                                get.putExtra("Found", "NO");
                                get.putExtra("codeID", codeID);
                                get.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(get);
                            }

                        }
                        //TODO: UPDATE USER
                        if (action.equals(getString(R.string.chooseAction2)) || action.equals(getString(R.string.chooseAction3))){
                            if(check[0] == 1){

                                //TODO: UPDATE STATUS
                                final String updateStatus;
                                if (action.equals(getString(R.string.chooseAction2))){
                                    updateStatus = getString(R.string.updatetoPaid);
                                }
                                else{ updateStatus = getString(R.string.updatetoNotPaid); }

                                progressDialog.setMessage("Working.. Please Wait...");
                                progressDialog.setTitle("Updating...");
                                progressDialog.show();
                                FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                                DocumentReference contact = FireInstance.collection("Users").document(documentID);
                                contact.update("Status", updateStatus)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                codeID = id[0];
                                                Intent getHelp = new Intent(CheckFee.this, Members.class);
                                                getHelp.putExtra("Name", name);
                                                getHelp.putExtra("Course", course);
                                                getHelp.putExtra("Status", updateStatus);
                                                getHelp.putExtra("Found", "YES");
                                                getHelp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(getHelp);
                                            }
                                        });



                            }
                            else{
                                progressDialog.dismiss();
                                Intent get = new Intent(CheckFee.this, Members.class);
                                get.putExtra("Found", "NO");
                                get.putExtra("codeID", codeID);
                                get.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(get);
                            }
                        }


                    }
                });


    }
}
