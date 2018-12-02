from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from datetime import datetime, timedelta

default_args = {
    'owner': 'test',
    'depends_on_past': False,
    'start_date': datetime(2018, 1, 1),
    'email': ['test@test.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG('basic_sample', default_args=default_args, schedule_interval=timedelta(days=1))

t1 = BashOperator(
    task_id='echo_template_one',
    bash_command="""echo '{{params.a}}'""",
    params={'a': 'first'},
    dag=dag)

t2 = BashOperator(
    task_id='echo_template_two',
    bash_command="""echo '{{params.b}}'""",
    params={'b': 'second'},
    retries=3,
    dag=dag)

t3 = BashOperator(
    task_id='echo_template_three',
    bash_command="""echo '{{params.c}}'""",
    params={'c': 'third'},
    dag=dag)

t2.set_upstream(t1)
t3.set_upstream(t2)
