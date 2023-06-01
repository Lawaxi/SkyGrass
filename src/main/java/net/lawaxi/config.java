package net.lawaxi;

import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.models.UP;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class config {
    private final Setting setting;
    private final List<UP> ups = new ArrayList<>();

    public config(File file) {
        if (!file.exists()) {
            FileUtil.touch(file);
            Setting setting = new Setting(file, StandardCharsets.UTF_8, false);
            setting.setByGroup("uid", "skygrass", "391445");
            setting.setByGroup("roomid", "skygrass", "872188");
            setting.setByGroup("kickInPast", "skygrass", "true");
            setting.setByGroup("joinMultiGroupByOneUid", "skygrass", "false");
            setting.setByGroup("qq_yyh", "skygrass", "");
            setting.setByGroup("uid_yyh", "skygrass", "");
            setting.setByGroup("cookie", "skygrass", "");
            setting.store();
        }
        this.setting = new Setting(file, StandardCharsets.UTF_8, false);
        SkyGrass.INSTANCE.getLogger().info("【SkyGrass】读取总配置");
        init();
    }

    private void init() {
        for (String g : setting.getGroups()) {
            ups.add(new UP(
                    g,
                    setting.getStr("uid", g, ""),
                    setting.getStr("roomid", g, ""),
                    setting.getBool("kickInPast", g, true),
                    setting.getBool("joinMultiGroupByOneUid", g, false),
                    setting.getStr("qq_yyh", g, ""),
                    setting.getStr("uid_yyh", g, ""),
                    setting.getStr("cookie", g, ""),
                    this
            ));
        }
    }

    public List<UP> getUps() {
        return ups;
    }

    public UP getParticularUp(long group_id) {
        for (UP up : ups) {
            for (String g : up.data.groups) {
                if (g.equals("" + group_id))
                    return up;
            }
        }
        return null;
    }

    public UP getParticularUpByYYH(long qqid) {
        for (UP up : ups) {
            if (up.qq_yyh.equals("" + qqid))
                return up;
        }
        return null;

    }

    public Setting getSetting() {
        return setting;
    }
}
