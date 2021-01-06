# Prepares mqlambdatm demo.
# Based on mq-docker image http://github.com/ibm-messaging/mq-docker.git
# That image must be build first and tagged mq-docker.
# By running this image you accept IBM MQ license. If you haven't changed MQ_URL 
# argument of mq-docker build, then it is IBM MQ developer license, otherwise
# the license for the MQ installation media you have chosen.

# FROM mq-docker
FROM ibmcom/mq:latest

MAINTAINER Yuri Steinschreiber <yuri@steinchreiber.com> 

ARG MQLAMBDATM_RELEASE="https://github.com/yurist/mqlambdatm/releases/download/v0.0.1-alpha.2/mqlambdatm.tar.gz"

# COPY *.sh /usr/local/bin/
COPY *.mqsc /etc/mqm/

# RUN chmod +x /usr/local/bin/*.sh

RUN apt-get update && \
    apt-get install -y ca-certificates 

RUN mkdir /tmp/mqlambda && \
    cd /tmp/mqlambda && \
    curl -LO $MQLAMBDATM_RELEASE && \
    tar -xzvf ./*.tar.gz && \
    mv mqlambdatm /usr/local/bin && \
    rm -rf /tmp/mqlambda && \
    useradd demo && \
    echo demo:demo | chpasswd && \
    usermod -a -G mqm demo


    
