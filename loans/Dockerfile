FROM openjdk:8-jdk-alpine
COPY ./target/loans-1.0-SNAPSHOT.jar loans-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/loans-1.0-SNAPSHOT.jar"]
