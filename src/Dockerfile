FROM openjdk:8-jre
ADD target/k8s-spring-boot-demo-0.0.1-SNAPSHOT app.jar
EXPOSE 8080
EXPOSE 8081
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
