[toc]

# Hive 调优

## Fetch 抓取

Fetch 抓取是 **Hive 中在某些情况的查询可以不必使用 MR 计算**。

将  `hive.fetch.task.conversion` 设置成 `more`，在进行全局查找、字段查找、limit 查找都可以不走 MR。

`hive.fetch.task.conversion` 有三种设置：

`none`：在任何时候执行查询语句都会执行 MR；

`more`：在 select，filter，limit 等操作时不会执行 MR；

`minimal`：在进行分区操作时，只有 select，filter 等操作才不会执行 MR。



## 本地模式

对于输入较小的数据量，可以使用本地模式在单机上进行所有任务操作。通过设置 hive.exec.mode.local.auto` 设置为 `true`，让 Hive 自动在适当的时候启动这个优化。同时这个优化还可以设置输入量大小和文件个数：

`hive.exec.mode.local.auto.inputbytes.max=number`

`hive.exec.mode.local.auto.input.files.max=number` 



## 表的优化

### 小表、大表 的 join

将 key 相对分散，并且数据量较小的表放在 join 的左边，这样可以减少内存溢出的概率。因为 join 操作一般会将左边的表放入内存。



### 大表 join 大表

#### 空 key 过滤

有时 join 导致的数据倾斜，是因为某一个 key 的数据量非常大，因为相同的 key 的数据会发送到同一个 reducer 上去进行计算。如果这些 key 对应的数据是异常数据，可以对 key 进行过滤处理。例如 key 对应的字段为空。

```
select n.* from (select * from nullidtable where id is not null) n left join ori o on n.id = o.id;
```



#### 空 key 转换

有时候虽然会导致数据倾斜的问题，但是该 key 不属于异常数据。可以是的数据随机分配到不同的 reducer 上。

```
select n.* from nullidtable n full join ori o on case when n.id is null then concat('hive',rand()) else n.id end = o.id;
```



### Mapjoin

如果不指定 MapJoin 或者不符合 MapJoin，那么 Hive 在进行 join 操作时会变成 common join，即在 Reducer 端进行join 操作，这样容易发生数据倾斜。因此可以考虑在 Map 端进行 join 操作，避免 reducer 的操作。

```
1. 开启 MapJoin
hive.auto.convert.join = true;

2. 设置大小表的阈值
hive.mapjoin.smalltable.filesize=25000000;

select * from bigtable b join smalltable s on b.id=s.id;
```



### Group By

一般情况下，Map 阶段的相同 key 会发送给同一个 reducer，当以个 key 很大时，就发生了数据倾斜。

但是并不是所有的操作都需要到 reduce 端进行数据的聚合，也可以在 Map 端进行部分聚合后，再在 reducer 端进行全局聚合。

```
1. 开启 Map 端聚合参数设置
hive.map.aggr=true;

2. 设置 Map 端聚合操作的条目数
hive.groupby.mapaggr.checkinterval=100000;

3. 设置负载均衡
hive.groupby.skewindata=true

当参数设置为 true 时，会进行两次 MapJob，第一次是在 Map 输出数据随机的分配到不同的 reduce 上，进行部分聚合操作（即使是相同的 key也会发送到不同的 reduce）。第一次是在 reduce 做完部分聚合后在发送到对应的 reduce（相同的key发送到同一个reduce），完成最终的全局聚合。
```



### Count(distinct) 去重统计

count distinct 操作需要一个 Reduce 来完成，在大量数据的情况下，虽然可以也可以完成操作。但是 distinct 会将所有数据保存到内存，因此可以会引发 OOM 问题。因此可以采用 group by 来进行分组，然后在进行 count 计算。

```
select count(distinct id) from table;

select count(id) from (select id from table group by id) a;
```

也就是说在大数据量的情况下使用 group by 来代替 distinct 其实就是以时间换空间的做法。

对于Hive来说，含有 distinct 的 HQL 语句，如果遇到瓶颈，想要调优，第一时间都是想到用group by来替换distinct来实现对数据的去重。 



### 笛卡尔积

尽量避免笛卡尔积，join 的时候不加 on 条件，或者无效的 on 条件，Hive 只能使用 1 个
reducer 来完成笛卡尔积。



### 行列过滤

列处理：在进行 select 操作时，只要拿需要的列。如果需要全部，尽量使用分区过滤，少用 select *；

行处理：在分区裁剪中，使用外关联时，如果将附表的过滤条件在 where 之后，那么就会先全表关联，然后过滤。

```
select o.id,from bigtable b join ori o on o.id=b.id where o.id<=10;

select 0.id from bigtable b join (select id from ori where ori.id <=10) o on b.id=o.id;
```



### 动态分区

在对分区表 insert 数据时，数据库自动根据分区字段的值，将数据插入到相应的分区中，即动态分区。

```
1. 开启动态分区
hive.exec.dynamic.partition=true;

2. 设置成非严格模式
hive.exec.dynamic.partition.mode=nonstrict;
/*strict 表示必须制定至少一个分区为静态分区，nonstrict允许所有分区字段都可以使用动态分区*/

3. 设置分区个数
hive.exec.max.dynamic.partitions=1000

4. 每个 MR 节点可以创建的最大动态分区
hive.exec.max.dynamic.partitions.pernode=100

