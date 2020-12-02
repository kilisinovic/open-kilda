ARG base_image=ubuntu:xenial
FROM ${base_image}

ENV DEBIAN_FRONTEND noninteractive

####################################
# Using base image from Open Kilda #
####################################

ENV JAVA_VER 8
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV JAVA_MEM_CONFIG "-XX:+PrintFlagsFinal -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
ENV DEBIAN_FRONTEND noninteractive

COPY app /app

RUN chmod 777 /app/* \
    && apt-get -q -o Acquire::CompressionTypes::Order=bz2 update \
    && apt-get install -yq --no-install-recommends \
        libffi-dev \
        libssl-dev \
        software-properties-common \
        curl \
        wget \
        ssh \
        iputils-ping \
        python \
        python-dev \
        python-pip \
        make \
        apt-transport-https \
        openjdk-8-jdk \
        iptables \
        iproute2 \
        rsync \
    && pip install --upgrade pip \
    && python -m pip install setuptools \
    && echo "export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64" >> ~/.bashrc \
    && rm -rfv /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Only this part will be used once added to Open Kilda - docker registry
ADD target/netconfspeaker-southbound.jar /app/
WORKDIR /app
CMD ["java", "-XX:+PrintFlagsFinal", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "netconfspeaker-southbound.jar"]