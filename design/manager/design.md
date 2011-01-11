Amoeba Manager Design
=====================

查看已部署的Amoeba实例信息，帮助运维人员统一管理系统状态，统一进行操作。

AmoebaInstance
--------------

用于描述Amoeba实例，包含一个Status对象。用状态模式描述Amoeba实例的属性

LifeCycleState
---------------

### LifeCycleStatePlanned 

机器上架，操作系统安装完成，SSH安装完成，用户名密码齐备。

* hostname
* username
* password

### LifeCycleStateInstalled

Amoeba软件安装完成

* installedVersion
* installedPath
* username
* configuration
* installedDateTime
* installedModules

### LifeCycleStateInstallFailed

Amoeba软件安装失败

* failedReason
* log

### LifeCycleStateStarted

启动命令发出

* hostname
* startDateTime
* configuration

### LifeCycleStateUp

正常运行

* uptime
* workingPort
* controlPort
* pid
* username
* hostname

### LifeCycleStateDown

心跳请求无响应，服务不可用

* lastSuccess
* downtime
* hostname

### LifeCycleStateStopped

服务关闭命令发出，确认关闭后恢复到Installed状态

* stoppedTime


Operation
---------

描述对实例的操作，分为ManualOperation和AutoOperation。

现在想到的一些：

* Register (注册新的Planned Server)
* Install
* Start
* Stop
* Restart
* Reload (更新配置)

这些操作都有Manual实现和Auto实现，Manual实现仅仅更新LifeCycleStatus，Auto实现需要去做事。
每个Operation只对特定的Status可用。

Logging
-------

自动的日志系统，记录所有Operation和Status变化都被记录日志。

Manager-Server 通信
-------------------
TODO




