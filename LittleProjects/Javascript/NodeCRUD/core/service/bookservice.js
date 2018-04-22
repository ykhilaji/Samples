const Service = require('./service');
const BookRepository = require('../repository/bookrepository');
const Book = require('../model/book');

class BookService extends Service {
    constructor() {
        super(new BookRepository());
    }

    save(object) {
        return super.save(Book.fromJson(object));
    }

    update(object) {
        return super.update(Book.fromJson(object));
    }
}

module.exports = BookService;