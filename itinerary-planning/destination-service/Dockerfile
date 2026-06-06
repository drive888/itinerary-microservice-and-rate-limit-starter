FROM amazoncorretto:17
VOLUME /tmp
COPY target/destination-service-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=docker"]
