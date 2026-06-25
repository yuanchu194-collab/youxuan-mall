# 优选商城 Docker 环境说明

## 启动 Docker 环境

docker compose -f docker\docker-compose.yml up -d

## 停止 Docker 环境

docker compose -f docker\docker-compose.yml down

## 查看容器

docker ps
docker ps -a

## MySQL

Host: localhost
Port: 3307
Database: youxuan_mall
User: root
Password: root

IDEA 连接 URL:
jdbc:mysql://localhost:3307/youxuan_mall?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true

## Redis

Host: localhost
Port: 6379
Password: 无

## RabbitMQ

管理页面: http://localhost:15672
User: guest
Password: guest
AMQP Port: 5672

## Nacos

地址: http://localhost:8848/nacos
User: nacos
Password: nacos

## Elasticsearch

地址: http://localhost:9200
账号密码: 无

## MinIO

Console: http://localhost:9101
API: http://localhost:9100
User: minioadmin
Password: minioadmin
Bucket: youxuan-mall

## 未来 Gateway

地址: http://localhost:9000

## 常用日志命令

docker logs youxuan-mysql
docker logs youxuan-redis
docker logs youxuan-rabbitmq
docker logs youxuan-nacos
docker logs youxuan-elasticsearch
docker logs youxuan-minio
