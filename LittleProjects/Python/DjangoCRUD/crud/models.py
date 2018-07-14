from django.db import models


# because django does not support updating rows with auto incremented PK =\
# instead of updating django creates new row with updated values
class Author(models.Model):
    indexes = [
        models.Index(fields=['first_name', 'last_name'])
    ]

    first_name = models.CharField(max_length=255)
    last_name = models.CharField(max_length=255)

    books = models.ManyToManyField('Book')

    def __str__(self):
        return "Author: {0} {1}".format(self.first_name, self.last_name)

    class Meta:
        ordering = ['first_name', 'last_name']


class Book(models.Model):
    indexes = [
        models.Index(fields=['title'])
    ]

    title = models.CharField(max_length=255)
    description = models.TextField()
    pages = models.IntegerField()
    pub_date = models.DateField()

    publisher = models.ForeignKey('Publisher', on_delete=models.SET_NULL, blank=True, null=True)
    authors = models.ManyToManyField('Author', through='AuthorBooks')
    genre = models.ForeignKey('Genre', on_delete=models.SET_NULL, blank=True, null=True)

    def __str__(self):
        return "Book: {0}".format(self.title)

    class Meta:
        ordering = ['title']


class Genre(models.Model):
    name = models.CharField(max_length=255)

    def __str__(self):
        return "Genre: {0}".format(self.name)


class Publisher(models.Model):
    indexes = [
        models.Index(fields=['name'])
    ]

    name = models.CharField(max_length=255, unique=True)

    def __str__(self):
        return "Publisher: {0}".format(self.name)

    class Meta:
        ordering = ['name']


class AuthorBooks(models.Model):
    author_id = models.ForeignKey('Author', on_delete=models.CASCADE)
    book_id = models.ForeignKey('Book', on_delete=models.CASCADE)
