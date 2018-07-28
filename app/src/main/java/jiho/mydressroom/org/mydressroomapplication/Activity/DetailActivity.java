package jiho.mydressroom.org.mydressroomapplication.Activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import jiho.mydressroom.org.mydressroomapplication.R;
import jiho.mydressroom.org.mydressroomapplication.Util.DBHelper;
import jiho.mydressroom.org.mydressroomapplication.Items.Items;

import static jiho.mydressroom.org.mydressroomapplication.Util.DBHelper.DATABASE_NAME;
import static jiho.mydressroom.org.mydressroomapplication.Util.DBHelper.DATABASE_VERSION;
import static jiho.mydressroom.org.mydressroomapplication.Util.DBHelper.TABLE_NAME;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView imageDetail;
    TextView txtTitleDetail, txtLPriceDetail;
    Button btnGoBuy, btnFavorite;
    Items itemList;
    Uri uri = null;
    long lowPrice;

    SQLiteDatabase database;
    DBHelper helper;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setElevation(0);

        initView();
        loadDataFromList();
        Log.e("mallName===", itemList.getMallName());
        Log.e("lowPrice===", itemList.getLprice()+"");
        //Log.e("link=====", itemForDetail.getLink());
        setData();

    }

    //Items 데이터를 가져옴.
    private void loadDataFromList() {
        Intent intent = getIntent();
        itemList = (Items) intent.getSerializableExtra("itemList");
        lowPrice = Long.parseLong(itemList.getLprice()+"");
        uri = Uri.parse(itemList.getLink());
    }

    private void initView() {
        imageDetail = (ImageView) findViewById(R.id.imageDetail);
        txtTitleDetail = (TextView) findViewById(R.id.txtTitleDetail);
        txtLPriceDetail = (TextView) findViewById(R.id.txtLPriceDetail);
        btnGoBuy = (Button) findViewById(R.id.btnGoBuyFavorite);
        btnFavorite = (Button) findViewById(R.id.btnFavorite);
        btnGoBuy.setOnClickListener(this);
        btnFavorite.setOnClickListener(this);

    }


    private void setData() {
        Glide.with(this).load(itemList.getImage()).into(imageDetail);
        txtTitleDetail.setText(itemList.getTitle());
        txtLPriceDetail.setText(lowPrice+"원");

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnGoBuyFavorite:
                Intent linkIntent = new Intent();
                linkIntent.setAction(Intent.ACTION_VIEW);
                linkIntent.setData(uri);
                if(linkIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(linkIntent);
                } else {
                    Toast.makeText(DetailActivity.this, "다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnFavorite:
                helper = new DBHelper(DetailActivity.this, DATABASE_NAME, null, DATABASE_VERSION);
                helper.getWritableDatabase();
                createDatabase();
                insertData(itemList);
                Toast.makeText(this, "관심항목에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }


   // 관심상품을 저장하기 위한 데이터베이스를 create or open.
    public void createDatabase(){
        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
    }

    // 현재 보고 있는 상세아이템을 데이터베이스에 저장.
    public void insertData(Items itemList){
        String sql = " INSERT INTO " + TABLE_NAME + " (PRODUCTNAME, IMAGEURL, LOWPRICE, MALLNAME, LINK) VALUES ('"
                     + itemList.getTitle() + "', '" + itemList.getImage() + "', '" + itemList.getLprice() + "', '" + itemList.getMallName() + "', '" + itemList.getLink() + "')" + ";";
        Log.e("sql===", sql);
        database.execSQL(sql);
    }

}
