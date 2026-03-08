FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/backend-pizzaria-1.1.0.jar backend-pizzaria-1.1.0.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "backend-pizzaria-1.1.0.jar"]