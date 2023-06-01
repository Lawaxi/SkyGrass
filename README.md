# SkyGrass：舰长群自动控制机器人

> 原本可能能被天草舰长群用上的机器人（由此得名，从此与她无关啦T_T）
> 因为day头觉得我不安全吧，就算了

Mirai-Console插件，构建mirai（[mamoe/mirai](https://github.com/mamoe/mirai), [docs.mirai.mamoe.net](https://docs.mirai.mamoe.net/)）后拖入plugins文件夹运行即可，首次运行生成配置，至少填写应援会管理QQ后重启即可正常使用。

### 功能

1. 多个UP主
2. 多个舰长群（受人数限制）并存的情况
3. 入群自动审批
    - 双重认证：舰长在B站私信应援会自己的QQ号&入群申请填写B站uid
        - 需要应援会准备一个B站账号，登录网页版后将Cookie提交机器人用于读私信
    - 不允许同一B站账号进群多个QQ
    - （可选）不允许同一B站账号进多个群（无论是否用同一QQ）
4. 每日8点更新舰长名单
5. （可选）每日8点踢出已过期舰长QQ

### 操作

1. 配置：config.setting
    - 一个机器人可用于多个UP主舰长群的控制
    - [xxx]为一个UP主，启动时也会根据此配置生成每个UP主舰长群的数据文件data_xxx.setting
        - 需要手动填写qq_yyh（群管QQ号），将机器人拉入舰长群设为管理，之后可通过群管QQ号私信机器人/help实现自助配置
        - `[skygrass]
          uid = 391445
          roomid = 872188
          kickInPast = true
          joinMultiGroupByOneUid = false
          qq_yyh =
          uid_yyh =
          cookie = `
2. 数据：data_xxx.setting
    - 数据文件不需要手动填，可通过群管QQ号私信添加/删除新群。groups中的群顺序与uids中的uid-qq号对应关系顺序一致，请不要随意手动修改
    - `[]`
      `groups = 1234,5678,...`
      `uids = ["{"uid1":"qqid1",...}"]`

### 源码

- handel/BiliHandel：B站私信读取的方法
- handel/BiliLiveHandel：B站直播舰长列表获取的方法
- models/UP
    - data
- config
- SkyGrass
    - listener

