FROM openjdk:8-alpine
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY ./target/eventbus-sdk-demo-1.0-SNAPSHOT.jar /home
ENTRYPOINT java $JAVA_OPTS -jar /home/eventbus-sdk-demo-1.0-SNAPSHOT.jar