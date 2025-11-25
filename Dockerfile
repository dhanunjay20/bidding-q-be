# Event Bidding application Dockerfile
FROM openjdk:26-ea-jdk-slim
LABEL maintainer="appupiratla@gmail.com"
LABEL version="1.0"
LABEL description="Event Bidding application"

WORKDIR /app

# Provide the exact jar to copy via build arg so wildcard expansion doesn't accidentally match multiple files
ARG JAR_FILE=target/bidding-*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

# keep memory settings modest for container environments
ENTRYPOINT ["java","-Xms256m","-Xmx512m","-jar","/app/app.jar"]
