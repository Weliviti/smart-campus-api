# ---- build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -ntp -q dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -q -DskipTests package

# ---- runtime stage ---------------------------------------------------------
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /src/target/smart-campus-api.jar /app/smart-campus-api.jar

ENV JAVA_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/smart-campus-api.jar"]
