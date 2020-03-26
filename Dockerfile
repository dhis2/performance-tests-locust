FROM python:3.6-alpine

RUN apk --no-cache add g++ \
        libffi-dev \
        zeromq \
      && pip install locustio==0.13.0 pyzmq Jinja2 \
      && mkdir /locust \
      && mkdir /templates

COPY docker-entrypoint.sh /
COPY report-template.html /templates
RUN chmod +x /docker-entrypoint.sh \
    && chmod +r /templates/report-template.html

WORKDIR /locust

EXPOSE 8089
EXPOSE 5557
EXPOSE 5558

ENTRYPOINT ["/docker-entrypoint.sh"]