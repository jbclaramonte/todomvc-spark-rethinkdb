FROM java:8-jre-alpine

ADD ./target/todomvc-jar-with-dependencies.jar /

EXPOSE 4567
CMD ["java", "-jar", "/todomvc-jar-with-dependencies.jar"]