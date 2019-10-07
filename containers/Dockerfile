FROM openjdk:11

ENV JAVA_VERSION=11  GRADLE_VERSION=5.6.2

RUN echo 'install  jpro' && \
    apt-get update  && DEBIAN_FRONTEND=noninteractive apt-get install -yq --no-install-recommends \
    xorg gtk2-engines libasound2 libgtk2.0-0 curl unzip libgtk-3-dev xorg
    
# Install Gradle
RUN echo "Install Gradle" \
    && curl -LSso /var/tmp/gradle-$GRADLE_VERSION-bin.zip https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip \
    && unzip /var/tmp/gradle-$GRADLE_VERSION-bin.zip -d /opt \
    && update-alternatives --install /usr/local/bin/gradle gradle /opt/gradle-$GRADLE_VERSION/bin/gradle 10000 \
    && rm -fr /var/lib/apt/lists/* /tmp/* /var/tmp/* \
    && apt-get clean
# sets the working dir for any RUN, CMD, ENTRYPOINT, COPY and ADD instructions that follow 
WORKDIR /src
COPY ./jpro  .
RUN chmod -R 777 .
CMD ["gradle", "clean", "build", "jproRun", "-x", "test"]