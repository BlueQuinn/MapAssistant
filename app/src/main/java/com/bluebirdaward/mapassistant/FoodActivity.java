package com.bluebirdaward.mapassistant;

import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import Adapter.FoodAdt;
import AsyncTask.FoodAst;
import DTO.Food;
import DTO.Restaurant;
import Listener.OnLoadListener;
import com.bluebirdaward.mapassistant.gmmap.R;

public class FoodActivity extends AppCompatActivity implements OnLoadListener<ArrayList<Food>>, OnClickListener
{
    GridView gridMenu;
    TextView tvEmpty;
    ArrayList<Food> listFood;
    FoodAdt adapter;
    FoodAst asyncTask;
    Restaurant restaurant;
    FloatingActionButton btnGo;
    ProgressBar prbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");
        getSupportActionBar().setTitle(restaurant.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnGo = (FloatingActionButton) findViewById(R.id.btnGo);
        gridMenu = (GridView) findViewById(R.id.lvArticle);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);

        listFood = new ArrayList<>();

        adapter = new FoodAdt(getApplicationContext(), R.layout.cell_food, listFood);
        gridMenu.setAdapter(adapter);

        asyncTask = new FoodAst();
        asyncTask.setOnLoaded(this);
        asyncTask.execute("https://www.deliverynow.vn" + restaurant.getUrl());

        btnGo.setOnClickListener(this);
    }

    @Override
    public void onFinish(ArrayList<Food> list)
    {
        prbLoading.setVisibility(View.GONE);
        if (list.size() < 1)
        {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        for (int i = 0; i < list.size(); ++i)
        {
            listFood.add(list.get(i));
        }
        adapter.notifyDataSetChanged();
        gridMenu.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v)
    {
        String name = restaurant.getTitle();
        int separator = name.length();
        for (int i = 0; i < name.length(); ++i)
        {
            if (name.charAt(i) == '-')
            {
                separator = i;
            }
        }
        name = name.substring(0, separator);

        Intent intent = new Intent(this, DirectionActivity.class);
        intent.putExtra("destination", name);
        startActivity(intent);
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
