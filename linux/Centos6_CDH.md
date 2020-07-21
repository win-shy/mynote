[toc]

平台：Centos 6



# 配置 Centos

## 修改 hostname

```
vim /etc/hosts
```

## 查看 hostname

```
hostname
```

## 关闭防火墙

```
//临时关闭
service iptables stop
//禁止开机启动
chkconfig iptables off
```

## 设置 ip

```
[root@hadoop102 ~]# vi /etc/sysconfig/network-scripts/ifcfg-eth0

DEVICE=eth0
HWADDR=00:0C:29:67:83:08
TYPE=Ethernetvi 
UUID=3e63661b-7918-4543-85ae-df73decc0e46
ONBOOT=yes
NM_CONTROLLED=yes
BOOTPROTO=static
IPADDR=192.168.100.120
NETMASK=255.255.255.0
GATEWAY=192.168.100.2
DNS1=8.8.8.8
DNS2=8.8.4.4
DNS3=114.114.114.114



#重启network
service network restart
ip addr
```

## 配置 yum

```
yum clean all
yum makecache
yum install wget
```

6. 配置 java

```
1、将jdk-8u131-linux-x64.rpm通过WinSCP上传到虚拟机中

2、安装JDK：rpm -ivh jdk-8u131-linux-x64.rpm

3、配置jdk相关的环境变量

vi /etc/profile
export JAVA_HOME=/usr/java/latest
export PATH=$PATH:$JAVA_HOME/bin
source /etc/profile

4、测试jdk安装是否成功：java -version
```



## 关闭 SELINUX

```
vim /etc/selinux/config

将 SELINUX=enforcing 改为 SELINUX=disabled
SELINUX=disabled
```



# ssh 免密登陆

## 生成公钥和私钥

```
# 三台执行相同的操作
ssh-keygen -t rsa
```

## 将公钥拷贝到要免密登录的目标机器上

```
# 三台执行相同的操作
ssh-copy-id hadoop120;ssh-copy-id hadoop121;ssh-copy-id hadoop122
```



# 安装 mysql

```
# yum来进行mysql安装
yum install -y mysql-server mysql mysql-devel

# 数据库安装成功之后，查看mysql-server的命令
rpm  -qi mysql-server

# 启动mysql服务
service mysqld start

# 开机启动
chkconfig  mysqld on

# root账号设置密码
mysqladmin -u root  password '123456'
```



## 配置

```
1. 使用 MySQL 数据库
mysql>use mysql;

2. 查询 user 表
mysql>select User, Host, Password from user;

3. 修改 user 表，把 Host 表内容修改为%
mysql>update user set host='%' where host='localhost';

4. 删除 root 用户的其他 host
mysql>
delete from user where Host='hadoop102';
delete from user where Host='127.0.0.1';
delete from user where Host='::1';

5. 刷新
mysql>flush privileges;
```



## 创建 CM 数据库

```
1）启动数据库
[root@hadoop120 ~]# mysql -uroot -p123456

2）集群监控数据库
mysql> create database amon DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

3）Hive 数据库
mysql> create database hive DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

4）Oozie 数据库
mysql> create database oozie DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

5）Hue 数据库
mysql> create database hue DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

6）关闭数据库
mysql> quit;
```



# 下载第三方依赖库

```
[root@hadoop120 ~]# yum -y install chkconfig python bind-utils psmisc libxslt zlib sqlite cyrus-sasl-plain cyrus-sasl-gssapi fuse fuse-libs redhat-lsb

[root@hadoop121 ~]# yum -y install chkconfig python bind-utils psmisc libxslt zlib sqlite cyrus-sasl-plain cyrus-sasl-gssapi fuse fuse-libs redhat-lsb

[root@hadoop122 ~]# yum -y install chkconfig python bind-utils psmisc libxslt zlib sqlite cyrus-sasl-plain cyrus-sasl-gssapi fuse fuse-libs redhat-lsb
```



# CM 安装

1. 创建/opt/module/cm 目录

```
[root@hadoop120 module]# mkdir –p /opt/module/cm
```

2. 上传 cloudera-manager-el6-cm5.16.2_x86_64.tar.gz 到 hadoop120 的/opt/software 目录，并解压到/opt/module/cm 目录

```
[root@hadoop120 software]#  tar -zxvf cloudera-manager-el6-cm5.16.2_x86_64.tar.gz -C /opt/module/cm
```

3. 分别在 hadoop120、hadoop121、hadoop122 创建用户 cloudera-scm

