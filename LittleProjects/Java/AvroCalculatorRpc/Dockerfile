FROM gradle:jdk8
COPY . /app
WORKDIR /app
RUN gradle fatJar && \
    mv /app/build/libs/avro-calculator-rpc-all-1.0-SNAPSHOT.jar /app/application.jar
