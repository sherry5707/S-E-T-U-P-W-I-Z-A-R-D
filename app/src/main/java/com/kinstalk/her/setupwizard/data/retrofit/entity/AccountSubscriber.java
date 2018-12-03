package com.kinstalk.her.setupwizard.data.retrofit.entity;

import rx.Subscriber;

public abstract class AccountSubscriber extends Subscriber<AccountResponse> {

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
    public void onNext(AccountResponse t) {
        this.resultSuccess(t);
    }

    public abstract void resultSuccess(AccountResponse t);

    public abstract void resultError(Throwable e);
}
