# 高性能MySQL

## MySQL 基准测试

### sysbench

* Centos 和 Mac OS安装参考：`https://github.com/akopytov/sysbench`
* CPU 基准测试：
  * `sysbench --test=cpu --cpu-max-prime=20000 run`
    * 测试计算素数直到某个最大值所需要的时间

# 服务器性能剖析

## 性能优化简介

* 重要的原则：性能就是响应时间
* 就数据库而言，数据库服务器关注的任务是 SQL 语句执行所消耗的时间，即查询的响应时间
* 优化：可以说是尽可能地降低响应时间
* 资源：性能优化就是降低CPU利用率，以减少资源的使用，是一个误区。资源是用来消耗支撑工作的，故而较多资源投入使用，可以加快查询速度
* 吞吐量优化：提升每秒的查询量
* 找出查询消耗较多时间的原因，然后对症优化
* 合适的测量范围：只测量需要优化的活动
* 不合适的测量：
  * 在错误的时间启动和停止测量
  * 测量的是聚合后的信息，而非目标活动本身：抓住主要矛盾不放手

### 通过性能剖析进行优化

* 测量和分析消耗的时间主要方法
* 测量任务花费的时间、对结果进行统计和排序
* 安装 `https://www.cnblogs.com/zishengY/p/6852280.html`
* 执行时间：查询时间 + 等待时间。需要分别就出来并分别分析

### 理解性能剖析

* 判断哪些是值得优化的查询，没有回报的优化就放弃
* 异常情况：对执行频率较低，但是每次都慢，影响用户体验的要优化
* 未知的未知
  * 丢失的时间：任务执行的时间与测量到的时间之间的差值
  * 测量工具没有测量到某些任务、测量工具经度的问题
* 被掩藏的细节
  * 性能剖析只显示了平均时间的行为，无法显示所有的响应时间的分布
  * 需要额外的诸如：直方图、百分比、标准偏差等信息

## 对应用程序进行性能剖析

* 对任何需要消耗时间的任务都可以进行性能剖析。建议自上而下，追踪用户发起到服务器响应时间的整个流程
* 外部资源( 调用了外部服务 ) -> 应用处理大量数据( 分析超大的XML文件 ) -> 循环中执行昂贵的操作( 使用了正则 ) -> 低效的算法 -> 等等

## 剖析 MYSQL 查询

* 剖析整个数据库服务器
* 剖析具体的单个查询

### 剖析服务器负载

* 剖析并找出代价高的查询：慢查询日志

* 捕获 MYSQL 的查询到日志文件中

  * 慢查询
    * 加强版的慢查询日志：设置 `long_query_time = 0` 来捕获所有的查询
    * 查询的响应时间达到了微妙级别
    * 慢查询日志是开销最低、精度最高的工具
    * 可能会消耗大量的磁盘空间，尽量不要长期开启或者使用日志轮转工具
  * 通用日志
    * 很少用于剖析服务器性能、秒级别、意义不大

* 分析查询日志

  * From now on：**利用慢查询捕获服务器上的所有查询，并进行分析**
  * 工具：`pt_query_digest`。将慢查询日志文件传给 **pt_query_digest** 即可
  * 通过 `yum` 安装 `pt_query_digest`。通过 `show variables like '%slow%';` 查找出慢日志文件位置
  * Mac：/usr/local/mysql/data/ACA80168-slow.log
  * Centos： /var/lib/mysql/hostname-slow.log

  ```bash
  $ pt_query_digest hostname-slow.log
  输出如下：
  # 150ms user time, 10ms system time, 25.75M rss, 220.07M vsz
  # Current date: Sun Nov 11 15:13:36 2018
  # Hostname: iZbp11jt0i73lffge9yew8Z
  # Files: iZbp11jt0i73lffge9yew8Z-slow.log
  # Overall: 1 total, 1 unique, 0 QPS, 0x concurrency ______________________
  # Time range: all events occurred at 2018-11-11T03:33:50
  # Attribute          total     min     max     avg     95%  stddev  median
  # ============     ======= ======= ======= ======= ======= ======= =======
  # Exec time            10s     10s     10s     10s     10s       0     10s
  # Lock time              0       0       0       0       0       0       0
  # Rows sent              1       1       1       1       1       0       1
  # Rows examine           0       0       0       0       0       0       0
  # Query size            29      29      29      29      29       0      29
  
  # Profile
  # Rank Query ID                           Response time  Calls R/Call  V/M
  # ==== ================================== ============== ===== ======= ===
  #    1 0x3E9BCB8A9A63A4079C8EDD53C741D289 10.0003 100.0%     1 10.0003  0.00 SELECT
  
  # Query 1: 0 QPS, 0x concurrency, ID 0x3E9BCB8A9A63A4079C8EDD53C741D289 at byte 0
  # This item is included in the report because it matches --limit.
  # Scores: V/M = 0.00
  # Time range: all events occurred at 2018-11-11T03:33:50
  # Attribute    pct   total     min     max     avg     95%  stddev  median
  # ============ === ======= ======= ======= ======= ======= ======= =======
  # Count        100       1
  # Exec time    100     10s     10s     10s     10s     10s       0     10s
  # Lock time      0       0       0       0       0       0       0       0
  # Rows sent    100       1       1       1       1       1       0       1
  # Rows examine   0       0       0       0       0       0       0       0
  # Query size   100      29      29      29      29      29       0      29
  # String:
  # Hosts        localhost
  # Users        root
  # Query_time distribution
  #   1us
  #  10us
  # 100us
  #   1ms
  #  10ms
  # 100ms
  #    1s
  #  10s+  ################################################################
  # EXPLAIN /*!50100 PARTITIONS*/
  select sleep(10) as a, 1 as b\G
  ```

