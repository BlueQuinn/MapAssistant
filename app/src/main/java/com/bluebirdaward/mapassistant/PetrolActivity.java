package com.bluebirdaward.mapassistant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bluebirdaward.mapassistant.gmmap.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import adapter.PetrolAdt;
import asyncTask.PetrolAst;
import listener.OnLoadListener;
import model.Petrol;

public class PetrolActivity extends AppCompatActivity
{
    ListView listView;
    PetrolAdt adapter;
    ArrayList<Petrol> listPetrol;
    ProgressBar prbLoading;
TextView txtLatest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petrol);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.lvPetrol);
        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);
        txtLatest = (TextView) findViewById(R.id.txtLatest);

        PetrolAst asyncTask = new PetrolAst();
        asyncTask.setListener(new OnLoadListener<JSONObject>()
        {
            @Override
            public void onFinish(JSONObject jsonObject)
            {
                try
                {
                    txtLatest.setText((String) jsonObject.get("time"));
                    listPetrol = (ArrayList<Petrol>) jsonObject.get("ListPetrol");
                    adapter = new PetrolAdt(PetrolActivity.this, R.layout.row_petrol, listPetrol);
                    listView.setAdapter(adapter);
                    prbLoading.setVisibility(View.GONE);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
        asyncTask.execute();
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
