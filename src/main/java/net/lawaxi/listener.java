package net.lawaxi;

import net.lawaxi.models.UP;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;

public class listener extends SimpleListenerHost {

    @EventHandler()
    public ListeningStatus onJoinRequest(MemberJoinRequestEvent event) {
        Long groupid = event.getGroupId();
        UP up = SkyGrass.config.getParticularUp(groupid);
        if (up != null) {//是舰长群

            String uid_request = event.getMessage();
            if (uid_request == null) {
                event.reject(false, "请在b站私信应援会QQ号，并在入群申请填入uid（都不要附加任何无关文字）");
            } else if (!up.data.isMember(uid_request)) {
                event.reject(false, "该uid非舰长");
            } else {
                //双重核实之b站
                String id = up.getQQIDFromMessage(uid_request);
                Long qqid = event.getFromId();
                if (id.equals("unlogined")) {
                    //应援会账号消息获取出错，
                    NormalMember yyh = event.getGroup().get(Long.valueOf(up.qq_yyh));
                    if (yyh != null) {
                        yyh.sendMessage("在处理新申请时出现问题，cookie可能过期需要更新");
                        //未经调试。如果cookie没问题是b站限制获取的原因，可以改成死循环外套try的办法加大获取强度
                    }
                } else if (String.valueOf(qqid).equals(id)) {
                    //同一个uid只能用于一个qq号
                    switch (up.data.addUid("" + groupid, "" + qqid, uid_request, true)) {
                        case -1:
                            event.reject(false, "未知错误，请联系管理后重新申请");
                            break;
                        case 0:
                            event.accept();
                        case 1:
                            event.reject(false, "此uid对应了一个已经加群的QQ");
                        case 2:
                            event.reject(false, "此uid对应了一个已经加其他群的QQ");
                    }

                } else {
                    event.reject(false, "请在b站私信应援会QQ号以便核实（不要附加任何无关文字）");
                }
            }
        }

        return ListeningStatus.LISTENING;
    }

/* 不统计离开了，每天八点清理即可
    @EventHandler()
    public ListeningStatus onLeave(MemberLeaveEvent event) {
        return ListeningStatus.LISTENING;
    }*/


    @EventHandler()
    public ListeningStatus onUserMessage(UserMessageEvent event) {
        User sender = event.getSender();
        String message = event.getMessage().contentToString();

        UP u = SkyGrass.config.getParticularUpByYYH(sender.getId());
        if (u != null) {
            if (u.onTypingCookie) {
                u.onTypingCookie = false;
                u.changeCookie(message, true);
                sender.sendMessage(u.name + "应援会账号Cookie更改为：" + message);
            } else {
                if (message.startsWith("/")) {
                    String[] args = message.split(" ");
                    try {
                        switch (args[0]) {
                            case "/addg": {
                                Long group = Long.parseLong(args[1]);
                                //权限检查
                                Group g = event.getBot().getGroup(group);
                                if (g == null)
                                    sender.sendMessage("机器人不在此群中");
                                else {
                                    NormalMember yyh = g.get(sender.getId());
                                    if (yyh == null)
                                        sender.sendMessage("您不在此群中");
                                    else if (!(yyh.getPermission() == MemberPermission.OWNER ||
                                            yyh.getPermission() == MemberPermission.ADMINISTRATOR))
                                        sender.sendMessage("您不是此群的管理员");
                                    else if (!u.data.addGroup("" + group))
                                        sender.sendMessage("此群已经添加");
                                    else
                                        sender.sendMessage("添加群" + group);
                                }
                                break;
                            }
                            case "/rmg": {
                                String group = "" + Long.parseLong(args[1]);
                                if (u.data.rmGroup(group))
                                    sender.sendMessage("删除群" + group);
                                else
                                    sender.sendMessage("未添加过此群");
                                break;
                            }
                            case "/transfer": {
                                String id = String.valueOf(Long.parseLong(args[1]));
                                u.changeQq_yyh(id, true);
                                sender.sendMessage("转让" + u.name + "机器人私聊管理权限给" + id);
                                break;
                            }
                            case "/cookie": {
                                u.onTypingCookie = true;
                                sender.sendMessage("请输入" + u.name + "应援会账号cookie");
                                break;
                            }
                            case "/uid": {
                                String id = String.valueOf(Long.parseLong(args[1]));
                                u.changeUid_yyh(id, true);
                                sender.sendMessage(u.name + "应援会账号uid更改为：" + id);
                                break;
                            }
                            case "/kickInPast":
                            case "/kip": {
                                sender.sendMessage("kickInPast切换至" + u.switchKIP(true));
                            }
                            case "/joinMultiGroup":
                            case "/jmp": {
                                sender.sendMessage("joinMultiGroup切换至" + u.switchJMG(true));
                            }
                            default: {
                                sender.sendMessage("[帮助]\n" +
                                        "添加新舰长群：/addg <群id>\n" +
                                        "删除舰长群：/rmg <群id>\n" +
                                        "切换是否每日8点踢人（当前" + u.kickInPast + "）：/kip\n" +
                                        "切换是否单uid加多群（当前" + u.joinMultiGroupByOneUid + "）：/jmp\n" +
                                        "提交应援会账号uid：/uid <uid>\n" +
                                        "提交应援会账号cookie：发送\"/cookie\"之后将cookie单独一条发送\n" +
                                        "转让" + u.name + "机器人私聊管理权限：/transfer <qqid>\n" +
                                        "每一条指令成功都会有回复，如出现问题请联系我");
                                break;
                            }
                        }
                    } catch (Exception e) {
                        sender.sendMessage("请按格式填写，输入/help查看");
                    }
                }

            }
        }

        return ListeningStatus.LISTENING;
    }
}
