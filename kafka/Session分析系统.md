[toc]

# 简介

本项目主要用于互联网电商企业中，使用Spark技术开发的大数据统计分析平台，对电商网站的各种用户行为（访问行为、购物行为、广告点击行为等）进行复杂的分析。用统计分析出来的数据，辅助公司中的PM（产品经理）、数据分析师以及管理人员分析现有产品的情况，并根据用户行为分析结果持续改进产品的设计，以及调整公司的战略和业务。最终达到用大数据技术来帮助提升公司的业绩、营业额以及市场占有率的目标  

本项目使用了Spark技术生态栈中最常用的三个技术框架，Spark Core、Spark SQL和Spark Streaming，进行离线计算和实时计算业务模块的开发。实现了包括用户访问session分析、页面单跳转化率统计、热门商品离线统计、广告流量实时统计4个业务模块。



## 模块简介

1. 用户访问session分析：该模块主要是对用户访问session进行统计分析，包括session的聚合指标计算、按时间比例随机抽取session、获取每天点击、下单和购买排名前10的品类、并获取top10品类的点击量排名前10的session。该模块可以让产品经理、数据分析师以及企业管理层形象地看到各种条件下的具体用户行为以及统计指标，从而对公司的产品设计以及业务发展战略做出调整。主要使用Spark Core实现。
2. 页面单跳转化率统计：该模块主要是计算关键页面之间的单步跳转转化率，涉及到页面切片算法以及页面流匹配算法。该模块可以让产品经理、数据分析师以及企业管理层看到各个关键页面之间的转化率，从而对网页布局，进行更好的优化设计。主要使用Spark Core实现。
3. 热门商品离线统计：该模块主要实现每天统计出各个区域的top3热门商品。然后使用Oozie进行离线统计任务的定时调度；使用Zeppeline进行数据可视化的报表展示。该模块可以让企业管理层看到公司售卖的商品的整体情况，从而对公司的商品相关的战略进行调整。主要使用Spark SQL实现。
4. 广告流量实时统计：该模块负责实时统计公司的广告流量，包括广告展现流量和广告点击流量。实现动态黑名单机制，以及黑名单过滤；实现滑动窗口内的各城市的广告展现流量和广告点击流量的统计；实现每个区域每个广告的点击流量实时统计；实现每个区域top3点击量的广告的统计。主要使用Spark Streaming实现。



# 平台环境的搭建

## 离线日志采集

1. 网页，APP 的数据经常会发送请求到后台服务器，通常会由 Nginx 接收请求，并进行转发；
2. 日志文件通常每天一份，并且会预先设定特殊的日志格式；
3. 日志转移，可以使用 crobtab 定时调度一个 shell 脚本进行定时的调度工具，工具每天负责将所有的日志的数据都收集进行合并和处理。
4. Flume 用于监控后台服务器的目录 ，将文件 pull 拉取到 HDFS；
5. HDFS 可以通过定时的 MR/Hive 作业，对原始数据进行清洗，并写到另外一个文件中；
6. 清洗后的数据，导入到 Hive 的表中，可以采用分区表来分区存放每天的数据；
7. 可以采用 Hive 来定时操作表；
8. 或者通过 Spark/storm/Hadoop 来进行数据的开发；



## 实时日志采集

1. 网页，APP 的数据经常会发送请求到后台服务器，通常会由 Nginx 接收请求，并进行转发；
2. 日志文件通常每天一份，并且会预先设定特殊的日志格式；
3. 日志转移，可以使用 crobtab 定时调度一个 shell 脚本进行定时的调度工具，工具每天负责将所有的日志的数据都收集进行合并和处理。
4. 通过 Kafka/Flume 来试试获取数据，并写入消息队列。
5. Spark 主动从 Kafka 拉取数据进行实时推荐计算。



## 提交方式

