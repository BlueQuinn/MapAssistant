package com.bluebirdaward.mapassistant;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    String url;
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

        url = "https://www.deliverynow.vn/ho-chi-minh/danh-sach-dia-diem-phuc-vu-" + intent.getStringExtra("url") + "-giao-tan-noi";
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        else        // copy link
        {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("hehe", url));
            Toast.makeText(this, "Đã copy link", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    void requestNewInterstitial()
    {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("95C26624BAF06BE43C74469300F76D9E")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
}
