FROM openjdk:17-oracle
ENV SPRING_PROFILES_ACTIVE=docker
WORKDIR /kio
COPY ../build/libs/*.jar .
ENTRYPOINT ["java", "-jar", "some.jar"]