```
[root@hadoop120 module]# 
useradd \
--system \
--home=/opt/module/cm/cm-5.16.2/run/cloudera-scm-server \
--no-create-home \
--shell=/bin/false \
--comment "Cloudera SCM User" cloudera-scm

[root@hadoop121 module]#
useradd \
--system \
--home=/opt/module/cm/cm-5.16.2/run/cloudera-scm-server \
--no-create-home \
--shell=/bin/false \
--comment "Cloudera SCM User" cloudera-scm

[root@hadoop122 module]#
useradd \
--system \
--home=/opt/module/cm/cm-5.16.2/run/cloudera-scm-server \
--no-create-home \
--shell=/bin/false \
--comment "Cloudera SCM User" cloudera-scm
```

4. 修改 CM Agent 配置

```
[root@hadoop120  cloudera-scm-agent]#  vim /opt/module/cm/cm-5.16.2/etc/cloudera-scm-agent/config.ini

server_host=hadoop120
```

5. 配置 CM 的数据库

```
[root@hadoop120 cm]# mkdir -p /usr/share/java/

[root@hadoop120  mysql-libs]#  tar  -zxvf mysql-connector-java-5.1.27.tar.gz

[root@hadoop120  mysql-libs]#  cp /opt/software/mysql-libs/mysql-connector-java-5.1.27/mysql-connector-java-5.1.27-bin.jar /usr/share/java/

[root@hadoop120  mysql-libs]#  mv /usr/share/java/mysql-connector-java-5.1.27-bin.jar /usr/share/java/mysql-connector-java.jar
```

6. 使用 CM 自带的脚本，在 MySQL 中创建 CM 库

```
/opt/module/cm/cm-5.16.2/share/cmf/schema/scm_prepare_database.sh mysql cm -hhadoop120 -uroot -p123456 --scm-host hadoop120 scm scm scm



grant all privileges on *.* to 'root'@'localhost' identified by '123456' with grant option;
grant all privileges on *.* to 'root'@'hadoop120' identified by '123456' with grant option;
grant all privileges on *.* to 'root'@'127.0.0.1' identified by '123456' with grant option;
grant all privileges on *.* to 'root'@'%' identified by '123456' with grant option;
grant all privileges on *.* to 'scm'@'localhost' identified by 'scm' with grant option;
grant all privileges on *.* to 'scm'@'hadoop120' identified by 'scm' with grant option;
grant all privileges on *.* to 'scm'@'127.0.0.1' identified by 'scm' with grant option;
grant all privileges on *.* to 'scm'@'%' identified by 'scm' with grant option;

delete from user where Host='hadoop102';
delete from user where Host='127.0.0.1';
delete from user where Host='::1';
```

7. 创建 Parcel-repo

```
[root@hadoop120 module]# mkdir -p /opt/cloudera/parcel-repo
[root@hadoop120 module]#  chown  cloudera-scm:cloudera-scm /opt/cloudera/parcel-rep

#  拷 贝 下 载 文 件 manifest.json 、 CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha1
CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel 到 hadoop120 的/opt/cloudera/parcel-repo/目录下
[root@hadoop120 parcel-repo]# ls
CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel
CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha1
manifest.json

# 将 CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha1：需改名为
CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha
[root@hadoop120  parcel-repo]#  mv CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha1 CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha
```

8. 在 hadoop120 上创建目录/opt/cloudera/parcels，并修改该目录的所属用户及用户组
   为 cloudera-scm

```
[root@hadoop120 module]# mkdir -p /opt/cloudera/parcels
[root@hadoop120 module]# chown cloudera-scm:cloudera-scm /opt/cloudera/parcels
```

9. 在 hadoop121,hadoop122 上分别创建

```
[root@hadoop121 module]# mkdir -p /opt/cloudera/parcels
[root@hadoop121 module]# chown cloudera-scm:cloudera-scm /opt/cloudera/parcels
[root@hadoop121 module]# mkdir -p /opt/cloudera/parcel-repo
[root@hadoop121 module]#  chown  cloudera-scm:cloudera-scm /opt/cloudera/parcel-rep

[root@hadoop122 module]# mkdir -p /opt/cloudera/parcels
[root@hadoop122 module]# chown cloudera-scm:cloudera-scm /opt/cloudera/parcels
[root@hadoop122 module]# mkdir -p /opt/cloudera/parcel-repo
[root@hadoop122 module]#  chown  cloudera-scm:cloudera-scm /opt/cloudera/parcel-rep

#  拷 贝 下 载 文 件 manifest.json 、 CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha1
CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel 到 hadoop120 的/opt/cloudera/parcel-repo/目录下
[root@hadoop120 parcel-repo]# ls
CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel
CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha1
manifest.json

# 将CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha1：需改名为
CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha
[root@hadoop120  parcel-repo]#  mv CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha1 CDH-5.16.2-1.cdh5.16.2.p0.8-el6.parcel.sha

# 分发
scp -r /opt/module/cm/ root@hadoop121:/opt/module/
scp -r /opt/module/cm/ root@hadoop122:/opt/module/
```



