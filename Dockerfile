FROM python:3.5-alpine

COPY docker-entrypoint.sh /

RUN  apk --no-cache add --virtual=.build-dep build-base \
    && apk --no-cache add libzmq \
    && pip install --no-cache-dir locustio \
    && apk del .build-dep \
    && chmod +x /docker-entrypoint.sh


RUN  mkdir /locust
WORKDIR /locust

EXPOSE 8089
EXPOSE 5557
EXPOSE 5558

ENTRYPOINT ["/docker-entrypoint.sh"]