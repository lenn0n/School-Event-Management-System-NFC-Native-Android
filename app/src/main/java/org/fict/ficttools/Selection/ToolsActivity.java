package org.fict.ficttools.Selection;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.bouncycastle.util.encoders.Hex;
import org.fict.ficttools.AddActivity;
import org.fict.ficttools.NavigationActivity;
import org.fict.ficttools.R;
import org.fict.ficttools.SectionAdapter;
import org.fict.ficttools.StudentObject;
import org.fict.ficttools.TempActivity;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ToolsActivity extends AppCompatActivity {
    Button addMember, tempFolder, dlButton;
    EditText searchbar;
    ImageView goback;
    Spinner sectionList;
    RecyclerView sectionRecyclerView;
    ArrayList<String> name;
    ArrayList<String> status;
    ArrayList<String> course;
    ArrayList<String> documentID;
    SectionAdapter sectionAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);
        setTitle("Manage Members");

        addMember = findViewById(R.id.addMember);

        tempFolder = findViewById(R.id.tempFolder);
        searchbar = findViewById(R.id.searchbar);
        goback = findViewById(R.id.gobackImage);
        dlButton = findViewById(R.id.dlbutton);
        name = new ArrayList<>();
        status = new ArrayList<>();
        course = new ArrayList<>();
        documentID = new ArrayList<>();
        sectionList = findViewById(R.id.sectionList);
        sectionRecyclerView = findViewById(R.id.sectionRecyclerView);
        sectionRecyclerView.setHasFixedSize(true);
        sectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sectionRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipelayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh,R.color.refresh1,R.color.refresh2);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                finish();
                startActivity(getIntent());
            }

        });
                //load sections
                loadSections();
        //action here
         tempFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(ToolsActivity.this, TempActivity.class);
                startActivity(go);
            }
        });
        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(ToolsActivity.this, AddActivity.class);
                if (getIntent().hasExtra("sec")) {
                    go.putExtra("sec", getIntent().getExtras().getString("sec"));
                }
                startActivity(go);
            }
        });

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(ToolsActivity.this, NavigationActivity.class);
                startActivity(go);
            }
        });
        dlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePDF();
            }
        });
        searchbar.addTextChangedListener(new TextWatcher() {
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
                                                     status.clear();
                                                     course.clear();
                                                     documentID.clear();
                                                     sectionRecyclerView.removeAllViews();
                                                     searchName(s.toString());
                                                 }
                                                 else{
                                                     name.clear();
                                                     status.clear();
                                                     course.clear();
                                                     documentID.clear();
                                                     sectionRecyclerView.removeAllViews();

                                                     FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                                                     FireInstance.collection("Users")
                                                             .whereEqualTo("Course", sectionList.getSelectedItem().toString())
                                                             .get()
                                                             .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                 @Override
                                                                 public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                     name.clear();
                                                                     status.clear();
                                                                     course.clear();
                                                                     documentID.clear();
                                                                     sectionRecyclerView.removeAllViews();
                                                                     for (DocumentSnapshot items : queryDocumentSnapshots) {
                                                                         name.add(items.getString("Name"));
                                                                         status.add("Membership Fee: " + items.getString("Status"));
                                                                         course.add(items.getString("Course"));
                                                                         documentID.add(items.getId());
                                                                     }

                                                                     sectionAdapter = new SectionAdapter(ToolsActivity.this, name, status, course, documentID);
                                                                     sectionRecyclerView.setAdapter(sectionAdapter);
                                                                 }
                                                             });
                                                 }
                                             }
                                         });
                sectionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
                        FireInstance.collection("Users")
                                .whereEqualTo("Course", sectionList.getSelectedItem().toString())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        name.clear();
                                        status.clear();
                                        course.clear();
                                        documentID.clear();
                                        sectionRecyclerView.removeAllViews();
                                        for (DocumentSnapshot items : queryDocumentSnapshots) {
                                            name.add(items.getString("Name"));
                                            status.add("Membership Fee: " + items.getString("Status"));
                                            course.add(items.getString("Course"));
                                            documentID.add(items.getId());
                                        }

                                        sectionAdapter = new SectionAdapter(ToolsActivity.this, name, status, course, documentID);
                                        sectionRecyclerView.setAdapter(sectionAdapter);
                                    }
                                });
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });



    }
    private void searchName(final String searchedString) {

        FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
        FireInstance.collection("Users")
                .whereEqualTo("Course",sectionList.getSelectedItem().toString())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        name.clear();
                        status.clear();
                        course.clear();
                        documentID.clear();
                        sectionRecyclerView.removeAllViews();
                        for(DocumentSnapshot items : queryDocumentSnapshots){
                            String fullName = items.getString("Name");
                            String fullCourse = items.getString("Course");
                            String fullStatus = "Membership Fee: "+items.getString("Status");

                            if(fullName.toLowerCase().contains(searchedString.toLowerCase())){
                                name.add(fullName);
                                course.add(fullCourse);
                                status.add(fullStatus);
                                documentID.add(items.getId());
                            }

                        }
                        sectionAdapter = new SectionAdapter(ToolsActivity.this, name, status, course, documentID);
                        sectionRecyclerView.setAdapter(sectionAdapter);
                    }
                });

    }
    private void loadSections() {

        final ArrayList<String> sectionListArray;
        FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
        sectionListArray = new ArrayList<>();
        FireInstance.collection("Users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){

                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                if(sectionListArray.contains(document.getString("Course"))){
                                    //Skip. Already in spinner.
                                }
                                else{
                                    sectionListArray.add(document.getString("Course"));
                                }
                            }
                            Collections.sort(sectionListArray, String.CASE_INSENSITIVE_ORDER);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(ToolsActivity.this, android.R.layout.simple_spinner_item, sectionListArray);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sectionList.setAdapter(adapter);
                        }
                        //else queryDocumentSnapshots.isEmpty()
                    }
                    //onFailure
                });
    }

    public static String encode(String codeID){
        String encoded = "";
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    codeID.getBytes(StandardCharsets.UTF_8));
            encoded = new String(Hex.encode(hash));

        }
        catch (Exception e){
            Log.d("Bug","Error");
        }
        return encoded;
    }

    private void savePDF() {
        final Document mDoc = new Document();
        final String mFilePath = Environment.getExternalStorageDirectory() + "/StudentMasterList.pdf";
        try {
            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            mDoc.open();
            FirebaseFirestore FireInstance = FirebaseFirestore.getInstance();
            FireInstance.collection("Users")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                try {
                                    mDoc.add(new Paragraph("FICT MASTERLIST FOR NFC STICKER ID"));
                                    mDoc.add(new Paragraph(" "));
                                    mDoc.add(new LineSeparator());

                                    PdfPTable cell = new PdfPTable(2);
                                    PdfPCell name = new PdfPCell(new Paragraph("NAME AND ID"));
                                    PdfPCell course = new PdfPCell(new Paragraph("COURSE, YEAR AND SECTION"));
                                    name.setHorizontalAlignment(Element.ALIGN_MIDDLE);
                                    name.setBackgroundColor(BaseColor.ORANGE);
                                    course.setBackgroundColor(BaseColor.ORANGE);
                                    cell.addCell(name);
                                    cell.addCell(course);
                                    cell.setWidthPercentage(100);
                                    mDoc.add(cell);


                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }
                                PdfPTable myTable = new PdfPTable(2);
                                myTable.setWidthPercentage(100);

                                int maxlength = 0;
                                List<StudentObject>studentObjects = new ArrayList<>();
                                for (DocumentSnapshot items : queryDocumentSnapshots) {
                                        studentObjects.add(new StudentObject(
                                                items.getString("Name")
                                                ,items.getString("Course")
                                                ,items.getString("StudentID")
                                                ,maxlength));
                                        maxlength +=1;
                                    }
                                Collections.sort(studentObjects, StudentObject.bySection);
                                for (int start=0;start<maxlength;start++){
                                    myTable.addCell("   "+studentObjects.get(start).getName()+"\n   "+studentObjects.get(start).getCode());
                                    myTable.addCell("   "+studentObjects.get(start).getCourse());
                                }

                                try {
                                    mDoc.add(myTable);
                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }
                                mDoc.close();
                                Toast.makeText(getApplicationContext(), "Saved To: " + mFilePath, Toast.LENGTH_SHORT).show();

                            }

                    });

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}
