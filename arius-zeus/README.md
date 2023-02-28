## 安装GOLANG环境

```
yum -y install golang
```

## 编译Zeus

```
git clone git@github.com:didi/KnowSearch.git
```

- 编译zeus-agent

```
cd KnowSearch/arius-zeus/agent
go mod init
go mod tidy
./contor build
```

- 编译zeus-exec

```
cd KnowSearch/arius-zeus/exec
./contor build
```

- 编译zeus-scheduler

```
cd KnowSearch/arius-zeus/scheduler
./contor build
```

- 编译zeus-web

```
cd KnowSearch/arius-zeus/web
./contor build
```

## 安装Zeus

[Zeus安装部署手册](https://github.com/didi/KnowSearch/blob/0.3.2/doc/KnowSearch-%E9%83%A8%E7%BD%B2%E6%96%87%E6%A1%A3.md )