# 启动 CM

```
[root@hadoop120  cm]# /opt/module/cm/cm-5.16.2/etc/init.d/cloudera-scm-server start Starting 
 cloudera-scm-server: [确定]


[root@hadoop120  cm]# /opt/module/cm/cm-5.16.2/etc/init.d/cloudera-scm-agent start
[root@hadoop121  cm]# /opt/module/cm/cm-5.16.2/etc/init.d/cloudera-scm-agent start
[root@hadoop122  cm]# /opt/module/cm/cm-5.16.2/etc/init.d/cloudera-scm-agent start

# 查看是否正常运行
[root@hadoop120 cm]# netstat -anp | grep 7180
tcp 0 0 0.0.0.0:7180 0.0.0.0:*
LISTEN 5498/java
```



# 问题

```
发现很多兄弟在安装CDH过程的分配这一步，总是遇到“主机运行状态不良”的提示，当然我也遇到过。


[root@hadoop122 ~]# cd /opt/module/cm/cm-5.16.2/lib/cloudera-scm-agent/
[root@hadoop122 cloudera-scm-agent]# ls
cm_guid  response.avro  uuid
[root@hadoop122 cloudera-scm-agent]# rm cm_guid 
[root@hadoop122 cloudera-scm-agent]# /opt/module/cm/cm-5.16.2/etc/init.d/cloudera-scm-agent restart
```

```
[root@hadoop120 cloudera-scm-server]# cat cloudera-scm-server.out 
JAVA_HOME=/usr/java/latest
Java HotSpot(TM) 64-Bit Server VM warning: ignoring option MaxPermSize=256m; support was removed in 8.0
Exception in thread "main" org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'com.cloudera.server.cmf.TrialState': Cannot resolve reference to bean 'entityManagerFactoryBean' while setting constructor argument; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactoryBean': FactoryBean threw exception on object creation; nested exception is java.lang.IllegalStateException
	at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:328)
	at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveValueIfNecessary(BeanDefinitionValueResolver.java:106)
	at org.springframework.beans.factory.support.ConstructorResolver.resolveConstructorArguments(ConstructorResolver.java:616)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:148)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1003)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:907)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:485)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:456)
	at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:293)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:222)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:290)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:192)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:585)
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:895)
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:425)
	at com.cloudera.server.cmf.Main.bootstrapSpringContext(Main.java:393)
	at com.cloudera.server.cmf.Main.<init>(Main.java:243)
	at com.cloudera.server.cmf.Main.main(Main.java:216)
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactoryBean': FactoryBean threw exception on object creation; nested exception is java.lang.IllegalStateException
	at org.springframework.beans.factory.support.FactoryBeanRegistrySupport.doGetObjectFromFactoryBean(FactoryBeanRegistrySupport.java:149)
	at org.springframework.beans.factory.support.FactoryBeanRegistrySupport.getObjectFromFactoryBean(FactoryBeanRegistrySupport.java:102)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getObjectForBeanInstance(AbstractBeanFactory.java:1440)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:247)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:192)
	at org.springframework.beans.factory.support.BeanDefinitionValueResolver.resolveReference(BeanDefinitionValueResolver.java:322)
	... 17 more
Caused by: java.lang.IllegalStateException
	at com.google.common.base.Preconditions.checkState(Preconditions.java:133)
	at com.cloudera.server.cmf.bootstrap.EntityManagerFactoryBean.checkVersionDoFail(EntityManagerFactoryBean.java:291)
	at com.cloudera.server.cmf.bootstrap.EntityManagerFactoryBean.getObject(EntityManagerFactoryBean.java:127)
	at com.cloudera.server.cmf.bootstrap.EntityManagerFactoryBean.getObject(EntityManagerFactoryBean.java:65)
	at org.springframework.beans.factory.support.FactoryBeanRegistrySupport.doGetObjectFromFactoryBean(FactoryBeanRegistrySupport.java:142)
	... 22 more


1. 删除 /opt/module/cm/cm-5.16.2/etc 目录下的 db*
[root@hadoop120 cm-5.16.2]# cd etc
[root@hadoop120 etc]# pwd
/opt/module/cm/cm-5.16.2/etc
[root@hadoop120 cloudera-scm-server]# rm db*

2. 删除 mysql 上的 cm 数据库
drop database cm;

3. 初始化 cm
/opt/module/cm/cm-5.16.2/share/cmf/schema/scm_prepare_database.sh mysql cm -hhadoop120 -uroot -p123456 --scm-host hadoop120 scm scm scm

4. 启动 server
/opt/module/cm/cm-5.16.2/etc/init.d/cloudera-scm-server start Starting 

```

