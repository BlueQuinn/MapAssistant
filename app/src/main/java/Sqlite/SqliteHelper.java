package sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import model.MyTraffic;
import model.Shortcut;
import model.TrafficCircle;
import model.TrafficLine;
import utils.MapUtils;
import utils.PolyUtils;

/**
 * Created by lequan on 9/20/15.
 */
public class SqliteHelper extends SQLiteOpenHelper
{
    private static String DB_PATH;
    private static String DB_NAME;
    private Context context;
    static SQLiteDatabase db;
    private static final int DB_VERSION = 1;

    public SqliteHelper(Context context, String database)
    {
        super(context, database, null, DB_VERSION);
        this.context = context;
        this.DB_NAME = database;
        DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {

    }

    @Override
    public synchronized void close()
    {
        if (db != null)
        {
            db.close();
        }
        super.close();
    }

    //check if database doesn't exist
    public boolean checkDataBase()
    {
        boolean check = false;
        try
        {
            String path = context.getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator + DB_NAME;
            check = new File(path).exists();
        }
        catch (SQLiteException e)
        {
            System.out.println("Database doesn't exist");
        }

        return check;
    }

    public void createDataBase() throws IOException
    {
        if (!checkDataBase())
        {
            getWritableDatabase();
            copyDataBase();
        }
    }

    public boolean excute(String sql)
    {
        try
        {
            db.execSQL(sql);
            Log.d("ERROR", "\n\n\nsql excute success");
            return true;
        }
        catch (Exception e)
        {
            Log.d("ERROR", "\n\n\nsql excute failed");
            e.printStackTrace();
            return false;
        }
    }

    private void copyDataBase() throws IOException
    {
        //Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        //transfer bytes from the input file to the output file
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0)
        {
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException
    {
        String myPath = DB_PATH + DB_NAME;
        //String myPath = context.getFilesDir().getAbsolutePath().replace("files", "databases")+File.separator + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public boolean saveTraffic(int id, double lat, double lng, int radius, String address, int timeNode, String jamType)
    {
        Date today = new Date();
        String[] datetime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(today).split(" ");
        String[] date = datetime[0].split("-");
        String[] time = datetime[1].split(":");

        String moment = "Ngày " + date[0] + "/" + date[1] + " lúc " + time[0] + ":" + time[1];

        String type = jamType.substring(0, 1);
        return excute(String.format(Locale.US, "insert into MyTraffic values (%d, '%s', '%s', %d, '%s', '%s', %d, '%s')",
                id, Double.toString(lat), Double.toString(lng), radius, moment, address.replace(", Hồ Chí Minh", ""), timeNode, type));
    }

    public void insertDestination(String table, String place, String address, double lat, double lng)
    {
        String latStr = Double.toString(lat);
        String lngStr = Double.toString(lng);
        excute(String.format("insert into %s values ('%s', '%s', '%s', '%s')", table, place, address, latStr, lngStr));
    }

    public double[] getPlace(String table, String place)
    {
        if (db != null)
        {
            Cursor cursor = db.rawQuery(String.format("select Lat, Lng from %s where Place = '%s'", table, place), null);
            if (cursor.moveToFirst())
            {
                double lat = cursor.getDouble(0);
                double lng = cursor.getDouble(1);
                return new double[]{lat, lng};
            }
        }
        return null;
    }

    public void delete(String table, String place)
    {
        db.delete(table, "Place = ?", new String[]{place});
    }

    public ArrayList<HashMap<String, String>> getAll(String table)
    {
        return executeQuery("select * from " + table);
    }

    public ArrayList<HashMap<String, String>> executeQuery(String sql)   // place - address
    {
        ArrayList<HashMap<String, String>> listData = new ArrayList<HashMap<String, String>>();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null)
        {
            String[] columnNames = cursor.getColumnNames();
            if (cursor.moveToFirst())
            {
                while (!cursor.isAfterLast())
                {
                    HashMap<String, String> item = new HashMap<>();
                    for (int i = 0; i < columnNames.length; ++i)
                    {
                        item.put(columnNames[i], cursor.getString(i));
                    }
                    listData.add(item);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return listData;
    }

    int getNextID()
    {
        Cursor cursor = db.rawQuery("select max(ID) from MyTraffic", null);
        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                return cursor.getInt(0) + 1;
            }
            cursor.close();
        }
        return 0;
    }

    public boolean addShortcut(int time, String jamType, int ID, String route, int distance, int duration)
    {
        String type = jamType.substring(0, 1);
        return excute(String.format(Locale.US, "insert into Shortcut values (%d, '%s', %d, %d, %d, '%s', %d, '%s')",
                ID, route, distance, duration, 0, "0", time, type));
    }

    ArrayList<Shortcut> getShortcut(int ID)
    {
        ArrayList<Shortcut> shortcuts = new ArrayList<>();
        ArrayList<HashMap<String, String>> data = executeQuery("select * from Shortcut where ID = " + ID);
        for (HashMap<String, String> row : data)
        {
            String route = row.get("Route");
            int distance = Integer.parseInt(row.get("Distance"));
            int duration = Integer.parseInt(row.get("Duration"));
            shortcuts.add(new Shortcut(route, distance, duration, 1));
        }
        return shortcuts;
    }

    public ArrayList<MyTraffic> getMyTraffic()
    {
        ArrayList<MyTraffic> myTraffics = new ArrayList<>();
        ArrayList<HashMap<String, String>> data = getAll("MyTraffic");
        for (HashMap<String, String> row : data)
        {
            int id = Integer.parseInt(row.get("ID"));
            String time = row.get("Moment");
            double lat = Double.parseDouble(row.get("Lat"));
            double lng = Double.parseDouble(row.get("Lng"));
            int radius = Integer.parseInt(row.get("Radius"));
            String address = row.get("Address");
            ArrayList<Shortcut> shortcuts = getShortcut(id);
            myTraffics.add(new MyTraffic(id, new LatLng(lat, lng), radius, time, address, shortcuts));
        }
        return myTraffics;
    }

    public ArrayList<MyTraffic> getMyLocation()
    {
        ArrayList<MyTraffic> myTraffics = new ArrayList<>();
        ArrayList<HashMap<String, String>> data = getAll("MyTraffic");
        for (HashMap<String, String> row : data)
        {
            int ID = Integer.parseInt(row.get("ID"));
            double lat = Double.parseDouble(row.get("Lat"));
            double lng = Double.parseDouble(row.get("Lng"));
            myTraffics.add(new MyTraffic(ID, new LatLng(lat, lng)));
        }
        return myTraffics;
    }

    public boolean haveStucked(TrafficCircle traffic)
    {
        ArrayList<MyTraffic> myLocation = getMyLocation();
        for (MyTraffic i : myLocation)
        {
            if (MapUtils.inCircle(i.getCenter(), traffic.getCenter(), traffic.getRadius()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean haveStucked(TrafficLine traffic)
    {
        ArrayList<MyTraffic> myLocation = getMyLocation();
        for (MyTraffic i : myLocation)
        {
            if (PolyUtils.isLocationOnPath(i.getCenter(), traffic.getPolyline(), false, 100))
            {
                return true;
            }
        }
        return false;
    }

    public boolean checkLiked(int time, String jamType, int ID)
    {
        String type = jamType.substring(0, 1);
        ArrayList<HashMap<String, String>> data = executeQuery(String.format(Locale.US,
                "select * from Shortcut where Time = %d and JamType = '%s' and ID = %d and Like = '1'",
                time, type, ID));
        return data.size() > 0;
    }

    boolean rating(int time, String jamType, int ID, int rating, String like)
    {
        String type = jamType.substring(0, 1);
        return excute(String.format(Locale.US,
                "update Shortcut set Like = '%s', Rating = %d" +
                        " where Time = %d and Jam = '%s' and ID = %d",
                like, rating, time, type, ID));
    }

    public boolean like(int time, String jamType, int ID, int rating)
    {
        return rating(time, jamType, ID, rating, "1");
    }

    public boolean dislike(int time, String jamType, int ID, int rating)
    {
        return rating(time, jamType, ID, rating, "0");
    }

    public boolean checkShortcutDuplicate(int time, String jamType, int ID, String route)
    {
        String type = jamType.substring(0, 1);
        ArrayList<HashMap<String, String>> data = executeQuery(String.format(Locale.US,
                "select * from Shortcut where Time = %d and JamType = '%s' and ID = %d and Route = '%s'",
                time, type, ID, route));
        return data.size() > 0;
    }
}
