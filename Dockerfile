FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/backend-pizzaria-1.1.0.jar backend-pizzaria-1.1.0.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "backend-pizzaria-1.1.0.jar"]