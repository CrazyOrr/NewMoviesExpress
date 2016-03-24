package com.github.crazyorr.newmoviesexpress.util;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by wanglei02 on 2016/3/18.
 */
public class RxBus {

    private static RxBus mInstance;
    //    private final Subject<Object, Object> _bus = new SerializedSubject<>(ReplaySubject.create());
//    private final Subject<Object, Object> _bus = new SerializedSubject<>(BehaviorSubject.create());
    // If multiple threads are going to emit events to this
    // then it must be made thread-safe like this instead
    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());

    public static RxBus getInstance() {
        if (mInstance == null) {
            mInstance = new RxBus();
        }
        return mInstance;
    }

    public void send(Object o) {
        _bus.onNext(o);
    }

    public Observable<Object> toObserverable() {
        return _bus;
    }

    public boolean hasObservers() {
        return _bus.hasObservers();
    }
}