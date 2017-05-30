package com.delaroystudios.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class PickActivity extends ActionBarActivity {

    private Button btnReport1;
    private Button btnAdd;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick);
        mAuth = FirebaseAuth.getInstance();
        btnReport1 = (Button) findViewById(R.id.btnReport1);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        btnReport1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(PickActivity.this, ReportActivity.class);
                startActivity(a);
            }

        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(PickActivity.this, MainActivity.class);
                startActivity(a);
            }

        });


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent i = new Intent(PickActivity.this, LoginActivity.class);
            startActivity(i);
            return true;
        }

        else if (id == R.id.action_home) {
            //mAuth.signOut();
            Intent i = new Intent(PickActivity.this, MainActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
