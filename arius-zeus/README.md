## GOLANG环境

Go语言安装

```
yum -y install golang
```

配置 GOPATH

```
mkdir -p $HOME/go/apps/src
export GOPATH=$HOME/go/apps
```

## Zeus代码配置

```
git clone git@github.com:didi/KnowSearch.git
git checkout 0.3.2
将arius-zeus的代码拷贝到$GOPATH/src目录下,修改arius-zeus名称为zeus
```

## Zeus代码编译

- 编译zeus-agent

```
cd $GOPATH/src/zeus/agent
./contor build
```

- 编译zeus-exec

```
cd $GOPATH/src/zeus/exec
./contor build
```

- 编译zeus-scheduler

```
cd $GOPATH/src/zeus/scheduler
./contor build
```

- 编译zeus-web

```
cd $GOPATH/src/zeus/web
./contor build
```

## 安装Zeus

[Zeus安装部署手册](https://github.com/didi/KnowSearch/blob/0.3.2/doc/KnowSearch-%E9%83%A8%E7%BD%B2%E6%96%87%E6%A1%A3.md )
