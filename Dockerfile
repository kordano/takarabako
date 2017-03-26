FROM ubuntu:latest
MAINTAINER Konrad Kühne konrad.kuehne@rocketmail.com

# Update the APT cache
RUN apt-get update

RUN apt-get upgrade -y

# Install and setup project dependencies
RUN apt-get install -y curl git wget unzip zip

# prepare for Java download
RUN apt-get install -y software-properties-common
RUN apt-get -y install openjdk-8-jre-headless
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

# fix wget
RUN export HTTP_CLIENT="wget --no-check-certificate -O"

# grab leiningen
RUN wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -O /usr/local/bin/lein
RUN chmod +x /usr/local/bin/lein
ENV LEIN_ROOT yes
RUN lein

# create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# clone repo
COPY project.clj /usr/src/app
COPY src /usr/src/app/src
COPY test /usr/src/app/test
COPY resources /usr/src/app/resources

RUN lein cljsbuild once min
RUN lein sass once
RUN lein deps

CMD ["lein", "run"]
