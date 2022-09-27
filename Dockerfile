FROM openjdk:11-slim

RUN mkdir -p /opt/logs

COPY target/account-permission-management.jar /opt/account-permission-management.jar

VOLUME /opt/config

EXPOSE 8080

CMD [ "java", "-jar", "/opt/account-permission-management.jar", "--spring.config.location=/opt/config/application.properties,classpath:/application.properties" ]
