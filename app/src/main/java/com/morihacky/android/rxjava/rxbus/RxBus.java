package com.morihacky.android.rxjava.rxbus;


import com.jakewharton.rxrelay3.PublishRelay;
import com.jakewharton.rxrelay3.Relay;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

/**
 * courtesy: <a href="https://gist.github.com/benjchristensen/04eef9ca0851f3a5d7bf">...</a>
 */
public class RxBus {

    private final Relay<Object> _bus = PublishRelay.create().toSerialized();

    public void send(Object o) {
        _bus.accept(o);
    }

    public Flowable<Object> asFlowable() {
        return _bus.toFlowable(BackpressureStrategy.LATEST);
    }

    public boolean hasObservers() {
        return _bus.hasObservers();
    }
}
