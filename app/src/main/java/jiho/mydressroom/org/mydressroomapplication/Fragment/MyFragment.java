package jiho.mydressroom.org.mydressroomapplication.Fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import jiho.mydressroom.org.mydressroomapplication.Activity.DressroomActivity;
import jiho.mydressroom.org.mydressroomapplication.Activity.MainActivity;
import jiho.mydressroom.org.mydressroomapplication.R;
import jiho.mydressroom.org.mydressroomapplication.Adapter.FavoriteAdapter;

import static jiho.mydressroom.org.mydressroomapplication.Util.DBHelper.DATABASE_NAME;
import static jiho.mydressroom.org.mydressroomapplication.Util.DBHelper.TABLE_NAME;

public class MyFragment extends Fragment {
    RecyclerView recyclerFavorite;
    FavoriteAdapter favoriteAdapter;
    TextView txtNone, txtItemCount;
    SQLiteDatabase database;
    Button btn_DressRoom;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout)inflater.inflate( R.layout.my_fregment ,container,false);
        recyclerFavorite = (RecyclerView) layout.findViewById(R.id.recyclerFavorite);
        txtNone = (TextView) layout.findViewById(R.id.txtNone);
        txtItemCount = (TextView) layout.findViewById(R.id.txtItemCount);
        btn_DressRoom = (Button)layout.findViewById(R.id.btn_DressRoom);
        btn_DressRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DressroomActivity.class);
                startActivity(intent);
            }
        });
        //txtItemCount.setVisibility(View.GONE);
        //setFavoriteAdapter();
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        favoriteAdapter = new FavoriteAdapter(getContext());
        recyclerFavorite.setAdapter(favoriteAdapter);
        recyclerFavorite.setLayoutManager(new LinearLayoutManager(getContext()));
        //'관심항목'이 있을 경우 배경에 나오는 '관심 항목이 없습니다.' 문구를 보이지 않게 함.
        if(favoriteAdapter.getItemCount() != 0){
            txtNone.setVisibility(View.GONE);
            txtItemCount.setVisibility(View.VISIBLE);
        }
        txtItemCount.setText(favoriteAdapter.getItemCount() + " 개");
    }

    protected boolean onCreateOptionsMenu(Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.menu_item_favorite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.deleteFavoriteAll:
                if(favoriteAdapter.getItemCount() != 0) {
                    AlertDialog.OnClickListener positiveListener = new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            database = SQLiteDatabase.openOrCreateDatabase("data/data/" + getActivity().getApplicationContext().getPackageName() + "/databases//" + DATABASE_NAME, null);
                            database.execSQL("DELETE FROM " + TABLE_NAME);
                            favoriteAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(), "관심 항목이 모두 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity(), "다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };

                    AlertDialog.OnClickListener negativeListener = new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    };

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext()).setMessage("모든 관심 항목을 삭제 하시겠습니까?").setPositiveButton("네", positiveListener).setNegativeButton("아니오", negativeListener);
                    dialog.show();
                } else {
                    Toast.makeText(getContext(), "등록된 관심항목이 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }
}