```
./bin/spark-submit \
--class org.apache.example.JavaSparkPi \
--master yarn-client \
--num-executors 1 \
--driver-memory 10m \
--executor-memory 10m \
--executor-cores 1 \
spark/lib/spark-example.jar \


./bin/spark-submit \
--class org.apache.example.JavaSparkPi \
--master yarn-cluster \
--num-executors 1 \
--driver-memory 10m \
--executor-memory 10m \
--executor-cores 1 \
spark/lib/spark-examples.jar \

```



# 准备阶段

## 建表

- user_visit_action 表：放每天的点击流的数据

```
date：日期，代表这个用户点击行为是在哪一天发生的
user_id：代表这个点击行为是哪一个用户执行的
session_id ：唯一标识了某个用户的一个访问session
page_id ：点击了某些商品/品类，也可能是搜索了某个关键词，然后进入了某个页面，页面的id
action_time ：这个点击行为发生的时间点
search_keyword ：如果用户执行的是一个搜索行为，比如说在网站/app中，搜索了某个关键词，然后会跳转到商品列表页面；搜索的关键词
click_category_id ：可能是在网站首页，点击了某个品类（美食、电子设备、电脑）
click_product_id ：可能是在网站首页，或者是在商品列表页，点击了某个商品（比如呷哺呷哺火锅XX路店3人套餐、iphone 6s）
order_category_ids ：代表了可能将某些商品加入了购物车，然后一次性对购物车中的商品下了一个订单，这就代表了某次下单的行为中，有哪些
商品品类，可能有6个商品，但是就对应了2个品类，比如有3根火腿肠（食品品类），3个电池（日用品品类）
order_product_ids ：某次下单，具体对哪些商品下的订单
pay_category_ids ：代表的是，对某个订单，或者某几个订单，进行了一次支付的行为，对应了哪些品类
pay_product_ids：代表的，支付行为下，对应的哪些具体的商品
```



-  user_info  表：就是一张最普通的用户基础信息表；这张表里面，其实就是放置了网站/app所有的注册用户的信息  

```
user_id：其实就是每一个用户的唯一标识，通常是自增长的Long类型，BigInt类型
username：是每个用户的登录名
name：每个用户自己的昵称、或者是真实姓名
age：用户的年龄
professional：用户的职业
city：用户所在的城市
```



- task表：用来保存平台的使用者  

```
task_id：表的主键
task_name：任务名称
create_time：创建时间
start_time：开始运行的时间
finish_time：结束运行的时间
task_type：任务类型，就是说，在一套大数据平台中，肯定会有各种不同类型的统计分析任务，比如说用户访问session分析任务，页面单跳转化率统计任务；所以这个字段就标识了每个任务的类型
task_status：任务状态，任务对应的就是一次Spark作业的运行，这里就标识了，Spark作业是新建，还没运行，还是正在运行，还是已经运行完毕
task_param：最最重要，用来使用JSON的格式，来封装用户提交的任务对应的特殊的筛选参数
```



## 需求分析

- 按条件筛选 session

​     搜索过某些关键词的用户、访问时间在某个时间段内的用户、年龄在某个范围内的用户、职业在某个范围内的用户、所在某个城市的用户，发起的session。找到对应的这些用户的session， 。

-  统计出符合条件的session中

访问时长，也就是说一个 session 对应的开始的 action，到结束的 action，之间的时间范围。

访问步长，指的是，一个 session 执行期间内，依次点击过多少个页面 。

- 按照时间比例随机抽取1000个session  

按时间比例随机采用抽取，就是要做到，观察样本的公平性

- 获取点击、下单和支付数量排名前10的品类 

计算出所有这些 session 对各个品类的点击、下单和支付的次数，然后按照这三个属性进行排序，获取前10个品类。  

- 排名前10的品类，分别获取其点击次数排名前10的session

对于top10的品类，每一个都要获取对它点击次数排名前10的session  



## 设计结果数据表

