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
import com.google.firebase.firestore.FirebaseFirestore;

import org.fict.ficttools.Selection.AttendanceScan.ManageAttendanceActivity;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    Context context;
    ArrayList<String> name;
    ArrayList<String> date;
    ArrayList<String> course;
    ArrayList<String> documentID;

    class SearchViewHolder extends RecyclerView.ViewHolder {
        Button participantsDelete;
        TextView participantsName, participantsDate, participantsCourse;

        public SearchViewHolder(View itemView) {
            super(itemView);
            participantsName = itemView.findViewById(R.id.participantsName);
            participantsDate = itemView.findViewById(R.id.participantsDate);
            participantsCourse = itemView.findViewById(R.id.participantsCourse);
            participantsDelete = itemView.findViewById(R.id.participantsDelete);
        }
    }

    public SearchAdapter(Context context, ArrayList<String> name, ArrayList<String> date, ArrayList<String> course, ArrayList<String> documentID) {
        this.context = context;
        this.name = name;
        this.date = date;
        this.course = course;
        this.documentID = documentID;
    }

    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row, parent, false);
        return new SearchAdapter.SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, final int position) {

        holder.participantsName.setText(name.get(position));
        holder.participantsDate.setText(date.get(position));
        holder.participantsCourse.setText(course.get(position));
        holder.participantsDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                final ProgressDialog progressDialog = new ProgressDialog(context);
                                progressDialog.setMessage("Deleting Participant...");
                                progressDialog.setTitle("Please Wait...");
                                progressDialog.show();
                                FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                                FireInstance.collection("Attendance").document(documentID.get(position))
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(context, "Deleted successfully.", Toast.LENGTH_LONG).show();
                                                Intent go = new Intent(context, ManageAttendanceActivity.class);
                                                v.getContext().startActivity(go);
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
                builder.setMessage("Remove "+name.get(position)+"?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();


            }
        });
    }



    @Override
    public int getItemCount() {
        return name.size();
    }
}
