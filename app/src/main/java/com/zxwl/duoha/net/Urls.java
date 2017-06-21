package com.zxwl.duoha.net;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * author：hw
 * data:2017/4/20 11:25
 * ClassName: ${Class_Name}
 */

public class Urls {

    public static final String baseUrl = "";

    private void setTime() {
        Observable.timer(3, TimeUnit.SECONDS)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.i("", aLong + "");
                    }
                });
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                for (int i = 0; i < 10; i++) {
                    subscriber.onNext("1");
                }
            }
        }).filter(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String s) {
                return s.contains("s");
            }
        }).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {

            }
        });
    }
}
