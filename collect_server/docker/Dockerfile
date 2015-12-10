FROM ubuntu:14.04
MAINTAINER Spring Choi (springchoi@airplug.com)

ENV DEBIAN_FRONTEND noninteractive

# Install support package
RUN apt-get update
RUN apt-get install -y  git \
                        python-dev \
                        python-pip

#RUN echo 4096 > /proc/sys/net/core/somaxconn

# Download & Install
RUN git clone http://deploy:deploy0621@src.airplug.co.kr/opensource/dd-collector.git /opt/dd-collector \
    && mkdir -p /opt/dd-collector/collect_server/log \
    && pip install -r /opt/dd-collector/collect_server/requirements.txt

EXPOSE 80

COPY config.local /opt/dd-collector/collect_server

ENTRYPOINT ["/usr/local/bin/uwsgi", "--ini", "/opt/dd-collector/collect_server/scripts/apat.wsgi"]
