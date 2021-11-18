FROM maven:3.5.3-jdk-8-slim as builder

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY ./src ./src
RUN mvn package


FROM adoptopenjdk/openjdk11:alpine-slim
COPY --from=builder target/performance-tests-jar-with-dependencies.jar /performance-tests.jar

CMD exec java $JAVA_OPTS -jar /performance-tests.jar