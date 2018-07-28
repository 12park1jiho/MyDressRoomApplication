package jiho.mydressroom.org.mydressroomapplication.Fragment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Arrays;

import jiho.mydressroom.org.mydressroomapplication.Adapter.ChatAdapter;
import jiho.mydressroom.org.mydressroomapplication.R;
import jiho.mydressroom.org.mydressroomapplication.Activity.SendActivity;
import jiho.mydressroom.org.mydressroomapplication.Items.ChatItems;
import jiho.mydressroom.org.mydressroomapplication.Util.CustomLayoutManager;

public class OtherFragment extends Fragment {
    public static final int RC_SIGN_IN = 1;
    ChatAdapter adapter;
    CustomLayoutManager customLayoutManager;

    RecyclerView recyclerView;
    Button btn_Insert;

    FirebaseDatabase database;
    DatabaseReference ref;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseAuth.AuthStateListener AuthStateListener;
    String mUsername;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout)inflater.inflate( R.layout.other_fragment ,container,false);
        recyclerView = (RecyclerView)layout.findViewById( R.id.recyclerView );
        btn_Insert = (Button)layout.findViewById( R.id.btn_Insert );
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();


        // 쿼리 수행 위치
        Query query = ref.child("message");
        FirebaseRecyclerOptions<ChatItems> options =
                new FirebaseRecyclerOptions.Builder<ChatItems>()
                        .setQuery(query, ChatItems.class)
                        .build();
        adapter = new ChatAdapter(options);

        // 리사이클러뷰에 레이아웃 매니저와 어댑터 설정
        customLayoutManager = new CustomLayoutManager(getContext());
        recyclerView.setLayoutManager(customLayoutManager);
        recyclerView.setAdapter(adapter);

        // 새로운 글이 추가되면 제일 하단으로 포지션 이동
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int BoardCount = adapter.getItemCount();
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (BoardCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        // 키보드 올라올 때 RecyclerView의 위치를 마지막 포지션으로 이동
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.smoothScrollToPosition(adapter.getItemCount());
                        }
                    }, 100);
                }
            }
        });
        btn_Insert.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( getActivity(),SendActivity.class );
                startActivity( intent );
            }
        } );
        AuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!= null){
                    onSignedInInitialize(user.getDisplayName());
                }
                else{
                    onSignedOutCleanup();
                    //user is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder().setIsSmartLockEnabled(false) //smart lock Automatically saves the user credentials and lock them in
                                    .setAvailableProviders( Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),

                                            new AuthUI.IdpConfig.EmailBuilder().build()))

                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // FirebaseRecyclerAdapter 실시간 쿼리 시작
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // FirebaseRecyclerAdapter 실시간 쿼리 중지
        adapter.stopListening();
    }
    public void onSignedInInitialize(String username){
        mUsername = username;
    }

    public void onSignedOutCleanup(){
        mUsername = null;
    }
}
