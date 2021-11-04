<p align="center">
  <img src="https://raw.githubusercontent.com/ZhouYixun/sonic-server/main/logo.png">
</p>
<p align="center">🎉Sonic Cloud Real Machine Testing Platform</p>
<p align="center">
  <span>English |</span>
  <a href="https://github.com/ZhouYixun/sonic-server/blob/main/README_CN.md">  
     简体中文
  </a>
</p>
<p align="center">
  <a href="#">  
    <img src="https://img.shields.io/badge/release-v1.1.0-orange">
  </a>
  <a href="https://hub.docker.com/repository/docker/zhouyixun/sonic-server-simple">  
    <img src="https://img.shields.io/docker/pulls/zhouyixun/sonic-server-simple">
  </a>
  <a href="https://github.com/ZhouYixun/sonic-server/blob/main/LICENSE">  
    <img src="https://img.shields.io/github/license/ZhouYiXun/sonic-server?color=green&label=license&logo=license&logoColor=green">
  </a>
</p>

### Official Website
[Sonic Official Website](http://zhouyixun.gitee.io/sonic-official-website)
## Background

#### What is sonic ?

> Nowadays, automatic testing, remote control and other technologies have gradually matured. [Appium](https://github.com/appium/appium) can be said to be the leader in the field of automation, and [STF](https://github.com/openstf/stf) is the ancestor of remote control. A long time ago, I began to have an idea about whether to provide test solutions for all clients (Android, IOS, windows, MAC and web applications) on one platform. Therefore, sonic cloud real machine testing platform was born.

#### Vision

> Sonic's vision is to help small and medium-sized enterprises solve the problem of lack of tools and testing means in client automation or remote control.
>
>If you want to participate, welcome to join! 💪
>
>If you want to support, you can give me a star. ⭐

#### What can sonic do ?

+ 0 coding for automated testing
+ Make full use of devices (24hours)
+ Remotely control your device (Android,iOS)
+ Perform UI automation tests,Stability tests and Traversal tests with devices
+ Connect CI/CD platform (Jenkins)
+ Visual report
+ And more...

## How to package

```
mvn package
```

## Deployment mode

```
docker-compose up -d
```
|  ENV Name   | Description  |
|  ----  | ----  |
| RABBIT_HOST  | RabbitMQ service host,default **localhost** |
| RABBIT_PORT  | RabbitMQ service port,default **5672** |
| RABBIT_USERNAME  | RabbitMQ service username,default **sonic** |
| RABBIT_PASSWORD  | RabbitMQ service password,default **sonic** |
| RABBIT_VHOST  | RabbitMQ service virtual-host,default **sonic** |
| REDIS_DATABASE  | redis database,default **0** |
| REDIS_HOST  | redis host,default **localhost** |
| REDIS_PORT  | redis port,default **6379** |
| MYSQL_HOST  | mysql host,default **localhost** |
| MYSQL_DATABASE  | mysql database,default **sonic** |
| MYSQL_USERNAME  | mysql username,default **root** |
| MYSQL_PASSWORD  | mysql password,default **Sonic!@#123** |
## LICENSE

[MIT License](LICENSE)
