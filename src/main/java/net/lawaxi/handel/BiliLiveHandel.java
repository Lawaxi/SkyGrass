package net.lawaxi.handel;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.List;

public class BiliLiveHandel {

    public static final String API = "https://api.live.bilibili.com/xlive/app-room/v2/guardTab/topList?roomid=#roomid#&page=#page#&ruid=#uid#";

    public static String[] getMembers(String uid, String room_id) {
        int page = 1;
        int total_page = 1;
        List<String> m = new ArrayList<>();
        while (page <= total_page) {
            try {
                String response = HttpRequest.get(API.replace("#roomid#", room_id).replace("#page#", "" + page).replace("#uid#", uid)).execute().body();
                JSONObject response_o = JSONUtil.parseObj(response);
                if (response_o.getInt("code") == 0) {
                    JSONObject data_o = response_o.getJSONObject("data");
                    //首次
                    if (page == 1) {
                        JSONObject info_o = data_o.getJSONObject("info");
                        total_page = info_o.getInt("page");

                        addMember(m, data_o.getJSONArray("top3"));
                    }

                    addMember(m, data_o.getJSONArray("list"));
                    page++;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return m.toArray(new String[0]);
    }

    private static void addMember(List<String> m, JSONArray members) {
        for (Object member : members) {
            m.add(JSONUtil.parseObj(member).getStr("uid"));
        }
    }
}
