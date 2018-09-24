# 介绍

## 核心概念

### Pod

* Pod 是若干相关容器的组合，Pod 包含的容器运行在同一台宿主机上，这些容器使用相同的网络命名空间、IP地址和端口，相互之间能通过 localhost 来发现和通信
* Pod 内的容器可以共享一块存储卷空间
* 在 Kubernetes 中创建、调度和管理的最小单位是 Pod，而不是容器

### Replication Controller

* 用来控制管理 Pod 副本（Replica，或者称为实例），Replication Controller确保任何时候Kubernetes集群中有指定数量的Pod副本在运行
* 如果少于指定数量的Pod副本，Replication Controller会启动新的Pod副本，反之会杀死多余的副本以保证数量不变
* 是弹性伸缩、滚动升级的实现核心

### Service

* Service是真实应用服务的抽象，定义了Pod的逻辑集合和访问这个Pod集合的策略
* Service将代理Pod对外表现为一个单一访问接口，外部不需要了解后端Pod如何运行，这给扩展和维护带来很多好处，提供了一套简化的服务代理和发现机制

### Label

* Label是用于区分Pod、Service、Replication Controller的Key/Value对
* Kubernetes中的任意API对象都可以通过Label进行标识
* 每个API对象可以有多个Label，但是每个Label的Key只能对应一个Value。
* 是Service和Replication Controller运行的基础，它们都通过Label来关联Pod

### Node

* Kubernetes属于主从分布式集群架构，Kubernetes Node 运行并管理容器
* Node作为Kubernetes的操作单元，用来分配给Pod（或者说容器）进行绑定，Pod最终运行在Node上，Node可以认为是Pod的宿主机

## 入门

* 在 Kubernetes 中，service 服务是分布式集群架构的核心，一个 service 对象的核心特征
  * 有唯一指定的名字
  * 有一个虚拟 IP 和端口
  * 能提供远程服务能力
  * 被映射到了提供这种服务能力的一组容器上
* Kubernetes 设计了 Pod 对象，将服务进程包装到相应的 Pod 中，使其成为 Pod 中运行的一个容器
* 

