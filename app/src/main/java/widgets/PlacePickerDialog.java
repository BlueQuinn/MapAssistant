package widgets;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bluebirdaward.mapassistant.gmmap.R;

import DTO.Nearby;
import Listener.OnLoadListener;

/**
 * Created by lequan on 8/30/2016.
 */
public class PlacePickerDialog extends Dialog
{
    public void setOnPickListener(OnLoadListener<Nearby> listener)
    {
        this.listener = listener;
    }

    OnLoadListener<Nearby> listener;

    public PlacePickerDialog(Context context)
    {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_place_picker);

        final TextView txtRadius = (TextView) findViewById(R.id.txtRadius);
        final SeekBar radiusPicker = (SeekBar) findViewById(R.id.radiusPicker);
        radiusPicker.setProgress(2);    // default 3km
        radiusPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                txtRadius.setText("Phạm vi tìm kiếm " + Integer.toString(progress + 1) + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        AutoCompleteTextView autoCompleteSearch = (AutoCompleteTextView) findViewById(R.id.auto_complete_search);

        //final String[] places = {"Kế toán", "Sân bay", "Công viên giải trí", "Hồ cá", "Phòng triển lãm", "ATM", "Tiệm bánh", "Ngân hàng", "Bar", "Thẩm mỹ viện", "Cửa hàng xe đạp", "Nhà sách", "Sàn chơi bowling", "Bến xe", "Cafe", "Khu cắm trại", "Đại lý ô tô", "Cho thuê xe", "Sửa chữa ô tô", "Rửa xe", "Casino", "Nghĩa trang", "Giáo hội", "Ủy Ban Nhân Dân thành phố", "Cửa hàng quần áo", "Cửa hàng tiện lợi", "Tòa án", "Nha sĩ", "Cửa hàng bách hóa", "Bác sĩ", "Thợ điện", "Cửa hàng điện tử", "Đại sứ quán", "Tài chính", "Trạm cứu hỏa", "Cửa hàng hoa", "Thực phẩm", "Nhà tang lễ", "Cửa hàng nội thất", "Trạm xăng", "Cửa hàng tạp hóa, siêu thị", "Thể hình", "Chăm sóc tóc", "Cửa hàng phần mềm", "Sức khỏe", "Cửa hàng đồ gia dụng", "Bệnh viện", "Cơ quan bảo hiểm", "Cửa hàng trang sức", "Giặt ủi", "Luật sư", "Thư viện", "Cửa hàng rượu", "Văn phòng Chính phủ địa phương", "Thợ khóa", "Nhà nghỉ ", "Giao hàng thức ăn", "Thức ăn mang đi", "Nhà thờ Hồi giáo", "Thuê phim", "Rạp phim", "Công ty chuyển hàng", "Bảo tàng ", "Hộp đêm", "Thợ sơn", "Công viên", "Bãi giữ xe", "Cửa hàng vật nuôi", "Nhà thuốc", "Thợ ống nước", "Cảnh sát", "Bưu điện", "Cơ quan bất động sản", "Nhà hàng", "Trường học", "Cửa hàng giày dép", "Trung tâm mua sắm", "Spa", "Sân vận động", "Kho", "Cửa hàng", "Trạm taxi", "Ga tàu lửa", "Cơ quan du lịch", "Đại học", "Sở thú"};
        String[] places = { "Kế toán", "Sân bay", "Công viên giải trí", "Hồ cá", "Phòng triển lãm", "ATM", "Tiệm bánh", "Ngân hàng", "Bar", "Thẩm mỹ viện", "Cửa hàng xe đạp", "Nhà sách", "Sàn chơi bowling", "Bến xe bus", "Cafe", "Khu cắm trại", "Đại lý ô tô", "Cho thuê xe", "Sửa xe", "Rửa xe", "Casino", "Nghĩa trang", "Nhà thờ", "Ủy Ban Nhân Dân", "Cửa hàng quần áo", "Cửa hàng tiện lợi", "Tòa án", "Nha sĩ", "Cửa hàng bách hóa", "Bác sĩ", "Thợ điện", "Cửa hàng điện tử", "Đại sứ quán", "Tài chính", "Trạm cứu hỏa","Cửa hàng hoa", "Thực phẩm", "Nhà tang lễ", "Cửa hàng nội thất", "Trạm xăng", "Cửa hàng tạp hóa, siêu thị", "Thể hình", "Chăm sóc tóc", "Cửa hàng phần cứng", "Đồ gia dụng", "Bệnh viện", "Công ty bảo hiểm", "Trang sức", "Giặt ủi", "Luật sư", "Thư viện", "Văn phòng chính quyền địa phương", "Thợ sửa khóa", "Nhà nghỉ ", "Thức ăn mang đi", "Thuê phim", "Rạp phim", "Bảo tàng ", "Hộp đêm", "Công viên", "Bãi giữ xe", "Nhà thuốc", "Thợ sửa ống nước", "Cảnh sát", "Bưu điện", "Công ty bất động sản", "Nhà hàng", "Lợp mái", "Trường học", "Giày dép", "Trung tâm mua sắm", "Spa", "Sân vận động", "Nhà kho", "Cửa hàng", "Nhà thờ Hồi giáo", "Trạm taxi", "Ga tàu lửa", "Công ty vận tải", "Công ty du lịch", "Đại học - Cao đẳng", "Bác sĩ thú y", "Sở thú"};

        //final String[] type = {"accounting", "airport", "amusement_park", "aquarium", "art_gallery", "atm", "bakery", "bank", "bar", "beauty_salon", "bicycle_store", "book_store", "bowling_alley", "bus_station", "cafe", "campground", "car_dealer", "car_rental", "car_repair", "car_wash", "casino", "cemetery", "church", "city_hall", "clothing_store", "convenience_store", "courthouse", "dentist", "department_store", "doctor", "electrician", "electronics_store", "embassy", "establishment", "finance", "fire_station", "florist", "food", "funeral_home", "furniture_store", "gas_station", "general_contractor", "grocery_or_supermarket", "gym", "hair_care", "hardware_store", "health", "hindu_temple", "home_goods_store", "hospital", "insurance_agency", "jewelry_store", "laundry", "lawyer", "library", "liquor_store", "local_government_office", "locksmith", "lodging", "meal_delivery", "meal_takeaway", "mosque", "movie_rental", "movie_theater", "moving_company", "museum", "night_club", "painter", "park", "parking", "pharmacy", "physiotherapist", "place_of_worship", "plumber", "police", "post_office", "real_estate_agency", "restaurant", "roofing_contractor", "school", "shoe_store", "shopping_mall", "spa", "stadium", "storage", "store", "synagogue", "taxi_stand", "train_station", "transit_station", "travel_agency", "university", "veterinary_care", "zoo"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, places);
        autoCompleteSearch.setAdapter(adapter);
        autoCompleteSearch.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String placeType = getPlaceType(adapter.getItem(position));
                if (placeType.length() > 0)
                {
                    //Log.d("234", places[position] + ", " + adapter.getItem(position) + ", " + position + ", " + type[position]);
                    listener.onFinish(new Nearby(placeType, radiusPicker.getProgress() + 1));
                }
            }
        });
    }

    String getPlaceType(String type)
    {
        switch (type)
        {
            case "Kế toán":
                return "accounting";
            case "Sân bay":
                return "airport";
            case "Công viên giải trí":
                return "amusement_park";
            case "Hồ cá":
                return "aquarium";
            case "Phòng triển lãm":
                return "art_gallery";
            case "ATM":
                return "atm";
            case "Tiệm bánh":
                return "bakery";
            case "Ngân hàng":
                return "bank";
            case "Bar":
                return "bar";
            case "Thẩm mỹ viện":
                return "beauty_salon";
            case "Cửa hàng xe đạp":
                return "bicycle_store";
            case "Nhà sách":
                return "book_store";
            case "Sàn chơi bowling":
                return "bowling_alley";
            case "Bến xe bus":
                return "bus_station";
            case "Cafe":
                return "cafe";
            case "Khu cắm trại":
                return "campground";
            case "Đại lý ô tô":
                return "car_dealer";
            case "Cho thuê xe":
                return "car_rental";
            case "Sửa xe":
                return "car_repair";
            case "Rửa xe":
                return "car_wash";
            case "Casino":
                return "casino";
            case "Nghĩa trang":
                return "cemetery";
            case "Nhà thờ":
                return "church";
            case "Ủy Ban Nhân Dân":
                return "city_hall";
            case "Cửa hàng quần áo":
                return "clothing_store";
            case "Cửa hàng tiện lợi":
                return "convenience_store";
            case "Tòa án":
                return "courthouse";
            case "Nha sĩ":
                return "dentist";
            case "Cửa hàng bách hóa":
                return "department_store";
            case "Bác sĩ":
                return "doctor";
            case "Thợ điện":
                return "electrician";
            case "Cửa hàng điện tử":
                return "electronics_store";
            case "Đại sứ quán":
                return "embassy";
            case "Tài chính":
                return "finance";
            case "Trạm cứu hỏa":
                return "fire_station";
            case "Cửa hàng hoa":
                return "florist";
            case "Thực phẩm":
                return "food";
            case "Nhà tang lễ":
                return "funeral_home";
            case "Cửa hàng nội thất":
                return "furniture_store";
            case "Trạm xăng":
                return "gas_station";
            case "Cửa hàng tạp hóa, siêu thị":
                return "grocery_or_supermarket";
            case "Thể hình":
                return "gym";
            case "Chăm sóc tóc":
                return "hair_care";
            case "Cửa hàng phần cứng":
                return "hardware_store";
            case "Đồ gia dụng":
                return "home_goods_store";
            case "Bệnh viện":
                return "hospital";
            case "Công ty bảo hiểm":
                return "insurance_agency";
            case "Trang sức":
                return "jewelry_store";
            case "Giặt ủi":
                return "laundry";
            case "Luật sư":
                return "lawyer";
            case "Thư viện":
                return "library";
            case "Văn phòng chính quyền địa phương":
                return "local_government_office";
            case "Thợ sửa khóa":
                return "locksmith";
            case "Nhà nghỉ":
                return "lodging";
            case "Thức ăn mang đi":
                return "meal_takeaway";
            case "Thuê phim":
                return "movie_rental";
            case "Rạp phim":
                return "movie_theater";
            case "Bảo tàng":
                return "museum";
            case "Hộp đêm":
                return "night_club";
            case "Công viên":
                return "park";
            case "Bãi giữ xe":
                return "parking";
            case "Nhà thuốc":
                return "pharmacy";
            case "Thợ sửa ống nước":
                return "plumber";
            case "Cảnh sát":
                return "police";
            case "Bưu điện":
                return "post_office";
            case "Công ty bất động sản":
                return "real_estate_agency";
            case "Nhà hàng":
                return "restaurant";
            case "Lợp mái":
                return "roofing_contractor";
            case "Trường học":
                return "school";
            case "Giày dép":
                return "shoe_store";
            case "Trung tâm mua sắm":
                return "shopping_mall";
            case "Spa":
                return "spa";
            case "Sân vận động":
                return "stadium";
            case "Nhà kho":
                return "storage";
            case "Cửa hàng":
                return "store";
            case "Nhà thờ Hồi giáo":
                return "synagogue";
            case "Trạm taxi":
                return "taxi_stand";
            case "Ga tàu lửa":
                return "train_station";
            case "Công ty vận tải":
                return "transit_station";
            case "Công ty du lịch":
                return "travel_agency";
            case "Đại học - Cao đẳng":
                return "university";
            case "Bác sĩ thú y":
                return "veterinary_care";
            case "Sở thú":
                return "zoo";
        }
        return "";
    }
}
