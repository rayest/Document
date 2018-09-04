# 简单动态字符串

> simple dynamic string ：SDS。Redis 默认的字符串表示。
>
> 保存数据库中的字符串值、作为缓冲区(AOF 缓冲)、客户端状态的输入缓冲区

## 定义

* 示例

  `struct sdshdr {`

  	`int len;`
	
  	`int free;`
	
  	`char buf[]`

  `}`

* SDS 以空字符结尾‘\0’，但是不计入 SDS 的 len 属性中，分配额外的 1 字节空间

## SDS 与 C 字符串的区别

### 获取字符串长度

* SDS 可以通过 len 属性直接获取，O(1)

### 杜绝缓冲区溢出

* SDS 的空间分配策略杜绝了发生缓冲区溢出的问题

* 当 SDS API 对 SDS 进行修改时，API 先检查 SDS 空间是否满足修改所需的要求。如果不满足，则自动将 SDS 的空间扩展至所需要的大小，再执行修改操作

  > 如：当执行字符串拼接操作时，SDS API 会检查 SDS 空间是否满足拼接后字符串所需要的空间，如果不满足，则 SDS 会扩充空间至所需要的值，再执行拼接操作

* 减少字符串修改带来的内存重分配次数

  > SDS 通过未使用空间解除了字符串长度和底层数组长度之间的关系。即：buf 数组长度未必等于字符串长度再加 1，数组中可以包含未使用的字节。通过 free 属性记录这些未使用的字节数量 

  > 2 种优化策略：空间预分配、惰性空间释放

  * 空间预分配

    * 当 SDS 空间需要扩展时，程序不仅会为其分配所必要大小的空间，还会为其分配额外的未使用的空间
    * 减少了连续执行字符串增长操作所需要的内存分配次数

  * 惰性空间释放

    * 当字符串执行缩短操作时，程序并不立即释放未使用的空间，而是通过 free 属性记录未使用的空间，并等将来使用
    * 在真正需要的时候，通过 SDS API 释放这些未使用的空间

# 链表

## 链表和链表节点的实现

  `typedef struct listNode {`

  	`struct listNode *prev;`

  	`struct listNode *next;`

  	`void *value`;

  `}`

`typedef struct list{`

​	`listNode *head;`

​	`listNode *tail;`

​	`unsigned long len`；

​	`……`

`}`

* 多个 listNode 通过 prev 和 Next 指针实现双端链表
* listNode 中 value 属性记录了节点的值
* 链表由 list 结构和 listNode 结构组成
* list 结构的 head 指针指向由 listNode 组成的双端链表的首个 listNode
* list 结构的 tail 指针指向由 listNode 组成的双端链表的最后一个 listNode 
* list 结构的 len 属性记录了链表的长度，listNode 的数量
* 链表的头结点 prev 指针和尾节点 Next 指针都指向 null，表示访问终点

# 字典

## 实现

### 哈希表

`typedef struct dictht{`

​	`dictEntry **table;`

​	`unsigned long size;`

​	`unsigned long sizemask;`

​	`unsigned long used;`

`}`

* 每一个哈希表中可以有多个哈希表节点，每一个哈希表节点保存了字典中的一个键值对
* table 属性是一个数组，数组中的每个元素都是 dictEntry 结构的指针
* 每个 dictEntry 结构都保存了一个键值对
* size 值记录了哈希表的大小（table 数组的大小）
* used 记录了哈希表目前已有的哈希节点数量
* sizemask 值总是 size - 1，用于计算索引值，即决定一个键应该被放到table数组的哪一个索引位置

### 哈希表节点

`typedef struct dictEntry {`

​	`void *key;`

​	  `union {`值`} value;`

​	`struct dictEntry *next`

`}`

* key 是键
* value 是值
* Next 指针指向下一个哈希表节点。该指针可以将多个哈希值相同的键值对连接在一起，以解决哈希冲突

### 字典

* 一个普通的字典结构由2个哈希表组成，ht[0] 用于实际存储键值对数据，ht[1] 用于rehash备用

## 哈希算法

> 当一个新的键值对需要添加到字典中时，需要先根据键计算出哈希值和索引值，根据索引值，将该新的键值对所在的节点放到哈希表数组指定的索引位置

## 解决键冲突

> 当两个或者两个以上的键在经过哈希算法计算后，被分配到哈希数组的同一个索引上时，即发生了键冲突

### 链地址法

> 解决键冲突

* 当出现键冲突时，具有相同索引的节点之间通过 Next 指针连接形成单向链表
* dictEntry 节点组成的链表没有指向链尾的指针，为考虑速度性能，新增的节点插入到链表的表头位置

## Rehash

* 当对字典的插入或者删除操作很多时，哈希表保存的键值对会逐渐地增加或者减少，为使哈希表的负载因子依然合理，当哈希表中键值对数量过多或者过少时，需要对哈希表的大小进行扩展或者收缩
* 为字典的 ht[1] 表分配空间，该空间大小取决于要执行的操作以及 ht[0] 包含的键值对数量
* 负载因子：ht[0].used / ht[0].size
* 当负载因子小于 0.1，自动收缩
* 当服务器目前没有执行 BGSAVE 或者 BGWRITEAOF 命令时，负载因子大于等于 1 时，自动扩展
* 当服务器目前正在执行 BGSAVE 或者 BGWRITEAOF 命令时，负载因子大于等于 5 时，自动扩展

