# Builds and runs the Java web version (no libraries needed)
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY *.java ./
RUN javac *.java
ENV PORT=8080
EXPOSE 8080
CMD ["java", "WebServer"]
