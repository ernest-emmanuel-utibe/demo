#FROM openjdk:11-jre-slim
#
## Set the working directory to /app
#WORKDIR /app
#
## Copy the application JAR file into the container at /app
#COPY target/demo-0.0.1-SNAPSHOT.jar .
#
## Run the application when the container launches
#CMD ["java", "-jar", "demo.jar"]

FROM openjdk:17

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:resolve


COPY src ./src

EXPOSE 8081

CMD ["./mvnw", "spring-boot:run"]