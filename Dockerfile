# Dependency images
FROM yti-spring-security:latest as yti-spring-security
FROM yti-common-ui:latest as yti-common-ui

# Builder image
FROM gradle:6.9-jdk11 as builder

# Copy yti-spring-security dependency from MAVEN repo
COPY --from=yti-spring-security /root/.m2/repository/fi/vm/yti/ /root/.m2/repository/fi/vm/yti/

# Copy yti-common-ui dependency from image
COPY --from=yti-common-ui /app/dist/yti-common-ui /yti-common-ui/dist/yti-common-ui

# Set working dir
WORKDIR /app

# Copy source file
COPY frontend frontend
COPY web-api web-api
COPY build.gradle .
COPY settings.gradle .

# Build project
RUN gradle assemble -x test --no-daemon

# Pull base image
FROM yti-docker-java11-base:alpine

# Copy from builder 
COPY --from=builder /app/web-api/build/libs/yti-groupmanagement.jar ${deploy_dir}/yti-groupmanagement.jar
ADD entrypoint.sh /

ENTRYPOINT ["/entrypoint.sh"]