```
第一表：session_aggr_stat表，存储第一个功能，session聚合统计的结果
CREATE TABLE `session_aggr_stat` (
  `task_id` int(11) NOT NULL,
  `session_count` int(11) DEFAULT NULL,
  `1s_3s` double DEFAULT NULL,
  `4s_6s` double DEFAULT NULL,
  `7s_9s` double DEFAULT NULL,
  `10s_30s` double DEFAULT NULL,
  `30s_60s` double DEFAULT NULL,
  `1m_3m` double DEFAULT NULL,
  `3m_10m` double DEFAULT NULL,
  `10m_30m` double DEFAULT NULL,
  `30m` double DEFAULT NULL,
  `1_3` double DEFAULT NULL,
  `4_6` double DEFAULT NULL,
  `7_9` double DEFAULT NULL,
  `10_30` double DEFAULT NULL,
  `30_60` double DEFAULT NULL,
  `60` double DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


第二个表：session_random_extract表，存储我们的按时间比例随机抽取功能抽取出来的1000个session
CREATE TABLE `session_random_extract` (
  `task_id` int(11) NOT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `start_time` varchar(50) DEFAULT NULL,
  `end_time` varchar(50) DEFAULT NULL,
  `search_keywords` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


第三个表：top10_category表，存储按点击、下单和支付排序出来的top10品类数据
CREATE TABLE `top10_category` (
  `task_id` int(11) NOT NULL,
  `category_id` int(11) DEFAULT NULL,
  `click_count` int(11) DEFAULT NULL,
  `order_count` int(11) DEFAULT NULL,
  `pay_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


第四个表：top10_category_session表，存储top10每个品类的点击top10的session
CREATE TABLE `top10_category_session` (
  `task_id` int(11) NOT NULL,
  `category_id` int(11) DEFAULT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `click_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


最后一张表：session_detail，用来存储随机抽取出来的session的明细数据、top10品类的session的明细数据
CREATE TABLE `session_detail` (
  `task_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `page_id` int(11) DEFAULT NULL,
  `action_time` varchar(255) DEFAULT NULL,
  `search_keyword` varchar(255) DEFAULT NULL,
  `click_category_id` int(11) DEFAULT NULL,
  `click_product_id` int(11) DEFAULT NULL,
  `order_category_ids` varchar(255) DEFAULT NULL,
  `order_product_ids` varchar(255) DEFAULT NULL,
  `pay_category_ids` varchar(255) DEFAULT NULL,
  `pay_product_ids` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

额外的一张表：task表，用来存储J2EE平台插入其中的任务的信息
CREATE TABLE `task` (
  `task_id` int(11) NOT NULL AUTO_INCREMENT,
  `task_name` varchar(255) DEFAULT NULL,
  `create_time` varchar(255) DEFAULT NULL,
  `start_time` varchar(255) DEFAULT NULL,
  `finish_time` varchar(255) DEFAULT NULL,
  `task_type` varchar(255) DEFAULT NULL,
  `task_status` varchar(255) DEFAULT NULL,
  `task_param` text,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8
```


# 需求处理

## 广告点击流量实时统计

1. 通过从 Kafka 获取数据，数据首先通过过滤方式过滤掉黑名单中的数据，再计算每个用户的点击次数，再次过滤掉可疑数据，并将可疑数据保存到数据库，再进行对 key 进行分析，计算实时点击操作数据，获取topN 数据，窗口时间内的点击统计等。

2. 从 hdfs 上读取数据进行离线分析计算，将数据映射成 PairRDD，通过 groupby 操作获得 <key,list>，通过拿取指定页面路径 （A_B），来进行 PairRDD 的封装，分别计算出单跳的集合，然后再在通过 A_B/A 或者 A_B/c_A 来分析单跳率。
3. 离线计算，从 hdfs 上读取数据然后统计出热门应用的排行。使用 HQL。
4. 离线操作，分析用户的步长和时长。





