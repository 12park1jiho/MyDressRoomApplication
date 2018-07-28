package jiho.mydressroom.org.mydressroomapplication.Activity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import jiho.mydressroom.org.mydressroomapplication.Adapter.FavoriteAdapter;
import jiho.mydressroom.org.mydressroomapplication.R;
import jiho.mydressroom.org.mydressroomapplication.Util.DBHelper;
import jiho.mydressroom.org.mydressroomapplication.Util.FavoriteItem;

import static jiho.mydressroom.org.mydressroomapplication.Util.DBHelper.DATABASE_NAME;
import static jiho.mydressroom.org.mydressroomapplication.Util.DBHelper.TABLE_NAME;

public class DetailFavoriteActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = getPackageName();
    ImageView imageDetailFavorite;
    Button btnGoBuyFavorite, btnDelFavorite;
    TextView txtTitleFavorite, txtLPriceFavorite;
    FavoriteItem items;
    FavoriteAdapter adapter;

    SQLiteDatabase database;
    DBHelper helper;
    int id;
    long lowPrice;

    int year,monthOfYear,dayOfMonth,hourOfDay,minute,second;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        setContentView(R.layout.activity_detail_favorite);
        adapter = new FavoriteAdapter(this);
        initView();
        loadData();
    }

    private void initView(){
        imageDetailFavorite = (ImageView) findViewById(R.id.imageDetailFavorite);
        imageDetailFavorite.setOnClickListener(this);
        btnGoBuyFavorite = (Button) findViewById(R.id.btnGoBuyFavorite);
        txtTitleFavorite = (TextView) findViewById(R.id.txtTitleDetail);
        txtLPriceFavorite = (TextView) findViewById(R.id.txtLPriceFavorite);
        btnGoBuyFavorite.setOnClickListener(this);
        btnDelFavorite = (Button) findViewById(R.id.btnDelFavorite);
        btnDelFavorite.setOnClickListener(this);
        Calendar mCalendar = Calendar.getInstance();
        Date curDate = new Date();
        mCalendar.setTime(curDate);
        year = mCalendar.get( Calendar.YEAR);
        monthOfYear = mCalendar.get(Calendar.MONTH);
        dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
        hourOfDay = mCalendar.get(Calendar.HOUR_OF_DAY);
        minute = mCalendar.get(Calendar.MINUTE);
        second = mCalendar.get(Calendar.SECOND);
    }

    private void loadData(){
        Intent intent = getIntent();
        items = (FavoriteItem) intent.getSerializableExtra("itemList");
        setData(items);
        id = intent.getIntExtra("position", 0);
        Log.e("id===", id+"");

    }

    private void setData(FavoriteItem items){
        Glide.with(this).load(items.getImage()).into(imageDetailFavorite);
        txtTitleFavorite.setText(items.getTitle());
        lowPrice = Long.parseLong(items.getLprice());
        txtLPriceFavorite.setText(lowPrice+"원");


    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnGoBuyFavorite:
                Uri uri = Uri.parse(items.getLink());
                Intent intentGoBuy = new Intent();
                intentGoBuy.setAction(Intent.ACTION_VIEW);
                intentGoBuy.setData(uri);
                startActivity(intentGoBuy);
                break;
            case R.id.btnDelFavorite:
                database = SQLiteDatabase.openOrCreateDatabase("data/data/" + this.getApplicationContext().getPackageName() + "/databases//" + DATABASE_NAME, null);
                database.execSQL("DELETE FROM " + TABLE_NAME + " WHERE PRODUCTNAME=" + "'" + items.getTitle() + "';");
                Intent intentDelFavorite = new Intent(this, MainActivity.class);
                intentDelFavorite.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentDelFavorite);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "관심 품목을 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.imageDetailFavorite:
                String downloadImage = String.valueOf(Glide.with(this).load(items.getImage()).into(imageDetailFavorite));
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(downloadImage);
                checkPermission();
                downloadFile(items.getImage());
                break;
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //퍼미션이 없는 경우
            //최초로 퍼미션을 요청하는 것인지 사용자가 취소되었던것을 다시 요청하려는건지 체크
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //퍼미션을 재요청 하는 경우 - 왜 이 퍼미션이 필요한지등을 대화창에 넣어서 사용자를 설득할 수 있다.
                //대화상자에 '다시 묻지 않기' 체크박스가 자동으로 추가된다.
                Log.v(TAG, "퍼미션을 재요청 합니다.");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            } else {
                //처음 퍼미션을 요청하는 경우
                Log.v(TAG, "첫 퍼미션 요청입니다.");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            //퍼미션이 있는 경우 - 쭉 하고 싶은 일을 한다.
            Log.v(TAG, "Permission is granted");
        }
    }

    private Bitmap downLoadUrl(String strUrl)throws IOException{
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try{
            URL url = new URL(strUrl);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        }catch (Exception e){

        }finally {
            inputStream.close();
        }return bitmap;
    }

    private class DownloadTask extends AsyncTask<String, Integer, Bitmap>{
        Bitmap bitmap=null;

        @Override
        protected Bitmap doInBackground(String... url) {
            try {
                bitmap = downLoadUrl(url[0]);
            }catch (Exception e){

            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Toast.makeText(getApplicationContext(),"다운로드 완료",Toast.LENGTH_LONG).show();
        }
    }
    public void downloadFile(String url) {
        String imagePath = Environment.getExternalStorageDirectory() + "/Download/"+year+"년"+monthOfYear+"월"+dayOfMonth+"일"+hourOfDay+"시"+minute+"분"+second+"초";

        File direct = new File(Environment.getExternalStorageDirectory() + "/download");

        if (!direct.exists()) {

            direct.mkdir();

        } // end of if

        DownloadManager mgr = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);



        Uri downloadUri = Uri.parse(url);

        DownloadManager.Request request = new DownloadManager.Request(

                downloadUri);



        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |

                DownloadManager.Request.NETWORK_MOBILE)

                .setAllowedOverRoaming(false)

                .setTitle("Sample")

                .setDescription("Something useful. No, Really")

                .setDestinationInExternalPublicDir("/download/", imagePath+".jpg");

            mgr.enqueue(request);


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: //
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //사용자가 동의했을때
                    Toast.makeText(this, "퍼미션 동의", Toast.LENGTH_SHORT).show();
                } else {
                    //사용자가 거부 했을때
                    Toast.makeText(this, "거부 - 동의해야 사용가능합니다.", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);

                }
                return;
        }
    }
}
