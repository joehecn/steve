# 服务部署

## 沙盒环境

- 流水线已经配置沙盒环境自动部署, 提交代码到 joe 分支即可触发部署

```bash
git push codeup
```

- 环境变量: 见文件 `src/main/resources/config/docker/main.properties`

## 多环境部署

- 环境配置目录: `src/main/resources/config`
- 使用多环境部署时, 需要执行运行的环境变量 `SERVER_URL=prod`
- Docker 运行:

```bash
docker run -d \
  --restart=unless-stopped \
  -p 8180:8180 \
  --env SERVER_ENV=prod \
  --network mega-net \
  --network-alias steve-app \
  --link mysqldb:mariadb \
  --name steve steve
```

## 线上部署完整流程

- 注意: `${DATE}` 为新的进项打包时间

1. 提交代码到 Code Up: `git push codeup`
2. 触发流水线: 上传 Docker 镜像
3. 拉取最新镜像: `docker pull registry.cn-hongkong.aliyuncs.com/mega_v3/steve:${DATE}`
4. 重命名镜像: `docker tag registry.cn-hongkong.aliyuncs.com/mega_v3/steve:${DATE} steve:${DATE}`
5. 停止原有的 Steve 服务
6. 启动新的 Steve 服务 `docker run -d --restart=unless-stopped -p 8180:8180 --env SERVER_ENV=prod --network mega-net --network-alias steve-app --link mysqldb:mariadb --name steve_${DATE} steve:${DATE}`