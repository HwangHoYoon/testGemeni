FROM openjdk:17-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} testGemeni-0.0.1-SNAPSHOT.jar
ENV TZ Asia/Seoul
ENTRYPOINT ["java", "-Dspring.profiles.active=real", "-jar","/testGemeni-0.0.1-SNAPSHOT.jar"]