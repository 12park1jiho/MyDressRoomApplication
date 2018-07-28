package jiho.mydressroom.org.mydressroomapplication.Fragment;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jiho.mydressroom.org.mydressroomapplication.Activity.MainActivity;
import jiho.mydressroom.org.mydressroomapplication.R;
import jiho.mydressroom.org.mydressroomapplication.Util.CustomLayoutManager;
import jiho.mydressroom.org.mydressroomapplication.Util.DBHelper;
import jiho.mydressroom.org.mydressroomapplication.Items.Items;
import jiho.mydressroom.org.mydressroomapplication.Adapter.ListTypeAdapter;
import jiho.mydressroom.org.mydressroomapplication.Util.NaverShoppingSearchService;
import jiho.mydressroom.org.mydressroomapplication.Util.SearchDataList;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;
import static jiho.mydressroom.org.mydressroomapplication.Util.DBHelper.DATABASE_NAME;
import static jiho.mydressroom.org.mydressroomapplication.Util.DBHelper.DATABASE_VERSION;

public class SearchFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static String NAVER_URL = "https://openapi.naver.com/v1/search/";

    TextView textLowPrice;
    ImageButton btnSearch;
    EditText query;
    String queryString;
    List<Items> itemList;
    ProgressBar progressBar;
    RadioButton radioBtnSim, radioBtnPrice;
    RadioGroup radioGroupSort;

    RecyclerView recyclerView;
    ListTypeAdapter listTypeAdapter;
    CustomLayoutManager customLayoutManager;

    SwipeRefreshLayout swipeRefreshLayout;

    SQLiteDatabase database;

    RequestManager requestManager;

    long pressedTime = 0;
    long seconds = 0;
    int lprice;
    int displayValue = 100;
    int startValue = 1;
    String sortType = "sim";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate( R.layout.search_fragment, container, false );
        recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);
        textLowPrice = (TextView) layout.findViewById(R.id.textLowPrice);
        query = (EditText) layout.findViewById(R.id.query);
        radioGroupSort = (RadioGroup) layout.findViewById(R.id.radioGroupSort);
        radioBtnPrice = (RadioButton)layout.findViewById(R.id.radioBtnPrice);
        radioBtnSim = (RadioButton) layout.findViewById(R.id.radioBtnSim);
        radioBtnSim.setChecked(true);
        radioGroupSort.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radioBtnSim:
                        if(!query.getText().toString().isEmpty()) {
                            recyclerView.scrollToPosition(0);
                            Toast.makeText(getContext(), "검색어와 유사한 물품을 검색합니다.", Toast.LENGTH_SHORT).show();
                            sortType = "sim";
                            clearData();
                            setRetrofit(queryString);
                        }
                        break;
                    case R.id.radioBtnPrice:
                        if(!query.getText().toString().isEmpty()) {
                            recyclerView.scrollToPosition(0);
                            Toast.makeText(getContext(), "최저가 순으로 검색합니다.", Toast.LENGTH_SHORT).show();
                            sortType = "asc";
                            clearData();
                            setRetrofit(queryString);
                        }
                        break;
                }
            }
        });

        btnSearch = (ImageButton) layout.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener((View.OnClickListener) this);
        progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener((SwipeRefreshLayout.OnRefreshListener) this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!query.getText().toString().isEmpty()){
                    swipeRefreshLayout.setRefreshing(false);
                    clearData();
                    Toast.makeText(getContext(), "다시 검색합니다.", Toast.LENGTH_SHORT).show();
                    setRetrofit(queryString);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        query.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == keyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    goSearch();
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });
        requestManager = Glide.with(this);
        customLayoutManager = new CustomLayoutManager(getContext());
        itemList = new ArrayList<>();
        listTypeAdapter = new ListTypeAdapter(getContext(), itemList, requestManager);
        recyclerView.setAdapter(listTypeAdapter);
        recyclerView.setLayoutManager(customLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e("count", recyclerView.getLayoutManager().getItemCount()+"");
                Log.e("lastVisible", customLayoutManager.findLastVisibleItemPosition()+"");
                int lastVisible = customLayoutManager.findLastVisibleItemPosition();
                if(lastVisible == itemList.size()-1){
                    startValue = startValue + 100;
                    setRetrofit(queryString);
                }
            }
        });
        return layout;
    }

    private void clearData(){
        itemList.clear();
        startValue = 1;
        lprice = 2147483647;
    }

    private HttpLoggingInterceptor loggingInterceptor(){
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.e("okhttp : ", message+"");
            }
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }


    public void setRetrofit(String queryString){
        progressBar.setVisibility(View.VISIBLE);
        textLowPrice.setVisibility(View.INVISIBLE);

        if(startValue == 1) {
            itemList.clear();
            lprice = 2147483647;
        }

        OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(loggingInterceptor()).build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(NAVER_URL).client(client).addCallAdapterFactory( RxJava2CallAdapterFactory.create()).addConverterFactory( GsonConverterFactory.create()).build();

        NaverShoppingSearchService naverShoppingSearchService = retrofit.create(NaverShoppingSearchService.class);

        Observable<SearchDataList> getSearchData = naverShoppingSearchService.getSearchDataList(queryString, displayValue, startValue, sortType);
        getSearchData.observeOn( AndroidSchedulers.mainThread()).subscribeOn( Schedulers.io()).subscribe( new Observer<SearchDataList>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(SearchDataList searchDataList) {
                for(Items itemResult : searchDataList.getItems()){
                    itemResult.setTitle(itemResult.getTitle().replace("<b>", ""));
                    itemResult.setTitle(itemResult.getTitle().replace("</b>", ""));
                    if (lprice >= itemResult.getLprice()) {
                        lprice = itemResult.getLprice();
                    }
                    itemList.add(itemResult);

                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e("error data", e.getLocalizedMessage()+"");
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onComplete() {
                listTypeAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(true);
                progressBar.setVisibility(View.INVISIBLE);
                if (itemList.size() == 0) {
                    lprice = 2147483647;
                    progressBar.setVisibility(View.GONE);
                    textLowPrice.setText("검색 결과 없음");
                    Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                if (lprice != 2147483647) {
                    textLowPrice.setText(String.format("%,d", lprice) + "원");
                } else {
                    textLowPrice.setText("검색 결과 없음");
                    Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                textLowPrice.setVisibility(View.VISIBLE);
                Log.e("listItemPositon",
                        customLayoutManager.findLastCompletelyVisibleItemPosition() + "");
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }


    private boolean networkCheck(){
        boolean connect = true;
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null){
            connect = false;
        }
        return connect;
    }

    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnSearch:
                clearData();
                goSearch();
                hideKeyboard();
                break;
        }
    }


    public void goSearch(){
        //검색데이터 가져오기!!
        queryString = query.getText().toString();
        progressBar.setVisibility(View.VISIBLE);
        if(queryString.equals("")){
            Toast.makeText(getContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        } else {
            if(networkCheck()) {
                clearData();
                listTypeAdapter.notifyDataSetChanged();
                setRetrofit(queryString);
                progressBar.setVisibility(View.GONE);
            } else {
                Toast.makeText(getContext(), "인터넷에 연결되어 있지 않아 검색할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    //엔터키를 누르고 나면(혹은 상품 검색 버튼을 누르면) InputMethodManage를 통해 키보드를 숨김.
    public void hideKeyboard(){
        InputMethodManager immanager = (InputMethodManager) getActivity().getSystemService( Context.INPUT_METHOD_SERVICE);
        immanager.hideSoftInputFromWindow(query.getWindowToken(), 0);
    }

    //back버튼을 3초 내에 두 번 누르면 종료되도록 함.
    protected void onBackPressed() {

        if(pressedTime == 0){
            Toast.makeText(getContext(), "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            pressedTime = System.currentTimeMillis();
        } else {
            seconds = System.currentTimeMillis() - pressedTime;

            if ( seconds > 3000 ) {
                Toast.makeText(getContext(), "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                pressedTime = 0;
            } else {
                getActivity().finish();
            }
        }
    }

    protected boolean onCreateOptionsMenu(Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.menu_item_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.goFavorite:
                createDatabase();
                Intent favoriteIntent = new Intent(getActivity(), MainActivity.class);
                if(favoriteIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(favoriteIntent);
                } else {
                    Toast.makeText(getContext(), "다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    public void createDatabase(){
        database = getActivity().openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        DBHelper helper = new DBHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        helper.getWritableDatabase();
    }

    public void onRefresh() {
        swipeRefreshLayout.setEnabled(false);
        goSearch();
    }
}
