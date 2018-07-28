package jiho.mydressroom.org.mydressroomapplication.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import jiho.mydressroom.org.mydressroomapplication.Items.ChatItems;
import jiho.mydressroom.org.mydressroomapplication.R;

public class SendActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER =  2;

    EditText et_sendName,et_sendTitle,et_sendText;
    Button btn_setPhoto,btn_sendBoard;
    ImageView iv_sendPhoto;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference ref;
    FirebaseStorage storage;
    StorageReference storageReference;


    Uri selectedImageUri;
    String mUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_send );
        init();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        mUsername = user.getDisplayName();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        ref = database.getReference().child( "message" );
        storageReference = storage.getReference().child( "chat_photos" );
        btn_setPhoto.setOnClickListener( this );
        btn_sendBoard.setOnClickListener( this );
    }

    private void init() {
        et_sendName = (EditText)findViewById( R.id.et_sendName );
        et_sendTitle = (EditText)findViewById( R.id.et_sendTitle );
        et_sendText = (EditText)findViewById( R.id.et_sendText );
        btn_setPhoto = (Button)findViewById( R.id.btn_setPhoto );
        btn_sendBoard = (Button)findViewById( R.id.btn_sendBoard );
        iv_sendPhoto = (ImageView)findViewById( R.id.iv_sendPhoto );
    }

    @Override
    public void onClick(View view) {
        if(view==btn_setPhoto){
            //갤러리에서 이미지파일을 선택 후 이미지 set
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
        }else if(view==btn_sendBoard){

            final StorageReference photoRef = storageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    ChatItems items = new ChatItems(mUsername,et_sendTitle.getText().toString(), et_sendText.getText().toString(), uri.toString());
                                    ref.push().setValue(items);
                                    Intent intent = new Intent( SendActivity.this,MainActivity.class );
                                    startActivity( intent );
                                }
                            });
                        }
                    });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Sign in canceled!", Toast.LENGTH_SHORT).show();
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            iv_sendPhoto.setImageURI( selectedImageUri );
        }
    }
}
