package jiho.mydressroom.org.mydressroomapplication.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import java.io.IOException;

import jiho.mydressroom.org.mydressroomapplication.R;
import jiho.mydressroom.org.mydressroomapplication.Util.StickerView;

public class DressroomActivity extends Activity {
    Bitmap bmp,bitmap,bgBitmap;
    StickerView stickerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dressroom);
        stickerView = (StickerView)findViewById(R.id.sticker_view);

        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        bgBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.main);
        stickerView.setWaterMark(bitmap, bgBitmap);

        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stickerView.saveBitmapToFile();
            }
        });

        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, 4);

            }
        });
        findViewById(R.id.btn_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DressroomActivity.this,EditActivity.class);
                startActivity(intent);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==4){
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                stickerView.setWaterMark(bmp, bgBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
