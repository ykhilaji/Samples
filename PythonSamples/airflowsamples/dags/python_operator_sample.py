from airflow import DAG
from airflow.operators.python_operator import PythonOperator
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

dag = DAG("python_operator_sample", default_args=default_args, schedule_interval=timedelta(days=1))


def a_func(*args, **kwargs):
    print(args, kwargs)


def b_func(*args, **kwargs):
    print(args, kwargs)


def c_func(*args, **kwargs):
    print(args, kwargs)


a = PythonOperator(
    task_id="a",
    python_callable=a_func,
    op_args=[1],
    op_kwargs={'a': 'first'},
    dag=dag
)

b = PythonOperator(
    task_id="b",
    python_callable=b_func,
    op_args=[2],
    op_kwargs={'b': 'second'},
    dag=dag
)

c = PythonOperator(
    task_id="c",
    python_callable=c_func,
    op_args=[3],
    op_kwargs={'c': 'third'},
    dag=dag
)

a.set_upstream(b)
b.set_upstream(c)
