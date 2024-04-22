FROM amazoncorretto:21-alpine

ARG JAR_FILE=target/movie-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]