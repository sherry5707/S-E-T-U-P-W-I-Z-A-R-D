package com.kinstalk.her.setupwizard.data.retrofit;

import com.kinstalk.her.setupwizard.HerSetupWizardApplication;
import com.kinstalk.her.setupwizard.data.retrofit.entity.AccountResponse;
import com.kinstalk.her.setupwizard.data.retrofit.entity.UnRegisterResponse;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by mamingzhang on 2017/4/21.
 */

public interface HerApiService {
    //String BASE_URL = DomainHelper.getInstance(HerSetupWizardApplication.getApplication()).getRequestUrl();
    String BASE_URL = HerSetupWizardApplication.getBaseURL();

    @FormUrlEncoded
    @POST("/user/register")
    Observable<AccountResponse> createJiaYuanUser(@Field("tinyid") String tinyid, @Field("din") String din, @Field("sn") String serialNo, @Field("devicetype") int devicetype, @Field("nickname") String nickname, @Field("version") String version);

    @FormUrlEncoded
    @POST("/user/unbind")
    Observable<UnRegisterResponse> unRegisterJiaYuanUser(@Field("tinyid") String tinyid, @Field("sn") String serialNo);
}
