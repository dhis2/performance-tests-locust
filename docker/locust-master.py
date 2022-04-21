import argparse
import csv
import datetime

from jinja2 import Environment, FileSystemLoader
import locust
from locust import events, runners

WORK_DIR = '/locust/'
TEMPLATE_NAME = 'report-template.html'

parser = argparse.ArgumentParser()
parser.add_argument('--csv')
args, unknown = parser.parse_known_args();

CSV_PREFIX = args.csv
HTML_REPORT_NAME = "html_report.html"

class MyTaskSet(locust.TaskSet):
    @locust.task
    def my_task(self):
        print('Locust instance ({}) executing "my_task"'.format(self.user))

class MyUser(locust.User):
    tasks = [MyTaskSet]


def generate_report():
    print('Generating html report')
    loader = FileSystemLoader('/templates')
    env = Environment(loader=loader)

    csv.register_dialect('dialect',
                         delimiter=',',
                         quotechar='"',
                         quoting=csv.QUOTE_ALL,
                         skipinitialspace=True)

    # write failures to file
    with open(WORK_DIR + CSV_PREFIX + '_errors.csv', "w") as f:
        f.write(failures_csv())

    r_data, e_data, dist_data = {}, {}, {}
    r_headers, e_headers, dist_headers = [], [], []

    def store_data(file, headers, data):
        with open(WORK_DIR + CSV_PREFIX + file) as f:
            content = csv.DictReader(f, dialect='dialect')
            headers.extend(content.fieldnames)
            for row in content:
                for field_name in headers:
                    if field_name not in data:
                        data[field_name] = []
                    data[field_name].append(row[field_name])

    store_data('_requests.csv', r_headers, r_data)
    store_data('_errors.csv', e_headers, e_data)
    store_data('_distribution.csv', dist_headers, dist_data)

    # compute failure rate based on total stats
    if 'Total' in r_data['Name']:
        index = r_data['Name'].index('Total')
        total_rps = r_data['Requests/s'][index]
        total_failures = r_data['# failures'][index]
        total_requests = r_data['# requests'][index]
        fail_percent = (float(total_failures) / float(total_requests + total_failures)) * 100

    # write report
    with open(WORK_DIR + HTML_REPORT_NAME, "w") as report:
        report.write(env.get_template(TEMPLATE_NAME).render(
            datetime=datetime.datetime.now().strftime("%I:%M%p on %B %d, %Y"),
            rps=total_rps,
            fails=str(fail_percent) + "%",
            stat_headers=r_headers,
            stat_data=r_data.values(),
            e_headers=e_headers,
            e_data=e_data.values(),
            dist_headers=dist_headers,
            dist_data=dist_data.values()
        ))


def failures_csv():
    rows = [
        ",".join((
            '"Method"',
            '"Name"',
            '"Error"',
            '"Occurrences"',
        ))
    ]

    errors = runners.locust_runner.stats.serialize_errors()

    for s in errors:
        er = errors[s];
        print(er)
        rows.append('"%s","%s","%s",%i' % (
            er['method'],
            er['name'],
            er['error'].replace("\"", "'"),
            er['occurences'],
        ))
    return "\n".join(rows)

