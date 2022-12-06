FROM yti-docker-java11-base:alpine

RUN apk add --update git

RUN apk add jq gawk openjdk11
ENV PATH="$PATH:/usr/lib/jvm/java-11-openjdk/bin/"

ADD web-api/build/libs/yti-groupmanagement.jar yti-groupmanagement.jar
ADD entrypoint.sh /

ENTRYPOINT ["/entrypoint.sh"]