* 其余略之

# schema 和数据类型优化

* 反范式的设计可以加快某些类型的查询，但是也会使得另一些查询变慢

## 选择优化的数据类型

* 更小的通常更好：选择可以正确存储数据的最小数据类型。通常更快：占用更少的磁盘、内存、CPU缓存
* 简单就好：简单的通常需要更少的CPU周期。整型比字符串的代价地：字符集和校对规则使字符比整型更复杂
* 避免使用 NULL：通常情况尽量指定为 not null
  * NULL 列使得 MYSQL 的优化更难，NULL 列使得索引、索引统计和值比较都更复杂
  * NULL 列会使用更多空间，mysql 也需要额外处理
  * NULL 列被索引时，每个索引记录需要一个额外的字节
  * 将 NULL 列改为 NOT NULL 列带来的性能提升比较小，但仍然需要避免使用
* 为列选择合适的数据类型
  * 数字、字符、时间
  * 如：DATETIME、TIMESTAMP 都可以存储时间和日期，精确到秒
    * TIMESTAMP 只使用 DATTIME 一半的存储空间
    * TIMESTAMP 会根据时区变化，具有自动更新能力
    * TIMESTAMP 允许的时间范围较小，有时候会成为障碍

### 整数类型

* 两种类型的数字：整数和实数
* 整数
  * TINYINT、SMALLINT、MIDIUMINT、INT、BIGINT，分别使用 8、16、24、32、64 位存储空间
  * 整型选择 UNSIGNED 表示不允许负值，可以正数的上限提高一倍。但是性能相同

### 实数类型

* 带有小数部分的数字。可以使用 DECIMAL 存储比 BIGINT 还大的整数
* DECIMAL：存储精确的小数。MySQL 5.0和更高版本将数字打包保存到一个二进制字符串中，每4字节存储9个数字
  * DECIMAL(18,9)：小数点两边将各存储9个数字，各需要4个字节，小数点本身占用一个字节，共需9字节
  * DECIMAL 是一种存储形式，在计算中会转换为 DOUBLE 类型（代码中为 double）
* 浮点类型 FLOAT 和 DOUBLE。比 DECIMAL 占用更少的空间
  * FLOAT 占用4字节
  * DOUBLE 占用8字节，比 FLOAT 有更高的精度和范围
* 只在有小数时才使用DECIMAL，因为DECIMAL需要额外的空间

### 字符串类型

* VARCHAR

  * 存储可变长字符串，常用于存储字符串。比定长类型更省空间。需要额外1~2个字节记录字符串长度
  * update 时可能需要做额外的工作，因为涉及到更新的数据变化导致其存储可能发生变化，页分裂等

* CHAR

  * 定长存储。适合存储较短的字符串，或者存储的数据都接近一个长度，适合存储密码的 MD5 值
  * 也适合于经常变更的数据，不容易产生碎片
  * 不需要字符长度的额外记录所需要的一字节空间
  * 只分配真正需要的空间

* BLOB 和 TEXT 先略过

* ENUM 代替字符串

  * 存储时比较紧凑。会根据列表的值的数量将其压缩到一或者两个字节中，mysql 内部会将每个值在列表中的位置保存为整数，并在表的.frm 文件中保存数字和字符串的映射关系
  * 且其内部是按照映射中的数字排序的，而非字符串顺序

  ```mysql
  mysql> create table enum_test (
  	-> e ENUM('fish', 'apple', 'dog') not null   
  	-> );
  mysql> insert into enum_test (e) values ('fish'), ('dog'),('apple');
  mysql> select e + 0 from enum_test;
  +-------+
  | e + 0 |
  +-------+
  |	  1 |
  |	  3 |
  |	  2 |
  +-------+
  ```

  * 由上可见，避免在 enum 中存储数字

### 日期和时间类型

* DATETIME
  * 保存较大的范围，从1001 年到 9999 年，精度为秒
  * 8字节存储空间
  * UNIX_TIMESTAMP( ) 函数将日期转换为 Unix 时间戳
* TIMESTAMP
  * 保存了自 1970年1月1日午夜以来的秒数，和 Unix 时间戳相同
  * 从 1970 年到 2038 年
  * 4字节存储空间
  * FROM_UNIXTIME( ) 函数将 Unix 时间戳转换为日期
* 尽量使用 TIMESTAMP，比DATETIME空间效率更高
* 不推荐整数保存时间戳，处理不方便且没有任何收益

### 位数据类型

* BIT，尽量避免使用之
* 其余目前忽略

### 选择标识符

* 整数类型，是标识列最好的选择，很快且可以使用自增
* ENUM 和 SET：尽量避免使用
* 字符串：消耗较大的空间，且比数字类型慢。也尽量避免
* 注意：一些随机产生的数据，如 MD5、SHA1、UUID 等函数产生的数据通常会分布在很大的空间内
  * 导致 INSERT 变慢：随机值会随机到索引不同的位置，分不到随机的页中或者页分裂
  * 导致 SELECT 变慢：导致逻辑上相邻的行会分布到磁盘或者内存的不同地方

## Schema 设计中的陷阱

* 太多的列：数千个字段
* 太多的关联：每个关联操作只能有61个表，最好在12个以内
* 全能的枚举：防止过度的枚举，即枚举的字段值分布过多
* 无能为力的时候可以设置 NULL 值

## 范式和反范式

* 范式：表更小，更新操作较快，数据冗余较少；关联操作可能过多，可能导致索引失效
* 反范式：避免了过多的关联，更有效地使用索引
* 混合使用

## 缓存表和汇总表

* 满足检索的需求时，可以创建完全独立的表
* 物化视图和计数器表

# 创建高性能索引

