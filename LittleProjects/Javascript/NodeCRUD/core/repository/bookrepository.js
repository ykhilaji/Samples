const Repository = require('./repository');
const Book = require('../model/book');

class BookRepository extends Repository {
    constructor() {
        super("node", "book", Book);
    }
}

module.exports = BookRepository;