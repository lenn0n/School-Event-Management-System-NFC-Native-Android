package org.fict.ficttools;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.fict.ficttools.Selection.Attendance;
import org.fict.ficttools.Selection.Members;
import org.fict.ficttools.Selection.ToolsActivity;

public class ChooseActivity extends AppCompatActivity {
    ImageView img_membership, img_events, img_tools;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        setTitle("Welcome Officer!");
        img_membership = findViewById(R.id.image_membership);
        img_events = findViewById(R.id.image_events);
        img_tools = findViewById(R.id.image_tools);

        img_events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(ChooseActivity.this, Attendance.class);
                startActivity(go);
            }
        });

        img_membership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(ChooseActivity.this, Members.class);
                startActivity(go);

            }
        });

        img_tools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(ChooseActivity.this, ToolsActivity.class);
                startActivity(go);
            }
        });

    }


}
