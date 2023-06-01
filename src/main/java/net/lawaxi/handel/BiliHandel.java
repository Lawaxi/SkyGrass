package net.lawaxi.handel;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.models.UP;

import java.util.ArrayList;
import java.util.List;

public class BiliHandel {
    public static final String API = "https://api.vc.bilibili.com/svr_sync/v1/svr_sync/fetch_session_msgs?sender_device_id=1&talker_id=#uid#&session_type=1&size=20&build=0&mobi_app=web";

    public static String[] getMessages(UP yyh_source, String uid) {
        JSONArray a = getOriMessages(yyh_source, uid);
        if (a == null)
            return null;

        List<String> m = new ArrayList<>();
        for (Object message : a.stream().toArray()) {
            JSONObject message_o = JSONUtil.parseObj(message);
            JSONObject content = JSONUtil.parseObj(message_o.getStr("content"));
            if (content.containsKey("content")) {
                m.add(content.getStr("content"));
            }
        }
        return m.toArray(new String[0]);
    }

    public static String getLatestMessage(UP yyh_source, String uid) {
        JSONArray a = getOriMessages(yyh_source, uid);
        if (a == null)
            return "unlogined";

        JSONObject message_o = JSONUtil.parseObj(a.stream().toArray()[0]);
        JSONObject content = JSONUtil.parseObj(message_o.getStr("content"));
        if (content.containsKey("content")) {
            return content.getStr("content");
        } else {
            return null;
        }
    }

    public static JSONArray getOriMessages(UP yyh_source, String uid) {
        String respond = HttpRequest.get(API.replace("#uid#", uid)).header("Cookie", yyh_source.cookie).execute().body();
        JSONObject respond_o = JSONUtil.parseObj(respond);
        if (respond_o.getInt("code") != 0)
            return null;

        JSONObject data_o = respond_o.getJSONObject("data");
        return data_o.getJSONArray("messages");
    }
}
