package com.liyu.suzhoubus.ui.bus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.baidu.location.BDLocation;
import com.liyu.suzhoubus.R;
import com.liyu.suzhoubus.http.ApiFactory;
import com.liyu.suzhoubus.http.BaseBusResponse;
import com.liyu.suzhoubus.http.api.BusController;
import com.liyu.suzhoubus.location.RxLocation;
import com.liyu.suzhoubus.model.BusLineStation;
import com.liyu.suzhoubus.ui.base.BaseActivity;
import com.liyu.suzhoubus.ui.bus.adapter.LineStationAdapter;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by liyu on 2016/11/1.
 */

public class StationDetailActivity extends BaseActivity {

    public static final String KEY_EXTRA_CODE = "CODE";
    public static final String KEY_EXTRA_NAME = "NAME";

    private RecyclerView recyclerView;
    private LineStationAdapter adapter;

    public static void start(Context context, String name, String code) {
        Intent intent = new Intent(context, StationDetailActivity.class);
        intent.putExtra(KEY_EXTRA_NAME, name);
        intent.putExtra(KEY_EXTRA_CODE, code);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_station_detail;
    }

    @Override
    protected int getMenuId() {
        return 0;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra(KEY_EXTRA_NAME));
        recyclerView = (RecyclerView) findViewById(R.id.rv_station_line);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LineStationAdapter(R.layout.item_bus_line, null);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void loadData() {
        final String code = getIntent().getStringExtra(KEY_EXTRA_CODE);
        RxLocation
                .get()
                .locate(this)
                .flatMap(new Func1<BDLocation, Observable<BaseBusResponse<BusLineStation>>>() {
                    @Override
                    public Observable<BaseBusResponse<BusLineStation>> call(BDLocation bdLocation) {
                        Map<String, String> options = new HashMap<>();
                        options.put("NoteGuid", code);
                        options.put("uid", BusController.uid);
                        options.put("DeviceID", BusController.deviceID);
                        options.put("sign", BusController.sign);
                        options.put("lat", String.valueOf(bdLocation.getLatitude()));
                        options.put("lng", String.valueOf(bdLocation.getLongitude()));
                        return ApiFactory.getBusController().getStationInfo(options).subscribeOn(Schedulers.io());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseBusResponse<BusLineStation>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseBusResponse<BusLineStation> listBaseBusResponse) {
                        adapter.setNewData(listBaseBusResponse.data.getList());
                    }
                });
    }
}