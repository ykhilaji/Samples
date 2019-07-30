package crud

import (
	"../model"
	"database/sql"
)

func SelectBook(db *sql.DB, id uint64) (*model.Book, error) {
	res := db.QueryRow("select title from go.book where id = $1", id)
	var book = new(model.Book)

	err := res.Scan(&book.Title)
	if err != nil {
		return nil, err
	}

	book.Id = id

	return book, nil
}

func InsertBook(db *sql.DB, book *model.Book) error {
	tx, err := db.Begin()

	if err != nil {
		return err
	}

	_, err = db.Exec("insert into go.book(title) values ($1)", book.Title)

	if err != nil {
		return err
	}

	defer tx.Rollback()
	err = tx.Commit()

	if err != nil {
		return err
	}

	return err
}

func DeleteBook(db *sql.DB, id uint64) error {
	tx, err := db.Begin()

	if err != nil {
		return err
	}

	_, err = db.Exec("delete from go.book where id = $1", id)

	if err != nil {
		return err
	}

	defer tx.Rollback()
	err = tx.Commit()

	if err != nil {
		return err
	}

	return err
}

func UpdateBook(db *sql.DB, book *model.Book) error {
	tx, err := db.Begin()

	if err != nil {
		return err
	}

	_, err = db.Exec("update go.book set title = $1 where id = $2", book.Title, book.Id)

	if err != nil {
		return err
	}

	defer tx.Rollback()
	err = tx.Commit()

	if err != nil {
		return err
	}

	return err
}

func SelectAuthor(db *sql.DB, id uint64) (*model.Author, error) {
	res := db.QueryRow("select first_name, last_name from go.author where id = $1", id)

	var author = new(model.Author)

	err := res.Scan(&author.FistName, &author.LastName)
	if err != nil {
		return nil, err
	}

	author.Id = id
	return author, nil
}

func InsertAuthor(db *sql.DB, author *model.Author) error {
	tx, err := db.Begin()

	if err != nil {
		return err
	}

	_, err = db.Exec("insert into go.author(first_name, last_name) values ($1, $2)", author.FistName, author.LastName)

	if err != nil {
		return err
	}

	defer tx.Rollback()
	err = tx.Commit()

	if err != nil {
		return err
	}

	return err
}

func DeleteAuthor(db *sql.DB, id uint64) error {
	tx, err := db.Begin()

	if err != nil {
		return err
	}

	_, err = db.Exec("delete from go.author where id = $1", id)

	if err != nil {
		return err
	}

	defer tx.Rollback()
	err = tx.Commit()

	if err != nil {
		return err
	}

	return err
}

func UpdateAuthor(db *sql.DB, author *model.Author) error {
	tx, err := db.Begin()

	if err != nil {
		return err
	}

	_, err = db.Exec("update go.author set firs_tname = $1, last_name = $2 where id = $3", author.FistName, author.LastName, author.Id)

	if err != nil {
		return err
	}

	defer tx.Rollback()
	err = tx.Commit()

	if err != nil {
		return err
	}

	return err
}

func SelectLibrary(db *sql.DB, id uint64) (*model.Library, error) {
	res := db.QueryRow("select city from go.library where id = $1", id)

	var library = new(model.Library)

	err := res.Scan(&library.City)
	if err != nil {
		return nil, err
	}

	library.Id = id

	return library, nil
}

func InsertLibrary(db *sql.DB, library *model.Library) error {
	tx, err := db.Begin()

	if err != nil {
		return err
	}

	_, err = db.Exec("insert into go.library(title) values ($1)", library.City)

	if err != nil {
		return err
	}

	defer tx.Rollback()
	err = tx.Commit()

	if err != nil {
		return err
	}

	return err
}

func DeleteLibrary(db *sql.DB, id uint64) error {
	tx, err := db.Begin()

	if err != nil {
		return err
	}

	_, err = db.Exec("delete from go.library where id = $1", id)

	if err != nil {
		return err
	}

	defer tx.Rollback()
	err = tx.Commit()

	if err != nil {
		return err
	}

	return err
}

func UpdateLibrary(db *sql.DB, library *model.Library) error {
	tx, err := db.Begin()

	if err != nil {
		return err
	}

	_, err = db.Exec("update go.library set city = $1 where id = $2", library.City, library.Id)

	if err != nil {
		return err
	}

	defer tx.Rollback()
	err = tx.Commit()

	if err != nil {
		return err
	}

	return err
}
