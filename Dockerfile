FROM eclipse-temurin:21-jdk

WORKDIR /App

COPY target/author_book_management-0.0.1-SNAPSHOT.jar app.jar

# Container's port
EXPOSE 8081

# What to run
CMD ["java", "-jar", "app.jar"]