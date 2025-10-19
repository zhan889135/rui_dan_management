删除target文件：
在 IDEA 的 Terminal（终端） 里进入目录执行： git rm -r --cached target，然后提交代码


推送连接超时：多试试就好了

设置代理：
git config --global http.proxy 'proxy-url'
git config --global https.proxy 'proxy-url'

删除代理：
git config --global --unset http.proxy
git config --global --unset https.proxy

拉取代码：git clone https://github.com/zhan889135/rui_dan_management.git


Ip地址查询：https://ip.cn/

119.117.245.115/32
113.228.122.102/32
/32 表示 精确到单个 IP 地址（只允许这一台设备访问）；
119.117.245.115 是你当前电脑访问互联网时的 公网出口 IP；

119.117.245.115/32

http://36.213.0.185:8090/

账号：
admin 123456
luanxiong 123456
xuanxuan1 123456


批量更新create_name字段
UPDATE tb_interview_feedback f
LEFT JOIN sys_user u ON f.create_by = u.user_name
SET f.create_name = u.nick_name
where f.del_flag = 'N' and u.del_flag = 'N'; 


UPDATE tb_interview_report f
LEFT JOIN sys_user u ON f.create_by = u.user_name
SET f.create_name = u.nick_name
where f.del_flag = 'N' and u.del_flag = 'N'; 