5. MR 可以创建的 HDFS文件
hive.exec.max.created.files=100000

6. 当有空分区时，是否抛出异常
hive.error.on.empty.partition=false

insert overwrite table ori_partitioned_target partition(p_time) select id,time,uid,keyword,url_rank,click_num,click_url,p_time from ori_partitoned;

# 查看分区
show partitons ori_partitioned_target;
```



## 数据倾斜

### 设置合理的 Map 数

- Map不是设置的越多越好

如果任务有很多小文件，则可以每个文件当做一个块，用一个 map 任务来完成，而一个 map 任务启动和初始化的时间远远大于逻辑处理时间，就会造成大量的资源浪费。



### 小文件的合并

在 map 执行前合并小文件，减少 map 数：CombineHiveInputFormat 具有对小文件进行合并的功能（系统默认的格式）。HiveInputFormat 没有对小文件合并功能。
`set hive.input.format= org.apache.hadoop.hive.ql.io.CombineHiveInputFormat`



### 复杂文件增加  Map

当 input 的文件都很大，任务逻辑复杂，map 执行非常慢的时候，可以考虑增加 Map
数，来使得每个 map 处理的数据量减少，从而提高任务的执行效率。

```
computeSliteSize(Math.max(minSize,Math.min(maxSize,blocksize)))=blocksize=128M 调
整 maxSize 最大值。让 maxSize 最大值低于 blocksize 就可以增加 map 的个数

1. 设置最大切片值为 100 byte。
mapreduce.input.fileinputformat.split.maxsize=100
```



### 合理设置 Reduce

1. 调整 reduce 的个数方法一

```
1. 调整每个 reduce 处理的数据量
hive.exec.reducers.bytes.per.reducer=256000000

2. 每个任务最大的 reduce 数
hive.exec.reducers.max=1009

3. 计算 reducer 数
N=min(hive.exec.reducers.max, 总输入大小/ hive.exec.reducers.bytes.per.reducer)
```

2. 调整 reduce 个数方法二

```
mapreduce.job.reduces=15
```



- reduce 是否越多越好

1. reduce 太多会导致启动和初试化消耗太多的时间和资源；
2. 设置 reduce 的个数与输出文件有关，所以尽量不要生成太多的文件。

设置 reduce 个数的时候也需要考虑这两个原则：**处理大数据量利用合适的 reduce
数；使单个 reduce 任务处理数据量大小要合适；**



## 并行执行

某个特定的 job 可能包含众多的阶段，而这些阶段可能并非完全互相依赖的，也就是说有些阶段是可以并行执行的，这样可能使得整个 job 的执行时间缩短。如果有更多的阶段可以并行执行，那么 job 可能就越快完成。

```
1. 开启并行执行
hive.exec.parallel=true;

2. 运行最大并行度
hive.exec.parallel.thread.number=16;
```



## 严格模式

```
hive.mapred.mode=strict/nonstrict
```

1. 对于分区表，除非 where 语句中含有分区字段过滤条件来限制范围，否则不允许执行。换句话说，就是用户不允许扫描所有分区。进行这个限制的原因是，通常分区表都拥有非常大的数据集，而且数据增加迅速。没有进行分区限制的查询可能会消耗令人不可接受的巨大资源来处理这个表。
2. 对于使用了 order by 语句的查询，要求必须使用 limit 语句。因为 order by 为了执行排序过程会将所有的结果数据分发到同一个 Reducer 中进行处理，强制要求用户增加这个 LIMIT语句可以防止 Reducer 额外执行很长一段时间。
3. 限制笛卡尔积的查询。对关系型数据库非常了解的用户可能期望在执行 JOIN 查询的时候不使用 ON 语句而是使用 where 语句，这样关系数据库的执行优化器就可以高效地将WHERE 语句转化成那个 ON 语句。不幸的是，Hive 并不会执行这种优化，因此，如果表足够大，那么这个查询就会出现不可控的情况。



## JVM 重用

由于 JVM 的启动过程相当消耗资源，因此可以采用 JVM 重用让JVM实例在同一个job 中重复使用。 开启JVM 重用对于大量小文件Job，可以开启JVM 重用会减少 45%运行时间。JVM 重用理解：一个 map 运行一个 jvm，重用的话，在一个 map 在 jvm 上运行完毕后，jvm 继续运行其他 map。

```
1. 可重用的个数
mapreduce.job.jvm.numtasks
```

这个功能的缺点是，开启 JVM 重用将一直占用使用到的 task 插槽，以便进行重用，直到任务完成后才能释放。如果某个“不平衡的”job 中有某几个 reduce task 执行的时间要比其他 Reduce task 消耗的时间多的多的话，那么保留的插槽就会一直空闲着却无法被其他的 job使用，直到所有的 task 都结束了才会释放。



## 推测执行

根据一定的法则推测出“拖后腿”的任务，并为这样的任务启动一个备份任务，让该任务与原始任务同时处理同一份数据，并最终选用最先成功运行完成任务的计算结果作为最终结果。

```
1. hadoop 文件中的开启推测执行参数
mapreduce.map.speculative=true
mapreduce.reduce.speculative=true

2. hive 的 reduce 推测执行参数
hive.mapred.reduce.tasks.speculative.execution
```

