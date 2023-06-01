package net.lawaxi;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.handel.BiliLiveHandel;
import net.lawaxi.models.UP;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class data {
    private final UP up;
    private final Setting setting;
    public String[] groups;
    public Object[] uids; //key为uid，value为qqid
    public String[] members;

    public data(UP up) {
        this(SkyGrass.INSTANCE.resolveConfigFile("data_" + up.name + ".setting"), up);
    }

    private data(File file, UP up) {
        this.up = up;

        if (!file.exists()) {
            FileUtil.touch(file);
            Setting setting = new Setting(file, StandardCharsets.UTF_8, false);
            setting.set("groups", ""); //多个群
            setting.set("uids", "[\"{}\"]"); //qq号与uid对应
            setting.store();
        }
        this.setting = new Setting(file, StandardCharsets.UTF_8, false);
        SkyGrass.INSTANCE.getLogger().info("【SkyGrass】读取" + up.name);
        init();
    }

    private void init() {
        groups = setting.getStrings("groups");
        uids = JSONUtil.parseArray(setting.getStr("uids")).stream().toArray();
        updateMembers();
    }

    public int updateMembers() {
        try {
            members = BiliLiveHandel.getMembers(up.uid, up.roomid);
            int amount = members.length;
            SkyGrass.INSTANCE.getLogger().info("【SkyGrass】" + up.name + "舰长名单更新：共" + amount + "个");
            return amount;

        } catch (Exception e) {
            return -1;
        }
    }

    public int getGroupIndex(String group) {
        for (int i = 0; i < this.groups.length; i++) {
            if (this.groups[i].equals(group))
                return i;
        }
        return -1;
    }

    private void fillUids() {
        //补全空缺使序号能对上
        for (int i = this.uids.length; i < this.groups.length; i++) {
            this.uids[i] = new JSONObject();
        }
    }

    public boolean isMember(String group, String qqid) {
        String uid = getUidByQQid(group, qqid);
        if (uid != null) {
            return isMember(uid);
        }
        return false;
    }

    public String getUidByQQid(String group, String qqid) {
        int g = getGroupIndex(group);
        if (g != -1) {
            JSONObject groupUids_o = JSONUtil.parseObj(this.uids[g]);
            for (Map.Entry<String, Object> entry : groupUids_o.entrySet()) {
                if (qqid.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public boolean isMember(String uid) {
        for (String member : members) {
            if (member.equals(uid))
                return true;
        }
        return false;
    }

    //数据修改相关

    public int addUid(String group, String qqid, String uid, boolean save_to_data) {
        int g = getGroupIndex(group);
        if (g == -1)
            return -1;//应该不会遇到的错误

        fillUids();
        JSONObject groupUids_o = JSONUtil.parseObj(this.uids[g]);
        if (groupUids_o.containsKey(uid))
            return 1;//此uid已有qq号在群

        if (!up.joinMultiGroupByOneUid) {
            for (int i = 0; i < this.groups.length; i++) {
                if (!this.groups[i].equals("removed")) { //废弃的群
                    JSONObject groupUids1_o = JSONUtil.parseObj(this.uids[i]);
                    if (groupUids1_o.containsKey(uid))
                        return 2; //此uid已有qq号在其他群
                }
            }
        }

        this.uids[g] = groupUids_o.set(uid, qqid);
        if (save_to_data) {
            saveUids();
        }
        return 0; //成功
    }

    public int rmUid(String group, String uid, boolean save_to_data) {
        int g = getGroupIndex(group);
        if (g != -1) {
            JSONObject groupUids_o = JSONUtil.parseObj(this.uids[g]);
            this.uids[g] = groupUids_o.remove(uid);
            if (save_to_data) {
                saveUids();
            }
            return 0;
        }
        return -1;
    }

    public int rmUidByQQId(String group, String qqID, boolean save_to_data) {
        String uid = getUidByQQid(group, qqID);
        if (uid != null)
            return rmUid(group, uid, save_to_data);
        return -1;
    }

    public boolean addGroup(String group) {
        int g = getGroupIndex(group);
        if (g == -1) {
            this.groups[groups.length] = group;
            saveGroups();
            return true;
        }
        return false;
    }

    public boolean rmGroup(String group) {
        int g = getGroupIndex(group);
        if (g != -1) {
            this.groups[g] = "removed";
            saveGroups();
            return true;
        }
        return false;
    }

    public void saveGroups() {
        setting.set("groups", ArrayUtil.join(groups, ","));
        setting.store();
    }

    public void saveUids() {
        String a = "[";
        for (Object object : uids) {
            a += JSONUtil.parseObj(object).toString() + ",";
        }
        setting.set("uids", (a.length() > 1 ? a.substring(0, a.length() - 1) : a) + "]");
        setting.store();
    }
}
