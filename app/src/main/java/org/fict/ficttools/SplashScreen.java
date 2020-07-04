package org.fict.ficttools;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.fict.ficttools.Selection.ToolsActivity;

import java.util.Arrays;
import java.util.List;

public class SplashScreen extends AppCompatActivity {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    private final static int RC_SIGN_IN = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: COMPARE FirebaseAuth.getInstance().getCurrentUser().getEmail() == Admin/documentID/Email
        //TODO: THEN create admin list generator.

      if (auth.getCurrentUser() != null) {
           Intent go = new Intent(SplashScreen.this, NavigationActivity.class);
           go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
           startActivity(go);
          } else {
           List<AuthUI.IdpConfig> providers = Arrays.asList(
                   new AuthUI.IdpConfig.EmailBuilder().build(),
                   new AuthUI.IdpConfig.FacebookBuilder().build());

           startActivityForResult(AuthUI.getInstance()
                 .createSignInIntentBuilder()
                 .setAvailableProviders(providers)
                 .setTheme(R.style.SplashTheme)
                 .build(), RC_SIGN_IN);
          }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in

                FirebaseFirestore s = FirebaseFirestore.getInstance();
                s.collection("Admin")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    boolean isAdminMatch = false;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if (auth.getCurrentUser().getEmail().equals(document.get("Email").toString())){
                                            isAdminMatch = true;
                                        }
                                    }

                                    if (isAdminMatch){
                                        Toast.makeText(SplashScreen.this, "Logged As "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                                        Intent go = new Intent(SplashScreen.this, NavigationActivity.class);
                                        go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(go);
                                    }
                                    else{
                                        Toast.makeText(SplashScreen.this, "YOU ARE NOT AUTHORIZED TO ACCESS THIS APP!", Toast.LENGTH_LONG).show();
                                        AuthUI.getInstance()
                                                .signOut(SplashScreen.this)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Intent go = new Intent(SplashScreen.this, SplashScreen.class);
                                                        go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(go);
                                                    }
                                                });
                                    }

                                }

                            }
                        });


                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Toast.makeText(this, ""+response.getError().getErrorCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
