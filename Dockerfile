# syntax=docker/dockerfile:1.6

# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 先拷贝 pom.xml 利用 Docker 层缓存预下载依赖
COPY pom.xml .
RUN mvn -B -e -q dependency:go-offline

# 拷贝源码并构建
COPY src ./src
RUN mvn -B -e -DskipTests clean package \
    && cp target/*.jar app.jar

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# 创建非 root 用户与持久化数据目录
RUN useradd -r -u 1001 -g root appuser \
    && mkdir -p /app/data \
    && chown -R appuser:root /app

COPY --from=builder --chown=appuser:root /build/app.jar /app/app.jar

# H2 数据库文件持久化目录
VOLUME ["/app/data"]

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
