[toc]

# 案例1

有十万个淘宝店铺，每个顾客访问任意一个店铺时都会生成一条访问日志。访问日志存储表的 visit，其中访问用户 ID 的字段为 uid，访问店铺的字段为 store，统计店铺的 UV。

```
select store,count(distinct(uid)) as uv from visit group by store;
```



# 案例二

有一亿个用户，被存储在表 Users，其中有一个用户字段 uid，用户年龄 age，和用户消费总消费金额 total，请按照用户年纪从大到小，如果年龄相同，那么按照金额来从小到大。

```
select * from Users order by age desc,total
```



# 案例三

当前有用户人生阶段表 LifeStage，有一个用户 uid，用户人生字段 stage，其中字段 stage 的内容采用逗号进行拼接，统计人生阶段的用户量

```
select *,stageLateral,count(distinct uid) from LifeStage lateral view explode(split(stage,",")) LifeStageTmp as stageLateral group by stageLateral;
```



## 侧视图

```
FROM tableName LATERAL VIEW udtf(expression) tableAlias AS columnAlias[','columnAlias]

--这种其实是针对一个字段内有多个数据，采用一定的方式进行的拼接，所以可以采用 explode 进行转换。
a:shandong,b:beijing,c:hebei|1,2,3,4,5,6,7,8,9|[{"source":"7fresh","monthSales":4900,"userCount":1900,"score":"9.9"},{"source":"jd","monthSales":2090,"userCount":78981,"score":"9.8"},{"source":"jdmart","monthSales":6987,"userCount":1600,"score":"9.0"}]

--这是一行数据，首先建表
drop table explode_lateral_view;
create table explode_lateral_view
(`area` string,
`goods_id` string,
`sale_info` string)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '|'
STORED AS textfile;

--导入数据后，显示肯定是这样的（对字段内意义显示）
**area**：a:shandong,b:beijing,c:hebei
**goods_id**：1,2,3,4,5,6,7,8,9
**sale_info**：[{"source":"7fresh","monthSales":4900,"userCount":1900,"score":"9.9"},{"source":"jd","monthSales":2090,"userCount":78981,"score":"9.8"},{"source":"jdmart","monthSales":6987,"userCount":1600,"score":"9.0"}]

--这样对 goods_id 使用 explode 来进行行转列
select explode(split(goods_id),",") as goodsID from tableView；
--因为 goodsID 是当前的列名，所以显示为
列名 goodsID
第一行：1
第二行：2，这样依次向下

--对 area 进行行转列
select explode(split(area,",")) as areaID from tableView;
--这样area的行数据就被转换为列数据了。也是通过‘,’,一行一条。

--对 sale_info 字段的拆解。
select explode(split(sale_info,",")) as saleInfo from tableView;

--由于explode 仅仅支持一个字段，所以就可以使用 lateral view 了。
select goodsID,sale_info from tableValue lateral view explode(split(goods_id,",")) goods as goodsID;

--如果对三个字段都进行行转列：
select goodsID,saleInfo,areaID from tableView lateral view explode(split(goods_id),",") goods as goodsID lateral view explode(split(area,',')) area as areaID lateral view explode(split(sale_info,",")) saleInfos as saleInfo;

-- 如果使用个 json 格式,正则表达式为 ：\\[\\{',''),'}]','=A, },\\{=B
select get_json_object(concat('{',sale_info_1,'}'),'$.source') as source, get_json_object(concat('{',sale_info_1,'}'),'$.monthSales') as monthSales,
get_json_object(concat('{',sale_info_1,'}'),'$.userCount') as monthSales, get_json_object(concat('{',sale_info_1,'}'),'$.score') as monthSales from  explode_lateral_view LATERAL VIEW explode(split(regexp_replace(regexp_replace('A'),'B') sale_info as sale_info_1;
```



# 案例四

LifeStage 中每行数据存储一个用户 stage，希望这个 stage 只是一个值，不是拼接的，现在希望可以将相同搞的 uid 的用户的 stage 都拼接在一起。如 uid=10，stage=A，stage=B。现在希望 stage=A,B.

```
select uid,concat_ws(',',collect_set(stage)) AS stage from LifeStage group by uid;
```



# 案例五

按照学生的位置和科目成绩取 topN。利用窗口函数来处理。

```
select a.* from (select * ,ROW_NUMBER() over(partition by ex_class order by ex_score desc) rank from (select * ,ROW_NUMBER() over(partition by ex_location) from ex_student) AS b) AS a where a.rank <= 3;
```



# 案例六

获取每个用户的前 1/4 次的数据。NTILE(4)：四个分桶

```
SELECT a.* from (SELECT cookieid,createtime,pv,NTILE(4) OVER(PARTITION BY cookieId ORDER BY createtime) AS rn from table) a WHERE a.rn = 1
```



# 案例七

```
SELECT a.* from t1 a LEFT OUT JOIN t2 b on a.id = b.id where b.id is null;
```

首先操作 join，然后在针对 join 之后的表进行 where 操作得到结果表。由于 join 操作会进行非空过滤，得到的中间表再进行 where 的过滤操作。



# 案例八

按照学生的科目成绩取 topN。利用窗口函数来处理。

```
select a.* from (select * ,ROW_NUMBER() over(partition by ex_class order by ex_score desc) rank from b) AS a where a.rank <= 3;
```

