# Build stage: compile the server into a single fat jar
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew ./
COPY gradle/ gradle/
COPY server/ server/
RUN ./gradlew -p server buildFatJar --no-daemon

# Runtime stage: only the JRE and the jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/server/build/libs/server.jar server.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "server.jar"]