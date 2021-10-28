FROM locustio/locust

RUN mkdir ./templates

COPY ./docker-entrypoint.sh /home/locust
COPY ./report-template.html /home/locust/templates
COPY ./locust-master.py /home/locust

USER root

RUN chmod +x ./docker-entrypoint.sh \
    && chmod +r ./templates/report-template.html \
    && chmod +r ./locust-master.py

ENV LOCUST_FILE /home/locust/locust-master.py

EXPOSE 8089
EXPOSE 5557

ENTRYPOINT /home/locust/docker-entrypoint.sh
