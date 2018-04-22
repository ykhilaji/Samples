const Helper = require('./helper');

class Book extends Helper {
    constructor(id, title, pages, authors) {
        super();

        this.id = id;
        this.title = title;
        this.pages = pages;
        this.authors = authors;
    }

    static apply(result) {
        let row = result[0];
        return new Book(row['id'], row['title'], row['pages'], row['authors']);
    }

    static fromJson(json) {
        return new Book(json['id'], json['title'], json['pages'], json['authors'])
    }

    get getId() {
        return this.id;
    }

    get getTitle() {
        return this.title;
    }

    get getPages() {
        return this.pages;
    }

    get getAuthors() {
        return this.authors;
    }

    set setId(id) {
        this.id = id;
    }

    static fields() {
        return ['id', 'title', 'pages', 'authors'];
    }
}

module.exports = Book;