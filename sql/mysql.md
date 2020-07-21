[toc]

# 查询命令
## 查询命令优先级顺序
SQL的优先级顺序是
```
SELECT 字段名，聚合函数
        FROM   表名
        WHERE  对数据行进行定位判断条件
        GROUP BY  分组字段1，分组字段2....
        HAVING  使用 聚合函数 统计当前临时表是否可以被删除
        ORDER BY 根据select提供的临时表字段进行排序
        LIMIT 起始数据行位置，截取行数

优先级顺序：
FROM->WHERE->GROUP BY->HAVING->SELECT->ORDER BY->LIMIT
```

## 临时表
- 由查询命令在内存中生成的虚拟表，被称为临时表。
- 当查询命令执行结束后，服务器会自动将上一个查询命令生活才能的临时表自动销毁处理。
- 每个查询命令都是操作这上一个查询命令生存的临时表。
- 临时表名称就是硬盘上表文件名。
- 在一个查询命令结束后，只能看到最后一个查询命令生成的临时表。

## from
from 是将表文件加载到内存中生成一个 ‘临时表’。在 sql 查询语句中，第一个执行的始终是 from 命令。
```
select name,age from student;

1. from 将 student 表文件加载生成为一张 ‘临时表’；
2. select name,age 来操作 from 生成的这张临时表，取出 name，age 字段再生成一张新的临时表，from 生成的临时表被销毁。
```

## where
where 查询命令是遍历 ‘临时表’中的每一行数据，遍历时取出数据行来判断当前数据行是否满足查询条件。在循环遍历结束后，where 命令将所有满足条件的数据行读取出来生成一张全新的 ‘临时表’。

所以，where 的操作类似于：
```
for (String s : lists){
	if(s.equals('条件')
}
```

而且，where 查询命令的优先级高于 select 查询命令。

```
select name，age from student where age > 10;

1. from 读取表文件生成一张 ‘临时表’；
2. where 根据 from 生成的 ‘临时表，遍历表中的所有行数据，每次取出一行，就进行判定是否满足定位条件。循环结束后，取出所有妈祖定位条件的行数据，保存到一张新的 ‘临时表’ 中。from 生成的临时表销毁。
3. select 在 where 操作完成后开始执行。将 where 生成的临时表中选择指定的字段内容读取出来，组成一张全新的临时表。而 where 生成的临时表销毁。
```

## group by
group by 是分组查询命令。其根据 ‘分组字段’ 内容对临时表数据进行分类，然后将具有相同特征的数据行保存到同一个临时表中。在七个基本查询命令中，只有  group by 在执行完成后才可能生成多个临时表。

```
select age count(*) from student group by age;

1. from 读取表文件生成一张 ‘临时表’；
2. group by age 对 ‘临时表’ 中具有相同 age 的数据行保存到同一个临时表中，并且每个临时表都是具有上一个临时表的所有字段。
3. 由于 group by 可能会生成多个临时表， select 查询将会依次操作每一个临时表，所以在对 group by 生成的临时表进行操作时，只会读取指定字段的第一行数据，然后再将这些数据行内容组成一张全新的临时表。

select 操作 group by 提供的临时表时，此时读取字段内容应该是能表示当前临时表所有数据共同特征，因此 select 读取字段一般是  group by 的分组字段。
```
如果，下图是 group by 生成的临时表，目前是有两张临时表。当进行  select 操作时，指定的字段一般就是每张表的共有属性特征。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2020060917130082.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2RlY19zdW4=,size_16,color_FFFFFF,t_70,#pic_center)

