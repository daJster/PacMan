FROM openjdk:21-jdk-slim

WORKDIR /app

COPY ./app/build/libs/PacMan.jar PacMan.jar

CMD ["java", "-jar", "PacMan.jar"]

