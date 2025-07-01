FROM maven:3.9.4-eclipse-temurin-21
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src ./src

ENV SPRING_PROFILES_ACTIVE=prod

CMD ["./mvnw", "spring-boot:run"]


#COPY ./target/DockerWSpring-0.0.1-SNAPSHOT.jar .
#
#CMD ["java", "-jar", "DockerWSpring-0.0.1-SNAPSHOT.jar"]
#
#EXPOSE 8080


