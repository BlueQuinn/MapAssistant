package com.bluebirdaward.mapassistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import adapter.RestaurantAdt;
import asyncTask.RestaurantAst;
import model.Restaurant;
import listener.OnLoadListener;

import com.bluebirdaward.mapassistant.gmmap.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class RestaurantActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, OnLoadListener<ArrayList<Restaurant>>
{
    GridView gridView;
    TextView tvEmpty;
    ArrayList<Restaurant> listRestaurant;
    RestaurantAdt adapter;
    ProgressBar prbLoading;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        gridView = (GridView) findViewById(R.id.lvArticle);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        prbLoading = (ProgressBar) findViewById(R.id.prbLoading);

        listRestaurant = new ArrayList<>();

        adapter = new RestaurantAdt(getApplicationContext(), R.layout.cell_restaurant, listRestaurant);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);

        Intent intent = getIntent();

        String url = "https://www.deliverynow.vn/ho-chi-minh/danh-sach-dia-diem-phuc-vu-" + intent.getStringExtra("url") + "-giao-tan-noi";
        RestaurantAst asyncTask = new RestaurantAst(this.findViewById(android.R.id.content));
        asyncTask.setOnLoaded(this);
        asyncTask.execute(url);

        getSupportActionBar().setTitle(intent.getStringExtra("name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8112894826901791/5653913263");

        mInterstitialAd.setAdListener(new AdListener()
        {
            @Override
            public void onAdClosed()
            {
                requestNewInterstitial();
                finish();
            }
        });

        requestNewInterstitial();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent i = new Intent(this, FoodActivity.class);
        i.putExtra("restaurant", adapter.getItem(position));
        startActivity(i);
    }

    @Override
    public void onFinish(ArrayList<Restaurant> list)
    {
        prbLoading.setVisibility(View.GONE);
        if (list.size() < 1)
        {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        for (int i = 0; i < list.size(); ++i)
        {
            listRestaurant.add(list.get(i));
        }

        adapter.notifyDataSetChanged();
        gridView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            if (mInterstitialAd.isLoaded())
            {
                mInterstitialAd.show();
            }
            else
            {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void requestNewInterstitial()
    {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
}
