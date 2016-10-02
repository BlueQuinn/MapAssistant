package com.bluebirdaward.mapassistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

import adapter.RestaurantSectionAdt;
import model.RestaurantSection;
import com.bluebirdaward.mapassistant.gmmap.R;

public class PlaceActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    ArrayList<RestaurantSection> list;
    RestaurantSectionAdt adapter;
    GridView grid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ăn gì - Ở đâu");

        list = new ArrayList<>();
        list.add(new RestaurantSection("Mì, bún, phở, cháo", "mi-bun-pho-chao", R.drawable.noodle));
        list.add(new RestaurantSection("Cafe, trà sữa", "tra-sua-tra-chanh", R.drawable.drink));
        list.add(new RestaurantSection("Lẩu", "lau", R.drawable.hotpot));
        list.add(new RestaurantSection("Bánh mì, xôi", "banh-mi-xoi", R.drawable.bread));
        list.add(new RestaurantSection("Hải sản", "hai-san", R.drawable.seafood));
        list.add(new RestaurantSection("Món chay", "mon-chay", R.drawable.salad));
        list.add(new RestaurantSection("Kem, bánh ngọt", "banh-kem", R.drawable.icecream));
        list.add(new RestaurantSection("Đồ nướng", "thit-nuong-quay", R.drawable.bbq));
        list.add(new RestaurantSection("Pizza, hamburger", "pizza-hamburger", R.drawable.pizza));
        list.add(new RestaurantSection("Cơm", "com-ga-com-tam", R.drawable.rice));
        list.add(new RestaurantSection("Trái cây tươi", "trai-cay-sach", R.drawable.fruit));
        list.add(new RestaurantSection("Ăn vặt, ăn nhẹ", "an-vat-an-nhe", R.drawable.fastfood));

        adapter = new RestaurantSectionAdt(getApplicationContext(), R.layout.cell_place, list);
        grid = (GridView) findViewById(R.id.gridPlace);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent intent = new Intent(this, RestaurantActivity.class);
        intent.putExtra("name", list.get(position).getName());
        intent.putExtra("url", list.get(position).getUrl());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
