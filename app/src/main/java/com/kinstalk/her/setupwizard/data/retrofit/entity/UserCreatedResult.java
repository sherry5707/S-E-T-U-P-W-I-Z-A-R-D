package com.kinstalk.her.setupwizard.data.retrofit.entity;

/**
 * Created by mamingzhang on 2017/4/21.
 */

public class UserCreatedResult {

    /**
     * uid : 10001
     * userCode : 86*****
     * accessToken : Ajldsfsdjfsdf
     * accessExpiresIn : 123234243
     * refreshToken : asdFnglfdsljfs
     * refreshExpiresIn : 123234243
     * pwd : 默认登录密码
     */

    private long uid;
    private String userCode;
    private String accessToken;
    private long accessExpiresIn;
    private String refreshToken;
    private long refreshExpiresIn;
    private String pwd;
    private String duduAppId;
    private String duduVoipAccount;
    private String duduVoipPwd;
    private String merchantId;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getDuduAppId() {
        return duduAppId;
    }

    public void setDuduAppId(String duduAppId) {
        this.duduAppId = duduAppId;
    }

    public String getDuduVoipAccount() {
        return duduVoipAccount;
    }

    public void setDuduVoipAccount(String duduVoipAccount) {
        this.duduVoipAccount = duduVoipAccount;
    }

    public String getDuduVoipPwd() {
        return duduVoipPwd;
    }

    public void setDuduVoipPwd(String duduVoipPwd) {
        this.duduVoipPwd = duduVoipPwd;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getAccessExpiresIn() {
        return accessExpiresIn;
    }

    public void setAccessExpiresIn(long accessExpiresIn) {
        this.accessExpiresIn = accessExpiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(long refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}
