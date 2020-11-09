FROM python:3.8-alpine

RUN apk --no-cache add g++ zeromq-dev libffi-dev file make musl-dev
RUN pip install locust==1.0.3 pyzmq Jinja2 \
      && mkdir /locust \
      && mkdir /templates

COPY ./docker-entrypoint.sh /
COPY ./report-template.html /templates
RUN chmod +x /docker-entrypoint.sh \
    && chmod +r /templates/report-template.html

WORKDIR /locust

EXPOSE 8089
EXPOSE 5557
EXPOSE 5558

ENTRYPOINT ["/docker-entrypoint.sh"]

ENV PYTHONUNBUFFERED=1