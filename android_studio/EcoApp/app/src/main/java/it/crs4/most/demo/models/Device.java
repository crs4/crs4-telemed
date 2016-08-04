package it.crs4.most.demo.models;

import java.io.Serializable;

public class Device implements Serializable {

    private static final long serialVersionUID = -7284527758558743253L;
    private String mStreamUri = null;
    private String mShotUri = null;
    private String mWebUri = null;
    private String mPtzUri;
    private String mName;
    private String mUser;
    private String mPwd;

    public Device(String name, String streamUri, String shotUri, String webUri, String ptzUri, String user, String pwd) {
        mName = name;
        mStreamUri = streamUri;
        mShotUri = shotUri;
        mWebUri = webUri;
        mPtzUri = ptzUri;
        mUser = user;
        mPwd = pwd;
    }

    public String getName() {
        return mName;
    }

    public String getStreamUri() {
        return mStreamUri;
    }

    public String getShotUri() {
        return mShotUri;
    }

    public String getWebUri() {
        return mWebUri;
    }

    public String getPtzUri() {
        return mPtzUri;
    }

    public String getUser() {
        return mUser;
    }

    public String getPwd() {
        return mPwd;
    }

    public String toString() {
        return String.format("[Device:%s\nStream: %s\nShot: %s\n Web: %s\n PTZ: %s]", mName,
                mStreamUri, mShotUri, mWebUri, mPtzUri);
    }
}
