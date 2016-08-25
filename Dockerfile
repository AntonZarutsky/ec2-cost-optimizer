FROM registry.opensource.zalan.do/stups/openjdk:8-34
MAINTAINER anton.zarutsky@zalando.de

RUN apt-get update \
     && apt-get install -y mc \
     && apt-get install -y wget

WORKDIR /opt

EXPOSE 8080

COPY ./target/root.jar /opt/root.jar
COPY ./run.sh /opt/run.sh

COPY target/scm-source.json /scm-source.json

CMD ./run.sh