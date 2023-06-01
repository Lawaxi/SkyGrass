package net.lawaxi.models;

import net.lawaxi.config;
import net.lawaxi.data;
import net.lawaxi.handel.BiliHandel;

public class UP {

    public final String name;
    public final String uid;
    public final String roomid;
    public boolean kickInPast;
    public boolean joinMultiGroupByOneUid;
    public String qq_yyh;
    public String uid_yyh;
    public String cookie;
    private final config config;
    public final data data;
    public boolean onTypingCookie = false;

    public UP(String name, String uid, String roomid, boolean kickInPast, boolean joinMultiGroupByOneUid, String qq_yyh, String uid_yyh, String cookie, config config) {
        this.name = name;
        this.uid = uid;
        this.roomid = roomid;
        this.kickInPast = kickInPast;
        this.joinMultiGroupByOneUid = joinMultiGroupByOneUid;
        this.qq_yyh = qq_yyh;
        this.uid_yyh = uid_yyh;
        this.cookie = cookie;
        this.config = config;
        this.data = new data(this);
    }

    public boolean switchKIP(boolean save_to_config) {
        this.kickInPast = !this.kickInPast;
        config.getSetting().setByGroup("kickInPast", name, String.valueOf(this.kickInPast));
        if (save_to_config) {
            config.getSetting().store();
        }
        return this.kickInPast;
    }

    public boolean switchJMG(boolean save_to_config) {
        this.joinMultiGroupByOneUid = !this.joinMultiGroupByOneUid;
        config.getSetting().setByGroup("joinMultiGroupByOneUid", name, String.valueOf(this.joinMultiGroupByOneUid));
        if (save_to_config) {
            config.getSetting().store();
        }
        return this.joinMultiGroupByOneUid;
    }

    public void changeQq_yyh(String qq_yyh, boolean save_to_config) {
        this.qq_yyh = qq_yyh;
        config.getSetting().setByGroup("qq_yyh", name, this.qq_yyh);
        if (save_to_config) {
            config.getSetting().store();
        }
    }

    public void changeUid_yyh(String uid_yyh, boolean save_to_config) {
        this.uid_yyh = uid_yyh;
        config.getSetting().setByGroup("uid_yyh", name, this.uid_yyh);
        if (save_to_config) {
            config.getSetting().store();
        }
    }

    public boolean changeCookie(String cookie, boolean save_to_config) {
        this.cookie = cookie;
        config.getSetting().setByGroup("cookie", name, this.cookie);
        if (save_to_config) {
            config.getSetting().store();
        }
        return true;
    }

    public String getQQIDFromMessage(String uid) {
        return BiliHandel.getLatestMessage(this, uid);
    }
}
