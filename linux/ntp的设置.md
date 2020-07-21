1. 检查 ntpd 服务是否开启 

```
 service ntpd status
```

2. 开启ntpd服务，并保证开机启动

```
service ntpd start
chkconfig ntpd on
```

3. 配置本地集群时间同步

```
vi /etc/ntp.conf
   放开并修改成自己的网段
   # Hosts on local network are less restricted.
   restrict 192.168.47.0 mask 255.255.255.0 nomodify notrap  
   内网环境下，注释以下设置 
   # Use public servers from the pool.ntp.org project.
   # Please consider joining the pool (http://www.pool.ntp.org/join.html).
   #server 0.centos.pool.ntp.org iburst
   #server 1.centos.pool.ntp.org iburst
   #server 2.centos.pool.ntp.org iburst
   #server 3.centos.pool.ntp.org iburst
   ntp server提供的本地服务
   server  127.127.1.0     # local clock
   fudge   127.127.1.0 stratum 10
```

4. 重启ntpd服务

```
 service ntpd restart
```

![点击并拖拽以移动](data:image/gif;base64,R0lGODlhAQABAPABAP///wAAACH5BAEKAAAALAAAAAABAAEAAAICRAEAOw==)

# 定时同步

```
在完全分布式hadoop集群中，需要保证每个节点的时间可以同步

1. 检查是否安装 ntp
    rpm -q ntp

2. 检查 ntpd 是否运行
   service ntpd start
   chkconfig ntpd on  # 开机启动
   chkconfig --list ntpd # 是否在开机启动列表中

3. vi /etc/ntp.conf （file:///C:/Users/winshy/Documents/My%20Knowledge/temp/79e76ebf-8d86-4042-b030-48b53abf2ed6/128/index_files/a19ba8df-66d4-47d5-855a-afcf8da3c8f4.png）
   第一处：restrict, 添加网段 192.168.217.*
   rict 192.168.1.0 mask 255.255.255.0 nomodify notrap
   restrict 192.168.47.0 mask 255.255.255.0 nomodify notrap

   第二处：server， 屏蔽
   # Please consider joining the pool (http://www.pool.ntp.org/join.html).
   #server 0.centos.pool.ntp.org iburst
   #server 1.centos.pool.ntp.org iburst
   #server 2.centos.pool.ntp.org iburst
   #server 3.centos.pool.ntp.org iburst

   第三处：将原有注释去掉，是当服务器与公用的时间服务器失去联系时以本地时间为客户端提供时间服务
   server 127.127.1.0
   fudge  127.127.1.0 stratum 10

   #重新启动ntpd
   service ntpd restart

4. crontab 设置任务，定时同步时间
   crontab -e
   ## sync time
   # min hour day month week task
     0-59 10 * * *  /usr/sbin/ntpdate bigdata.001.com
```
