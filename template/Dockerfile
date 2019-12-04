# tag: roxa/myapp:latest
FROM roxa/java:8u212
LABEL maintainer="steven@abacus.com.cn"

ENV ARTIFACT_ID <ARTIFACT_ID>
ENV ARTIFACT_VERSION 0.0.1-SNAPSHOT

ENV ARTIFACT_HOME /opt/vertxapp
ENV ARTIFACT_CFG_LOCATION $ARTIFACT_HOME/conf
ENV ARTIFACT_FILE $ARTIFACT_ID-$ARTIFACT_VERSION-fat.jar

EXPOSE 8181

COPY target/$ARTIFACT_FILE $ARTIFACT_HOME/
COPY deploy/conf $ARTIFACT_CFG_LOCATION/
COPY src/main/resources/log4j2.xml $ARTIFACT_CFG_LOCATION/
COPY docker-entrypoint.sh $ARTIFACT_HOME/

RUN chmod +x $ARTIFACT_HOME/docker-entrypoint.sh

VOLUME ["/var/roxa", "$ARTIFACT_CFG_LOCATION", "/var/mvn"]

WORKDIR $ARTIFACT_HOME
ENTRYPOINT ["./docker-entrypoint.sh"]
CMD ["run"]