package com.kinstalk.her.setupwizard.data.retrofit.entity;

public class AccountResponse {

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int code;
    public String msg;
    public UserEntity user;
    public TokenEntity token;


    public class UserEntity {
        int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        String mobile;
        String nickname;
    }

    public class TokenEntity {
        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(String expires_in) {
            this.expires_in = expires_in;
        }

        public String getRefresh_token() {
            return refresh_token;
        }

        public void setRefresh_token(String refresh_token) {
            this.refresh_token = refresh_token;
        }

        public String getRe_expires_in() {
            return re_expires_in;
        }

        public void setRe_expires_in(String re_expires_in) {
            this.re_expires_in = re_expires_in;
        }

        String access_token;
        String expires_in;
        String refresh_token;
        String re_expires_in;
    }
}
