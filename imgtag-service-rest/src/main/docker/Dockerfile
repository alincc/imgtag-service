FROM java:8
ADD imgtag-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]