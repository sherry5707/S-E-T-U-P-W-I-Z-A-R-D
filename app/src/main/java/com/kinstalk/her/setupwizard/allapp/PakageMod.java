package com.kinstalk.her.setupwizard.allapp;

import android.graphics.drawable.Drawable;

public class PakageMod {

    public String pakageName;
    public String appName;
    public Drawable icon;

    public PakageMod() {
        super();
    }

    public PakageMod(String pakageName, String appName, Drawable icon) {
        super();
        this.pakageName = pakageName;
        this.appName = appName;
        this.icon = icon;
    }
}
