package com.bluebirdaward.mapassistant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.bluebirdaward.mapassistant.gmmap.R;

import java.util.ArrayList;

import adapter.PetrolAdt;
import model.Petrol;

public class PetrolActivity extends AppCompatActivity
{
ListView listView;
    PetrolAdt adapter;
    ArrayList<Petrol> listPetrol;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petrol);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.lvPetrol);

        listPetrol = new ArrayList<>();
        listPetrol.add(new Petrol("Xăng RON 92-II", "16.400"));
        listPetrol.add(new Petrol("Xăng RON 95-II", "17.100"));
        listPetrol.add(new Petrol("E5 RON 92-II", "16.140"));
        listPetrol.add(new Petrol("DO 0,05S", "12.420"));
        listPetrol.add(new Petrol("Dầu hoả", "11.020"));

        adapter = new PetrolAdt(this, R.layout.row_petrol, listPetrol);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
