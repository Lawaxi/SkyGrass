package net.lawaxi;

import cn.hutool.cron.CronUtil;
import cn.hutool.json.JSONUtil;
import net.lawaxi.models.UP;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.GlobalEventChannel;

import java.util.Map;

public final class SkyGrass extends JavaPlugin {
    public static final SkyGrass INSTANCE = new SkyGrass();
    public static config config;

    private SkyGrass() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.skygrass", "0.1.1")
                .name("SkyGrass")
                .author("delay0delay")
                .build());
    }

    @Override
    public void onEnable() {
        //配置和数据
        config = new config(resolveConfigFile("config.setting"));

        //加群申请
        GlobalEventChannel.INSTANCE.registerListenerHost(new listener());

        //每日8点
        for (Bot b : Bot.getInstances()) {
            CronUtil.schedule("0 0 8 * * ? ", new Runnable() {
                @Override
                public void run() {
                    //更新舰长列表并踢人
                    for (UP up : config.getUps()) {
                        data d = up.data;
                        int amount = d.updateMembers();
                        if (amount == -1) {
                            getLogger().info("【SkyGrass】" + "读取舰长出现错误，今日计划取消");
                            return;
                        }

                        for (int i = 0; i < d.groups.length; i++) {
                            String group = d.groups[i];
                            if (group.equals("removed"))
                                continue;
                            Long id = Long.valueOf(group);
                            if (id == null)//不是long格式
                                continue;
                            Group g = b.getGroup(id);
                            if (g == null)//不在群
                                continue;

                            NormalMember yyh = g.get(Long.parseLong(up.qq_yyh));

                            try {
                                //清理
                                int count = 0;
                                if (up.kickInPast) {
                                    if (!(g.getBotPermission() == MemberPermission.ADMINISTRATOR ||
                                            g.getBotPermission() == MemberPermission.OWNER) && yyh != null) {
                                        yyh.sendMessage("请给机器人在" + g.getName() + "群管理权限");
                                    } else {
                                        for (Member m : g.getMembers()) {
                                            if (m instanceof NormalMember) {
                                                //管理员不考虑
                                                if (m.getPermission() == MemberPermission.ADMINISTRATOR ||
                                                        m.getPermission() == MemberPermission.OWNER) {
                                                    continue;
                                                }

                                                if (!d.isMember(group, "" + m.getId())) {
                                                    ((NormalMember) m).kick("您的舰长已过期，暂时移除群聊");
                                                    up.data.rmUidByQQId(group, "" + m.getId(), false);
                                                    count++;
                                                }
                                            }
                                        }
                                    }
                                }

                                //报告结果
                                if (yyh != null) {
                                    yyh.sendMessage("本日数据更新，舰长共" + amount + "个，群" + g.getName() + "清理非舰长成员" + count + "个");
                                }

                                //反向清理数据方便其他人入群
                                count = 0;
                                for (Map.Entry<String, Object> m : JSONUtil.parseObj(d.uids[i]).entrySet()) {
                                    String uid = m.getKey();
                                    Long qqid = Long.parseLong((String) m.getValue());
                                    if (g.getOrFail(qqid) == null) {
                                        d.rmUid(group, uid, false);
                                        count++;
                                    }
                                }

                                getLogger().info("【SkyGrass】" + up.name + "：" + i + "号群任务完成，列表清理" + count + "人");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        d.saveUids();
                    }
                }
            });

        }
        getLogger().info("【SkyGrass】Plugin loaded!");
    }
}