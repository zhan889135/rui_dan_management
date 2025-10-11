删除target文件：
在 IDEA 的 Terminal（终端） 里进入目录执行： git rm -r --cached target，然后提交代码


推送连接超时：多试试就好了

设置代理：
git config --global http.proxy 'proxy-url'
git config --global https.proxy 'proxy-url'

删除代理：
git config --global --unset http.proxy
git config --global --unset https.proxy