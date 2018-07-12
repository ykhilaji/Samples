from django.views import generic
from django import forms
from django.http import HttpResponseRedirect

from . import models


class IndexView(generic.TemplateView):
    template_name = 'crud/index.html'


class AuthorForm(forms.ModelForm):
    first_name = forms.CharField(label='First name', max_length=255,
                                 widget=forms.TextInput(attrs={'class': 'form-control'}))
    last_name = forms.CharField(label='Last name', max_length=255,
                                widget=forms.TextInput(attrs={'class': 'form-control'}))

    class Meta:
        model = models.Author
        fields = ('first_name', 'last_name',)


class AuthorView(generic.ListView, generic.FormView):
    template_name = 'crud/author.html'
    context_object_name = 'authors'
    queryset = models.Author.objects.all()
    paginate_by = 10
    form_class = AuthorForm

    def post(self, request, *args, **kwargs):
        form = AuthorForm(request.POST)

        if form.is_valid():
            author = form.save(commit=False)
            print(author)

        return HttpResponseRedirect('/crud/authors/')


class BookForm(forms.ModelForm):
    title = forms.CharField(label='Title', max_length=255,
                            widget=forms.TextInput(attrs={'class': 'form-control'}))
    description = forms.CharField(label='Description', max_length=255,
                                  widget=forms.TextInput(attrs={'class': 'form-control'}))
    pages = forms.IntegerField(label='Pages',
                               widget=forms.NumberInput(attrs={'class': 'form-control'}))

    class Meta:
        model = models.Book
        fields = ('title', 'description', 'pages',)


class BookView(generic.ListView, generic.FormView):
    template_name = 'crud/book.html'
    context_object_name = 'books'
    queryset = models.Book.objects.all()
    paginate_by = 15
    form_class = BookForm

    def post(self, request, *args, **kwargs):
        form = BookForm(request.POST)

        if form.is_valid():
            book = form.save(commit=False)
            print(book)

        return HttpResponseRedirect('/crud/books/')


class PublisherForm(forms.ModelForm):
    name = forms.CharField(label='Name', max_length=255,
                           widget=forms.TextInput(attrs={'class': 'form-control'}))

    class Meta:
        model = models.Publisher
        fields = ('name',)


class PublisherView(generic.ListView, generic.FormView):
    template_name = 'crud/publisher.html'
    context_object_name = 'publishers'
    queryset = models.Publisher.objects.all()
    paginate_by = 10
    form_class = PublisherForm

    def post(self, request, *args, **kwargs):
        form = PublisherForm(request.POST)

        if form.is_valid():
            publisher = form.save(commit=False)
            publisher.save()

        return HttpResponseRedirect('/crud/publishers/')

