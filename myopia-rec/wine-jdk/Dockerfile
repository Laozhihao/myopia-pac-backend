FROM tianon/wine:latest

#  系统字符集
ENV LC_ALL C.UTF-8
ENV LANG C.UTF-8
ENV LANGUAGE C.UTF-8

# 软件目录及软件运行时的目录
RUN  mkdir -p /app
RUN  mkdir -p /root/.wine
COPY EpiC.exe /app

VOLUME /root/.wine

#  jdk环境
RUN  mkdir -p /usr/local/java
#  jdk安装包太大自己下载
COPY jdk1.8.0_161.tar.gz  /usr/local
ADD  jdk1.8.0_161.tar.gz /usr/local/java

ENV JAVA_HOME /usr/local/java/jdk1.8.0_161
ENV PATH $PATH:$JAVA_HOME/bin


