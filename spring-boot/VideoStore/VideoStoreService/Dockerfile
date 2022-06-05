#FROM openjdk:8-jdk-alpine
FROM openjdk:11
COPY ./target/*.jar /Documents/videostore/demo.jar
WORKDIR /Documents/videostore
RUN sh -c 'touch demo.jar'
ENTRYPOINT ["java", "-jar", "demo.jar"]
