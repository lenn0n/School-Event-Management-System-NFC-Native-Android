package org.fict.ficttools;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.fict.ficttools.Selection.AttendanceScan.GetDataActivity;
import org.fict.ficttools.Selection.AttendanceScan.ManageAttendanceActivity;
import org.fict.ficttools.Selection.ToolsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {

    Context context;
    ArrayList<String> name;
    ArrayList<String> status;
    ArrayList<String> course;
    ArrayList<String> documentID;

    class SectionViewHolder extends RecyclerView.ViewHolder {
        Button memDelete, memEdit;
        TextView memName, memCourse, memStatus;

        public SectionViewHolder(View itemView) {
            super(itemView);
            memName = itemView.findViewById(R.id.memName);
            memCourse = itemView.findViewById(R.id.memCourse);
            memStatus = itemView.findViewById(R.id.memStatus);
            memDelete = itemView.findViewById(R.id.memDelete);
            memEdit = itemView.findViewById(R.id.memEdit);
        }
    }

    public SectionAdapter(Context context, ArrayList<String> name, ArrayList<String> status, ArrayList<String> course, ArrayList<String> documentID) {
        this.context = context;
        this.name = name;
        this.status = status;
        this.course = course;
        this.documentID = documentID;
    }

    @Override
    public SectionAdapter.SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_panel, parent, false);
        return new SectionAdapter.SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SectionViewHolder holder, final int position) {

        holder.memName.setText(name.get(position));
        holder.memStatus.setText(status.get(position));
        holder.memCourse.setText(course.get(position));

        holder.memEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(context, UpdateActivity.class);
                go.putExtra("name", name.get(position));
                go.putExtra("course", course.get(position));
                go.putExtra("status", status.get(position));
                go.putExtra("documentID", documentID.get(position));
                v.getContext().startActivity(go);
            }
        });

        holder.memDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                final ProgressDialog progressDialog = new ProgressDialog(context);
                                progressDialog.setMessage("Deleting Member...");
                                progressDialog.setTitle("Please Wait...");
                                progressDialog.show();
                                final FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                                Map<String, String> addMap = new HashMap<>();
                                addMap.put("Name", name.get(position));
                                addMap.put("Course", course.get(position));
                                addMap.put("Status", status.get(position));
                                FireInstance.collection("DeletedMembers")
                                        .add(addMap)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                FireInstance.collection("Users").document(documentID.get(position))
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(context, "Deleted successfully.", Toast.LENGTH_LONG).show();
                                                                Intent go = new Intent(context, ToolsActivity.class);
                                                                v.getContext().startActivity(go);

                                                            }
                                                        });
                                            }
                                        });

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Confirm remove "+name.get(position)+"?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
        });

    }


    @Override
    public int getItemCount() {
        return name.size();
    }
}
