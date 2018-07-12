from django.urls import path

from . import views

urlpatterns = [
    path('', views.IndexView.as_view(), name='index'),

    path('authors/', views.AuthorView.as_view(), name='authors'),
    path('authors/<str:name>/', views.AuthorView.as_view(), name='authors'),

    path('books/', views.BookView.as_view(), name='books'),
    path('books/<str:name>/', views.BookView.as_view(), name='books'),

    path('publishers/', views.PublisherView.as_view(), name='publishers'),
    path('publishers/<str:name>/', views.PublisherView.as_view(), name='publishers'),
]
