package jiho.mydressroom.org.mydressroomapplication.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jypdev.maskcroplibrary.ImageUtil;
import com.jypdev.maskcroplibrary.MaskCropView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import jiho.mydressroom.org.mydressroomapplication.R;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {
    int year,monthOfYear,dayOfMonth,hourOfDay,minute,second;
    String imagePath = Environment.getExternalStorageDirectory() + "/Download/"+year+"년"+monthOfYear+"월"+dayOfMonth+"일"+hourOfDay+"시"+minute+"분"+second+"초"+".png";
    private MaskCropView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        findViewById(R.id.camera_button).setOnClickListener(this);
        findViewById(R.id.gallery_button).setOnClickListener(this);
        findViewById(R.id.confirm_button).setOnClickListener(this);
        view = (MaskCropView) findViewById(R.id.maskview);
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_button: {
                //카메라 인텐트를 오픈
                ImageUtil.getCameraImage(this);
            }
            break;
            case R.id.gallery_button: {
                //갤러리 인텐트를 오픈
                ImageUtil.getAlbumImage(this);
            }
            break;
            case R.id.confirm_button:{
                Bitmap bitmap = view.getPicture();
                if(bitmap!=null) {
                    FileOutputStream fos;
                    try {
                        //현재 이미지를 PNG파일로 저장
                        fos = new FileOutputStream(imagePath);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        Toast.makeText(EditActivity.this, "success", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EditActivity.this,DressroomActivity.class);
                        startActivity(intent);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(EditActivity.this, "null", Toast.LENGTH_SHORT).show();
                }
            }break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        ImageUtil.onActivityResult(this,view,requestCode,resultCode,data);
    }

}