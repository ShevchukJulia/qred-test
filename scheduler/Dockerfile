FROM openjdk:8-jdk-alpine
COPY ./target/scheduler-1.0-SNAPSHOT.jar scheduler-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/scheduler-1.0-SNAPSHOT.jar"]
