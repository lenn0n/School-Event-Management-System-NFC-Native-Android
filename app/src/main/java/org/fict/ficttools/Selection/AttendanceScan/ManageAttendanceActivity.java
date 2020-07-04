package org.fict.ficttools.Selection.AttendanceScan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.fict.ficttools.NavigationActivity;
import org.fict.ficttools.R;
import org.fict.ficttools.SearchAdapter;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ManageAttendanceActivity extends AppCompatActivity {
    EditText search;
    Spinner event;
    ImageView goHome;
    RecyclerView eventRecyclerView;
    ArrayList<String> name;
    ArrayList<String> date;
    ArrayList<String> course;
    ArrayList<String> documentID;
    SearchAdapter searchAdapter;
    CardView dlLink;
    public static final int STORAGE_CODE = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_attendance);
        setTitle("View Participants");
        loadEvents();

        search = findViewById(R.id.search_name);
        event = findViewById(R.id.eventList);
        dlLink = findViewById(R.id.dlLink);
        goHome = findViewById(R.id.btnHome);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        eventRecyclerView.setHasFixedSize(true);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        name = new ArrayList<>();
        date = new ArrayList<>();
        course = new ArrayList<>();
        documentID = new ArrayList<>();

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipelayout2);
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh,R.color.refresh1,R.color.refresh2);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                finish();
                startActivity(getIntent());
            }

        });

        event.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                search.setText("");
                FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                FireInstance.collection("Attendance")
                        .whereEqualTo("Event",event.getSelectedItem().toString())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                name.clear();
                                date.clear();
                                course.clear();
                                documentID.clear();
                                eventRecyclerView.removeAllViews();
                                for (DocumentSnapshot items: queryDocumentSnapshots){
                                   name.add(items.getString("Name"));
                                   date.add(items.getString("Date"));
                                   course.add(items.getString("Course"));
                                   documentID.add(items.getId());
                                }
                                searchAdapter = new SearchAdapter(ManageAttendanceActivity.this, name, date, course, documentID);
                                eventRecyclerView.setAdapter(searchAdapter);
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(ManageAttendanceActivity.this, NavigationActivity.class);
                go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(go);
            }
        });

        dlLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions, STORAGE_CODE);

                    }
                    else{
                    savePDF();    
                    }
                }
                else{
                    savePDF();
                }
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()){
                    name.clear();
                    date.clear();
                    course.clear();
                    documentID.clear();
                    eventRecyclerView.removeAllViews();
                    searchName(s.toString());
                }
                else{
                    name.clear();
                    date.clear();
                    course.clear();
                    documentID.clear();
                    eventRecyclerView.removeAllViews();
                    eventRecyclerView.removeAllViewsInLayout();
                    FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                    FireInstance.collection("Attendance")
                            .whereEqualTo("Event",event.getSelectedItem().toString())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    name.clear();
                                    date.clear();
                                    course.clear();
                                    documentID.clear();
                                    eventRecyclerView.removeAllViews();
                                    for (DocumentSnapshot items: queryDocumentSnapshots){
                                        name.add(items.getString("Name"));
                                        date.add(items.getString("Date"));
                                        course.add(items.getString("Course"));
                                        documentID.add(items.getId());
                                    }
                                    searchAdapter = new SearchAdapter(ManageAttendanceActivity.this, name, date, course, documentID);
                                    eventRecyclerView.setAdapter(searchAdapter);
                                }
                            });
                }
            }
        });


    }

    private void savePDF() {
        final Document mDoc = new Document();
        Calendar calendar = Calendar.getInstance();
        final String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        final String currentTime = new SimpleDateFormat(" hh:mm:ss a", Locale.US).format(new Date());
        final String heading = currentDate + currentTime;
        final String mFilePath = Environment.getExternalStorageDirectory() + "/" + event.getSelectedItem() + ".pdf";

        try {
            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            mDoc.open();
            FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
            FireInstance.collection("Attendance")
                    .whereEqualTo("Event",event.getSelectedItem().toString())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (search.getText().toString().equals("")){
                                try {
                                    String datetime = "";
                                    int bilang = 0;
                                    mDoc.add(new Paragraph("Event Attendance: "+event.getSelectedItem().toString()));
                                    for (DocumentSnapshot items : queryDocumentSnapshots) {
                                        datetime = items.getString("Date");
                                        bilang = bilang + 1;
                                    }
                                    mDoc.add(new Paragraph("Date & Time Started: "+datetime));
                                    mDoc.add(new Paragraph("File Created: "+heading));
                                    mDoc.add(new Paragraph(bilang+" out of 693 Members."));
                                    mDoc.add(new Paragraph(" "));
                                    mDoc.add(new LineSeparator());
                                    PdfPTable cell = new PdfPTable(2);
                                    PdfPCell name = new PdfPCell(new Paragraph("NAME"));
                                    PdfPCell course = new PdfPCell(new Paragraph("COURSE, YEAR AND SECTION"));

                                    name.setBackgroundColor(BaseColor.ORANGE);
                                    course.setBackgroundColor(BaseColor.ORANGE);
                                    cell.addCell(name);
                                    cell.addCell(course);
                                    cell.setWidthPercentage(100);
                                    mDoc.add(cell);

                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }
                                //start of experimental

                                PdfPTable actA = new PdfPTable(2);
                                PdfPTable actB = new PdfPTable(2);
                                PdfPTable cs1A = new PdfPTable(2);
                                PdfPTable cs1B = new PdfPTable(2);
                                PdfPTable cs1C = new PdfPTable(2);
                                PdfPTable cs1D = new PdfPTable(2);
                                PdfPTable cs1E = new PdfPTable(2);
                                PdfPTable cs1F = new PdfPTable(2);
                                PdfPTable it1A = new PdfPTable(2);
                                PdfPTable it1B = new PdfPTable(2);
                                PdfPTable it1C = new PdfPTable(2);
                                PdfPTable it1D = new PdfPTable(2);
                                PdfPTable it1E = new PdfPTable(2);
                                PdfPTable it1F = new PdfPTable(2);
                                PdfPTable it2A = new PdfPTable(2);
                                PdfPTable it2B = new PdfPTable(2);
                                PdfPTable it3A = new PdfPTable(2);
                                PdfPTable it3B = new PdfPTable(2);
                                PdfPTable it4A = new PdfPTable(2);
                                PdfPTable it4B = new PdfPTable(2);
                                PdfPTable it4C = new PdfPTable(2);
                                PdfPTable it4D = new PdfPTable(2);
                                PdfPTable cs4A = new PdfPTable(2);
                                PdfPTable cs4B = new PdfPTable(2);


                                actA.setWidthPercentage(100);
                                actB.setWidthPercentage(100);
                                it1A.setWidthPercentage(100);
                                it1B.setWidthPercentage(100);
                                it1C.setWidthPercentage(100);
                                it1D.setWidthPercentage(100);
                                it1E.setWidthPercentage(100);
                                it1F.setWidthPercentage(100);
                                cs1A.setWidthPercentage(100);
                                cs1B.setWidthPercentage(100);
                                cs1C.setWidthPercentage(100);
                                cs1D.setWidthPercentage(100);
                                cs1E.setWidthPercentage(100);
                                cs1F.setWidthPercentage(100);
                                it2A.setWidthPercentage(100);
                                it2B.setWidthPercentage(100);
                                it3A.setWidthPercentage(100);
                                it3B.setWidthPercentage(100);
                                it4A.setWidthPercentage(100);
                                it4B.setWidthPercentage(100);
                                it4C.setWidthPercentage(100);
                                it4D.setWidthPercentage(100);
                                cs4A.setWidthPercentage(100);
                                cs4B.setWidthPercentage(100);

                                for (DocumentSnapshot items : queryDocumentSnapshots) {

                                    if ("ACT - 2nd Year - A".equals(items.getString("Course"))){
                                        actA.addCell(" * "+items.getString("Name"));
                                        actA.addCell(items.getString("Course"));
                                    }
                                    if ("ACT - 2nd Year - B".equals(items.getString("Course"))){
                                        actB.addCell(" * "+items.getString("Name"));
                                        actB.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 1st Year - A".equals(items.getString("Course"))){
                                        it1A.addCell(" * "+items.getString("Name"));
                                        it1A.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 1st Year - B".equals(items.getString("Course"))){
                                        it1B.addCell(" * "+items.getString("Name"));
                                        it1B.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 1st Year - C".equals(items.getString("Course"))){
                                        it1C.addCell(" * "+items.getString("Name"));
                                        it1C.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 1st Year - D".equals(items.getString("Course"))){
                                        it1D.addCell(" * "+items.getString("Name"));
                                        it1D.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 1st Year - E".equals(items.getString("Course"))){
                                        it1E.addCell(" * "+items.getString("Name"));
                                        it1E.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 1st Year - F".equals(items.getString("Course"))){
                                        it1F.addCell(" * "+items.getString("Name"));
                                        it1F.addCell(items.getString("Course"));
                                    }
                                    if ("BS ComSci - 1st Year - A".equals(items.getString("Course"))){
                                        cs1A.addCell(" * "+items.getString("Name"));
                                        cs1A.addCell(items.getString("Course"));
                                    }

                                    if ("BS ComSci - 1st Year - B".equals(items.getString("Course"))){
                                        cs1B.addCell(" * "+items.getString("Name"));
                                        cs1B.addCell(items.getString("Course"));
                                    }

                                    if ("BS ComSci - 1st Year - C".equals(items.getString("Course"))){
                                        cs1C.addCell(" * "+items.getString("Name"));
                                        cs1C.addCell(items.getString("Course"));
                                    }

                                    if ("BS ComSci - 1st Year - D".equals(items.getString("Course"))){
                                        cs1D.addCell(" * "+items.getString("Name"));
                                        cs1D.addCell(items.getString("Course"));
                                    }
                                    if ("BS ComSci - 1st Year - E".equals(items.getString("Course"))){
                                        cs1E.addCell(" * "+items.getString("Name"));
                                        cs1E.addCell(items.getString("Course"));
                                    }
                                    if ("BS ComSci - 1st Year - F".equals(items.getString("Course"))){
                                        cs1F.addCell(" * "+items.getString("Name"));
                                        cs1F.addCell(items.getString("Course"));
                                    }

                                    if ("BS InfoTech - 2nd Year - A".equals(items.getString("Course"))){
                                        it2A.addCell(" * "+items.getString("Name"));
                                        it2A.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 2nd Year - B".equals(items.getString("Course"))){
                                        it2B.addCell(" * "+items.getString("Name"));
                                        it2B.addCell(items.getString("Course"));
                                    }

                                    if ("BS InfoTech - 3rd Year - A".equals(items.getString("Course"))){
                                        it3A.addCell(" * "+items.getString("Name"));
                                        it3A.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 3rd Year - B".equals(items.getString("Course"))){
                                        it3B.addCell(" * "+items.getString("Name"));
                                        it3B.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 4th Year - A".equals(items.getString("Course"))){
                                        it4A.addCell(" * "+items.getString("Name"));
                                        it4A.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 4th Year - B".equals(items.getString("Course"))){
                                        it4B.addCell(" * "+items.getString("Name"));
                                        it4B.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 4th Year - C".equals(items.getString("Course"))){
                                        it4C.addCell(" * "+items.getString("Name"));
                                        it4C.addCell(items.getString("Course"));
                                    }
                                    if ("BS InfoTech - 4th Year - D".equals(items.getString("Course"))){
                                        it4D.addCell(" * "+items.getString("Name"));
                                        it4D.addCell(items.getString("Course"));
                                    }
                                    if ("BS ComSci - 4th Year - A".equals(items.getString("Course"))){
                                        cs4A.addCell(" * "+items.getString("Name"));
                                        cs4A.addCell(items.getString("Course"));
                                    }
                                    if ("BS ComSci - 4th Year - B".equals(items.getString("Course"))){
                                        cs4B.addCell(" * "+items.getString("Name"));
                                        cs4B.addCell(items.getString("Course"));
                                    }

                                }

                                try {
                                    mDoc.add(actA);
                                    mDoc.add(actB);
                                    mDoc.add(it1A);
                                    mDoc.add(it1B);
                                    mDoc.add(it1C);
                                    mDoc.add(it1D);
                                    mDoc.add(it1E);
                                    mDoc.add(it1F);
                                    mDoc.add(cs1A);
                                    mDoc.add(cs1B);
                                    mDoc.add(cs1C);
                                    mDoc.add(cs1D);
                                    mDoc.add(cs1E);
                                    mDoc.add(cs1F);
                                    mDoc.add(it2A);
                                    mDoc.add(it2B);
                                    mDoc.add(it3A);
                                    mDoc.add(it3B);
                                    mDoc.add(it4A);
                                    mDoc.add(it4B);
                                    mDoc.add(it4C);
                                    mDoc.add(it4D);
                                    mDoc.add(cs4A);
                                    mDoc.add(cs4B);

                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }
                                mDoc.close();
                                Toast.makeText(getApplicationContext(), "Saved To: " + mFilePath, Toast.LENGTH_SHORT).show();



                            }
                            else{
                                try {

                                    String datetime = "";
                                    mDoc.add(new Paragraph("Event Attendance: "+event.getSelectedItem().toString()));
                                    for (DocumentSnapshot items : queryDocumentSnapshots) {
                                        datetime = items.getString("Date");
                                    }
                                    mDoc.add(new Paragraph("Date & Time Started: "+datetime));
                                    mDoc.add(new Paragraph("File Created: "+heading));
                                    mDoc.add(new Paragraph(" "));
                                    mDoc.add(new LineSeparator());
                                    PdfPTable cell = new PdfPTable(2);
                                    PdfPCell name = new PdfPCell(new Paragraph("NAME"));
                                    PdfPCell course = new PdfPCell(new Paragraph("COURSE, YEAR AND SECTION"));

                                    name.setBackgroundColor(BaseColor.ORANGE);
                                    course.setBackgroundColor(BaseColor.ORANGE);
                                    cell.addCell(name);
                                    cell.addCell(course);
                                    cell.setWidthPercentage(100);
                                    mDoc.add(cell);

                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }
                                int count = 0;
                                for (DocumentSnapshot items : queryDocumentSnapshots) {
                                    if(items.getString("Name").toLowerCase().contains(search.getText().toString().toLowerCase())){
                                        count+=1;
                                        try {
                                            PdfPTable cell = new PdfPTable(2);
                                            cell.setWidthPercentage(100);
                                            cell.addCell(count+". "+items.getString("Name"));
                                            cell.addCell(items.getString("Course"));
                                            mDoc.add(cell);


                                        } catch (DocumentException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else if (items.getString("Course").toLowerCase().contains(search.getText().toString().toLowerCase())){
                                        count+=1;
                                        try {
                                            PdfPTable cell = new PdfPTable(2);
                                            cell.setWidthPercentage(100);
                                            cell.addCell(count+". "+items.getString("Name"));
                                            cell.addCell(items.getString("Course"));
                                            mDoc.add(cell);


                                        } catch (DocumentException e) {
                                            e.printStackTrace();
                                        }
                                    }



                                }
                                mDoc.close();
                                Toast.makeText(getApplicationContext(), "Saved To: " + mFilePath, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

        }
        catch (Exception e){

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    savePDF();
                }
                else{
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void searchName(final String searchedString) {

        FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
        FireInstance.collection("Attendance")
                .whereEqualTo("Event",event.getSelectedItem().toString())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        name.clear();
                        date.clear();
                        course.clear();
                        documentID.clear();
                        eventRecyclerView.removeAllViews();
                    for(DocumentSnapshot items : queryDocumentSnapshots){
                            String fullName = items.getString("Name");
                            String fullCourse = items.getString("Course");
                            String fullDate = items.getString("Date");

                            if(fullName.toLowerCase().contains(searchedString.toLowerCase())){
                                name.add(fullName);
                                course.add(fullCourse);
                                date.add(fullDate);
                                documentID.add(items.getId());
                            }
                            else if (fullCourse.toLowerCase().contains(searchedString.toLowerCase())){
                                name.add(fullName);
                                course.add(fullCourse);
                                date.add(fullDate);
                                documentID.add(items.getId());
                            }

                    }
                        searchAdapter = new SearchAdapter(ManageAttendanceActivity.this, name, date, course, documentID);
                        eventRecyclerView.setAdapter(searchAdapter);
                    }
                });

    }
    private void loadEvents() {
        final ArrayList<String> eventListArray;
        FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
        eventListArray = new ArrayList<>();
        FireInstance.collection("Attendance")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){

                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                if(eventListArray.contains(document.getString("Event"))){
                                //Skip. Already in spinner.
                                }
                                else{
                                    eventListArray.add(document.getString("Event"));
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(ManageAttendanceActivity.this, android.R.layout.simple_spinner_item, eventListArray);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            event.setAdapter(adapter);
                        }
                        //else queryDocumentSnapshots.isEmpty()
                    }
                    //onFailure
                });
    }
}
