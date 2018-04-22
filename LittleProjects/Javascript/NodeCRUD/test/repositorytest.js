const BookRepository = require('../core/repository/bookrepository');
const Book = require('../core/model/book');

let book = new Book(5, "updatedTitle", 321, ["some author", "another author"]);
let repo = new BookRepository();


repo.get(1)
    .then(book => console.log(book))
    .catch(err => console.log(err));

repo.save(book)
    .then(result => {
        console.log(result);
        return result;
    })
    .then(result => repo.remove(result.getId()))
    .then(result => console.log(result))
    .catch(err => console.log(err));

repo.save(book)
    .then(result => {
        console.log(result);
        return JSON.parse(result)['id'];
    })
    .catch(err => console.log(err));

repo.update(book)
    .then(result => console.log(result))
    .catch(err => console.log(err));

