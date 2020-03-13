FROM java:8-jdk-alpine

COPY ./target/spring-boot-camel-0.0.1-SNAPSHOT.jar /usr/app/

WORKDIR /usr/app

RUN sh -c 'touch spring-boot-camel-0.0.1-SNAPSHOT.jar'

ENTRYPOINT ["java","-jar","spring-boot-camel-0.0.1-SNAPSHOT.jar"]