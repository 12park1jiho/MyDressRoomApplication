package jiho.mydressroom.org.mydressroomapplication.Util;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by XPS on 2017-12-02.
 */

public interface NaverShoppingSearchService {
    @Headers({
            "X-Naver-Client-Id: 66duNoVAmQTLfIF3g6Fy",
            "X-Naver-Client-Secret: WMZ0g6Sz6Z"
    })
    @GET("shop")
    Observable<SearchDataList> getSearchDataList(@Query("query") String queryKey, @Query("display") int displayValue, @Query("start") int start, @Query("sort") String sortType);
}
