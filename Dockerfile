FROM python:3.6.6-alpine3.8

COPY docker-entrypoint.sh /

RUN apk --no-cache add g++ \
      && apk --no-cache add zeromq-dev \
      && pip install locustio pyzmq \
      && chmod +x /docker-entrypoint.sh


RUN  mkdir /locust
WORKDIR /locust

EXPOSE 8089
EXPOSE 5557
EXPOSE 5558

ENTRYPOINT ["/docker-entrypoint.sh"]