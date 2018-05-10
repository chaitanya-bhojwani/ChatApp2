package com.example.affine.chatapp2;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DataManager {
    private ApiService apiService;

    DataManager(ApiService apiService) {
        this.apiService = apiService;
    }


    public Single<Response<List<HistoryResponseItem>>> getHistory(String topic) {
        return apiService.getHistory(topic).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.computation());
    }

}
