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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.fict.ficttools.Selection.AttendanceScan.ManageAttendanceActivity;
import org.fict.ficttools.Selection.ToolsActivity;

import java.util.ArrayList;

public class TempAdapter extends RecyclerView.Adapter<TempAdapter.TempViewHolder> {

    Context context;
    ArrayList<String> studId;
    ArrayList<String> date;
    ArrayList<String> event;
    ArrayList<String> documentID;

    class TempViewHolder extends RecyclerView.ViewHolder {
       ImageView tempDelete;
        TextView tempStudID, tempDate, tempEvent;

        public TempViewHolder(View itemView) {
            super(itemView);
            tempDelete = itemView.findViewById(R.id.tempDelete);
            tempStudID = itemView.findViewById(R.id.tempStudID);
            tempEvent = itemView.findViewById(R.id.tempEvent);
            tempDate = itemView.findViewById(R.id.tempDate);
        }
    }

    public TempAdapter(Context context, ArrayList<String> studId, ArrayList<String> date, ArrayList<String> event, ArrayList<String> documentID) {

        this.context = context;
        this.studId = studId;
        this.date = date;
        this.event = event;
        this.documentID = documentID;
    }

    @Override
    public TempAdapter.TempViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_temp, parent, false);
        return new TempAdapter.TempViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TempAdapter.TempViewHolder holder, final int position) {

        holder.tempStudID.setText(studId.get(position));
        holder.tempDate.setText(date.get(position));
        holder.tempEvent.setText(event.get(position));
        holder.tempDelete.setOnClickListener(new View.OnClickListener() {
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
                                FireInstance.collection("Temp").document(documentID.get(position))
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(context, "Deleted successfully.", Toast.LENGTH_LONG).show();
                                                Intent go = new Intent(context, TempActivity.class);
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
                builder.setMessage("Remove "+studId.get(position)+"?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();


            }
        });
    }



    @Override
    public int getItemCount() {
        return studId.size();
    }
}
