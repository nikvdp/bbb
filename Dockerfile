# an example of how you might dockerize a CLI built with bbb. 
# Building this Dockerfile will create a container image that runs 
# the bbb cli in this folder.

# You can also use this image to build a Linux binary of your CLI from an Intel
# mac, to do this, run the following commands;

# docker build . -t bbb
# docker run --entrypoint /bin/bash bbb -c 'cat /example-cli' > example-cli
# chmod +x example-cli

FROM ubuntu:focal as build

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y build-essential wget clojure git curl 

# install clojure with all tools
RUN curl -O https://download.clojure.org/install/linux-install-1.10.3.1075.sh && \
    chmod +x linux-install-1.10.3.1075.sh && \
    ./linux-install-1.10.3.1075.sh

# fixes 'ld: cannot find -lz' error
RUN apt-get install -y zlib1g-dev

# install babashka
RUN curl -s https://raw.githubusercontent.com/babashka/babashka/master/install | bash

WORKDIR /build

COPY . /build

ENV BBB_AUTOGRAAL_NOINTERACTIVE=true

RUN bb native-image

FROM ubuntu:focal

COPY --from=build /build/bb /example-cli

ENTRYPOINT /example-cli 