group by 在进行多字段的分组时，只需要以逗号来分割分组字段。
```
select home,sex from student group by sex,home;
```
1. 分组字段执行的顺序对于最终结果没有任何影响；字段的顺序不影响结果；
2. group by 一次只能根据一个分组字段进行临时表拆分；
3. 第二个分组字段的分组操作是从第一个分组字段分组操作生成的临时表上进行的操作。
![process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2RlY19zdW4=,size_16,color_FFFFFF,t_70)](https://img-blog.csdnimg.cn/20200609172101363.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2RlY19zdW4=,size_16,color_FFFFFF,t_70,#pic_center)

## having
在 SQL 中增加 having 子句原因是，where 关键字无法与聚合函数一起使用。having 子句可以让我们筛选分组后的各组数据。

having 只会出现在 group by 之后，也是在 select 查询操作之前执行。

1. 因此 having 不能独立的出现，只能在 group by 出现之后才有可能出现；
2. 出现时，必须在 group by 之后。

having 是对 group by 生成的临时表进行 ‘统计分析’，将不满足条件的临时表从内存中进行销毁。即 having 的任务就是销毁内存中的临时表。

```
select home,count(*) from student group by home having count(*) >=3;

1. from 读取表文件生成 ‘临时表’；
2. group by home 对 临时表进行分组；
3. having count(*)  >= 3 对生成的每个临时表进行统计分析，如果 统计结果 不满足条件，即销毁这个临时表；
4. select 读取 having 销毁后的 临时表内容共同特征数据结果。
```

### having & where
| where                                          | having                                                       |
| ---------------------------------------------- | ------------------------------------------------------------ |
| 将满足条件的数据行读取出来生成一张全新的临时表 | 对 group by 生成的临时表中不满足条件的临时表进行销毁         |
| 针对数据行操作，判断条件不能使用聚合函数       | 针对group by 生成的临时表操作，having 后面应该使用聚合函数作为判断条件 |
| 在 group by 之前执行                           | 在 group by 之后执行                                         |
| 可以独立存在                                   | 必须依附 group by 存在而存在                                 |


## order by
order by 是对 select 生成的临时表数据进行排序，将排序后的数据行组成一个全新的临时表。

因此，order  by 是在 select 操作执行之后执行。

```
select name,age from student order by age;

1. from 读取表文件生成一张临时表
2.  select 读取 from 的临时表，取出指定字段的数据组成一张全新的临时表；
3. order by  读取 select 的临时表，按照指定字段进行排序。
```

## limit
limit 的优先级小于 order  by， 其作用是对临时表的数据行进行截取，将截取得到的数据存入到一个全新的临时表中。

```
limit start,offset
# start：为查询结果的索引值(默认从0开始)，当start=0时可省略start
# offset：为查询结果返回的数量
# start 与 offset 之间使用英文逗号","隔开

# limit n 等同于 limit 0,n
```

# 多表查询
多表查询的本质是将多张表中数据行 合并成  一张临时表，然后利用查询命令对合成后的临时表数据行进行过滤查询的过程。

多张表数据行合并成一张临时表有 “连接查询合并” 和 “联合查询合并”。而无论多少张表合并在一起，sql 服务器一次也仅仅能将两张表合为一张临时表。

| 连接查询合并             | 联合查询合并           |
| ------------------------ | ---------------------- |
| 两表存在隶属关系，如外键 | 两张表没有任何隶属关系 |


## 连接合并查询
只有具有隶属关系的两张表数据行才有可能通过连接合并组合成一张临时表。

```
from tableOne join tableTwo

1. tableName 与 tableTwo 在 join 两边位置对于查询结果没有任何影响；
2. 合成的临时表字段由 tableOne  和 tableTwo 的所有字段组成；
3. 合成的临时表的数据行为  tableOne * tableTwo；
4. 合成的临时表一定存在不满足实际隶属关系的数据行，这种数据被称为 脏数据；
5. 对合成后的临时表在进行具体操作之前，需要将合法的数据行找出来进行后续操作。
```

### 内连接
对由连接查询合并而来的临时表中合法数据行进行定位，只会将合法的数据行读取保存到一个全新的临时表中。
```
from t1 join t2 on 判断条件

对 join 连接后的所有数据行根据 on 的判断条件进行判断，在循环遍历结束后，将合法的数据读取出来保存到一个全新的临时表中。


select t1.name,t1.age,t2,sex from tableOne as t1 join tableTwo as t2 on t1.name= t2.name;

1. from tableOne as t1 join tableTwo as t2，首先执行 from，将两张表连接合并成一张临时表，数据行的总数为  tableOne * tableTwo；
2. on t1.name= t2.name， 判断 from 中的临时表中满足判断条件的数据行生成一张全新的临时表；
3. select ，从 on 生成的临时表中取出指定字段的数据生成一张全新的临时表。

from tableOne join tableTwo on 判断条件：可以假定认为是 “ tableOne join tableTwo on 判断条件” 是一张临时表，通过 from 来加载到内存中。

对于多张表连接
select t1.name,t2.sex,t3.sex 
	from tableName as t1 
		join tableName as t2 
			on 连接条件 
		join tableName as t3 
			on 连接条件
		join tableName as t4
			on 连接条件
		join tableName as t5
			on 连接条件
```

### 外连接
1. 将表分为两种表 【不被偏袒的表】和 【被偏袒表】 
    【被偏袒的表】的数据行如果与【不被偏袒表】的数据行可以拼接为合法数据行，此时不被偏袒表正常执行
    【被偏袒的表】 的某个数据行如果与 【不被偏袒表 】的数据行  【都无法匹配成功 】 ，此时将这个数据行作为一个独立数据行存入到新的临时表

2. 在外连接过滤查询之后，如果统计数据行行数，不能使用count(*),只能使用count(不被偏袒的表中主键字段)
```
from t1 left/right join t2 on 判断条件
```

left join : t1 是【被偏袒表】，表示生成的全新【临时表】会全部显示 t1表的数据；t2 是【不被偏袒表】，表示全新的【临时表】只会显示满足判断条件的 t2 表的数据

right join : t2 是【被偏袒表】，表示生成的全新【临时表】会全部显示 t2表的数据；t1 是【不被偏袒表】，表示全新的【临时表】只会显示满足判断条件的 t1 表的数据

### 自连接
自连接是指：硬盘上的表文件被多次【加载】，每加载一次就会生成一张【临时表】，将这些【临时表】通过连接合并成一张全新的临时表。
```
select t1.name,t2.sex
	FROM  tableOne AS t1 LEFT 
		JOIN  tableOne AS t2
        	ON  t1.id = t2.rid;
```

## 联合合并
联合合并采用 【union [all]】操作命令，将两个【临时表】数据行按照【垂直】方向堆砌在一个【临时表】中。

1. 两个临时表必须拥有【相同个数】的字段；
2. 合并生成的临时表字段名称来至于第一个【临时表字段名】；
3. union 合并操作会将【重复的数据】进行过滤；
4. 如果要保留可能存在的重复的数据行，可以使用 union all

```
select  字段 from tableOne
union [all]
select 字段 from tableTwo；
```

# case... end
case... end 用于多条件判断的结果输出，其执行优先级高于 select 。默认是从 【select 前一步的临时表】中遍历所有行，然后按照判断条件，因此填充相应的结果内容。其操作相当于 if...else，或者 swich 的条件判断。
```
CASE
        WHEN  当前数据行.字段名>=值1      THEN '解释内容'
        WHEN  当前数据行.字段名>=值2      THEN '解释内容2'
        [ELSE   '解释内容3']
END


select ename, sal,
	(case 
		when emp.sal >= 5000 then '第一批次'
		when  emp.sal>=3000  then '第二批次'
		when  emp.sal>=2000  then '第三批次'
		else '零批次'
	end)  as  title
from emp；

----------------------------------------------------------
CASE   当前数据行.字段名
        WHEN  '值1'   THEN '解释内容1'
        WHEN  '值2'   THEN '解释内容2'
        [ELSE  ‘值3']  
END


SELECT  ENAME,JOB,SAL,
	(CASE  emp.job
		when 'PRESIDENT'  then '私营老板'
		when 'manager'    then '企业高管'
		when 'ANALYST'    then '技术宅男'
		ELSE '不予考虑'
	END) AS  title
FROM EMP；
```

# 子查询
 对临时表读取数据时，如果当此临时表无法提供需要的数据，此时MYSQL服务器允许开发人员通过一次完整的查询从其他地方得到需要的数据，这个独立的完整的查询语句就是子查询

```
1) FROM： 【可以】为子查询提供操作的临时表
2）WHERE： 【可以】通过子查询得到一个用于判断使用的数据
3）GROUP BY：  后面需要的字段名而不是数据，因此【不可能出现】子查询
4)   HAVING ： 【可以】通过子查询得到一个用于判断使用的数据
5）SELECT：  【可以】通过子查询得到一组用于解释的数据
6)  ORDER BY： 【不需要】使用子查询
7）LIMIT： 【不需要】使用子查询
```

1. 独立子查询: 相当于一个无参方法，在运行时不需要得到当前临时表中数据行内容作为帮助，独立子查询在执行过程中只会执行一次

2. 依赖子查询: 相当于一个有参方法，在运行时需要得到当前临时表中数据行内容作为执行的条件依赖子查询在执行过程中会执行多次。执行次数与当前临时表数据行行数对应。

# 行转列，列转行
MYSQL 的【[行转列](https://blog.csdn.net/lilong329329/article/details/81664451)】用到了 【group by】 和 【case... when】
```
1. 先对临时表进行分组（group by)；
2. 然后对字段进行（case...end）判断，输出所需要的列。

SELECT userid,
	SUM(CASE `subject` WHEN '语文' THEN score ELSE 0 END) as '语文',
	SUM(CASE `subject` WHEN '数学' THEN score ELSE 0 END) as '数学',
	SUM(CASE `subject` WHEN '英语' THEN score ELSE 0 END) as '英语',
	SUM(CASE `subject` WHEN '政治' THEN score ELSE 0 END) as '政治' 
FROM tb_score 
GROUP BY userid
```
MYSQL 的【[列转行](https://blog.csdn.net/lilong329329/article/details/81664451)】用到了 【union alll】

```
1. 分别将指定的字段取出来，然后用 【union all】联合合并

SELECT userid,'语文' AS course,cn_score AS score FROM tb_score1
UNION ALL
SELECT userid,'数学' AS course,math_score AS score FROM tb_score1
UNION ALL
SELECT userid,'英语' AS course,en_score AS score FROM tb_score1
UNION ALL
SELECT userid,'政治' AS course,po_score AS score FROM tb_score1
ORDER BY userid
```

# 运算符号
## 关系运算符
| 符号             | 描述         | 备注                                                         | 示例                            |
| ---------------- | ------------ | ------------------------------------------------------------ | ------------------------------- |
| =                | 等于         |                                                              | select 2 = 3                    |
| <>,!=            | 不等于       |                                                              | select 2 <> 3                   |
| >                | 大于         |                                                              | select 2 > 4                    |
| >=               | 大于等于     |                                                              | select 2 >= 4                   |
| <                | 小于         |                                                              | select 2 < 5                    |
| <=               | 小于等于     |                                                              | select 2 <=5                    |
| between...and    | 在两值之间   | [min,max]                                                    | select 5 between 1 and 10       |
| not between..and | 不在两值之间 | < min && max <                                               | select 5 not between 1 and 10   |
| in               | 在集合中     |                                                              | select 5 in (1,2,3,4,5)         |
| not in           | 不在集合中   |                                                              | select 5 not in (1,2,3,4,5)     |
| like             | 模糊查询     | '%'是一个通配符，表示一个任意长度的字符串；'_'是一个通配符,表示一个任意字符 | select '12345' like '12%'       |
| rlike            | 正则匹配     |                                                              | select 'beijing' REGEXP 'jing'; |
| is null          | 为空         |                                                              | select 'a' is NULL;             |
| is not null      | 非空         |                                                              | select 'a' is not NULL;         |

## 逻辑运算符
| 符号     | 作用     | 示例            |
| -------- | -------- | --------------- |
| NOT 或 ! | 逻辑非   | select not 1;   |
| AND      | 逻辑与   | select 2 and 0; |
| OR       | 逻辑或   | select 2 or 0;  |
| XOR      | 逻辑异或 | select 1 xor 1; |

## 算数运算符
| 符号     | 作用 |
| -------- | ---- |
| +        | 加法 |
| -        | 减法 |
| *        | 乘法 |
| / 或 DIV | 除法 |
| % 或 MOD | 取余 |

# 聚合函数
| 函数名| 作用 |示例|
|--|--|--|
|MAX(column)	| 返回某列的最低值（有则不会返回NULL，没有则返回NULL）| SELECT MAX(SAL) FROM EMP |
|MIN(column)	| 返回某列的最高值（没有则返回NULL）| SELECT MIN(SAL) FROM EMP |
|COUNT(column)  |	返回某列的行数（不包括 NULL 值）| SELECT COUNT(COMM) FROM EMP |
|COUNT(*) |	返回被选列行数（包括NULL）| select  count(*) FROM EMP |
|SUM(column)	 | 求和 |SELECT SUM(SAL) FROM EMP |
|AVG(column)	 | 求平均值 | SELECT AVG(SAL) FROM EMP WHERE DEPTNO =20 |

# 窗口函数
[参考](https://blog.csdn.net/weixin_39010770/article/details/87862407)

```
窗口函数的基本用法

窗口函数 over(子句)

1. over() 是用来指定函数执行的窗口范围，如果括号中没有任何内容，则表示会对【临时表】中所有的行进行操作；
2. 【子句】可以包括：【partition by 子句】，【over by 子句】等。
```
窗口函数包括：
### 序号函数

| ROW_NUMBER()                                           | RANK()                                                       | DENSE_RANK()                                                 |
| ------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 排序，根据查询出的顺序依次标注排名，没有重复。如 1,2,3 | 排序，相同数据标注相同的排名，而下一个不同的数据则被跳跃标注。如，1,1,3 | 排序，相同数据标注相同的排名，下一条不同的数据直接依次标注。如1,1,2 |
```
SELECT id,
	ROW_NUMBER() OVER (PARTITION BY idORDER BY score) AS row_number,
    RANK() OVER (PARTITION BY id ORDER BY score) AS rank,
    DENSE_RANK() OVER (PARTITION BY id ORDER BY score) AS dense_rank
FROM student;
```

### 分布函数

| PERCENT_RANK ()                                              | CUME_DIST()                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| (【根据 Rank()产生的序号】-1) 除以 (【当前窗口的总记录】- 1) | 小于等于(【根据 Rank()产生的序号】除以 【当前窗口的总记录】) |

```
SELECT id,
	PERCENT_RANK() OVER(PARTITION BY id ORDER BY score) AS perk,
	CUME_DIST() OVER(PARTITION BY id ORDER BY score) AS cudt
FROM student;
```

### 前后函数

| LAG(col, n )                                                 | LEAD(col, n )                                                |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| 返回窗口临时表【前 n 行】的 col 列的数值。 col 可以是列名，亦可以是其他值 | 返回窗口临时表 【后 n 行】的 col 列的数值。col 可以是列名，亦可以是其他值 |

```
SELECT id,
	LAG(score,2) OVER(PARTITION BY id ORDER BY score) AS lag_score,
	LEAD(score,2) OVER(PARTITION BY id ORDER BY score) AS lead_score
FROM student;
```

### 头尾函数
| FIRST_VALUE(col )                                            | LAST_VALUE(col)                                              |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| 返回窗口临时表【第一行】的 col 列的数值。col 可以是列名，亦可以是其他值 | 返回窗口临时表 【最后一 行】的 col 列的数值。col 可以是列名，亦可以是其他值 |

```
SELECT NAME, 
	LAST_VALUE(NAME) over(),
	FIRST_VALUE(NAME) over()
FROM student;
```

### 其他函数
| NTH_VALUE(col, n )                                           | NTILE(n )                                 |
| ------------------------------------------------------------ | ----------------------------------------- |
| 返回窗口临时表第 n 个的 col 列的数值。col 可以是列名，亦可以是其他值 | 将分区中的有序数据分为n个等级，记录等级数 |

```
SELECT NAME, 
	NTH_VALUE(score, 3) over(),
	NTILE(4) over()
FROM student;
```