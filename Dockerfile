FROM ubuntu:focal as build

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y build-essential wget clojure

RUN apt-get install -y curl 
# install clojure with all tools
RUN curl -O https://download.clojure.org/install/linux-install-1.10.3.1075.sh && \
    chmod +x linux-install-1.10.3.1075.sh && \
    ./linux-install-1.10.3.1075.sh

# fixes 'ld: cannot find -lz' error
RUN apt-get install -y zlib1g-dev

RUN apt-get install -y git 

# install babashka
RUN curl -s https://raw.githubusercontent.com/babashka/babashka/master/install | bash

WORKDIR /build

COPY . /build



ENV BBB_AUTOGRAAL_NOINTERACTIVE=true

RUN bb native-image

FROM ubuntu:focal

COPY build:/build/bb /example-cli

ENTRYPOINT /example-cli 
