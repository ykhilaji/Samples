package main

import (
	"./crud"
	"./db"
	//"./model"
	"log"
)

func checkErr(err error) {
	if err != nil {
		log.Fatal(err)
	}
}

func main() {
	connect, err := db.Connect("postgres", "postgres://postgres:1111@192.168.99.100:5432/postgres?sslmode=disable")
	checkErr(err)
	defer connect.Close()

	book, err := crud.SelectBook(connect, 1)
	checkErr(err)
	author, err := crud.SelectAuthor(connect, 1)
	checkErr(err)
	library, err := crud.SelectLibrary(connect, 1)
	checkErr(err)
	log.Println(book)
	log.Println(author)
	log.Println(library)


}
