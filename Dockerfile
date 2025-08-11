# 使用官方的 Java 运行时作为父镜像
FROM openjdk:17-jdk-alpine

# 设置工作目录
WORKDIR /app

# 将本地 jar 包复制到容器中
COPY target/yupao-backend-0.0.1-SNAPSHOT.jar app.jar

# 设置容器启动时执行的命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# 暴露应用端口
EXPOSE 8091