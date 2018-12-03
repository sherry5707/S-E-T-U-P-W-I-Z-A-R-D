package com.kinstalk.her.setupwizard.data.retrofit.entity;

import rx.Subscriber;

public abstract class UnregisterSubscriber extends Subscriber<UnRegisterResponse> {

    @Override
    public void onCompleted() {
    }

    public void onError(Throwable e) {
        if (e != null) {
            e.printStackTrace();
            if (e.getMessage() == null) {
                this.resultError(new Throwable(e.toString()));
            } else {
                this.resultError(new Throwable(e.getMessage()));
            }
        } else {
            this.resultError(new Exception("null message"));
        }

    }

    @Override
    public void onNext(UnRegisterResponse t) {
        this.resultSuccess(t);
    }

    public abstract void resultSuccess(UnRegisterResponse t);

    public abstract void resultError(Throwable e);
}
