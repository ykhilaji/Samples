package db

import (
	"database/sql"
	_ "github.com/lib/pq"
	"log"
)

func Connect(driverName string, connectionString string) (*sql.DB, error) {
	log.Println("Open connection: ", connectionString)
	db, err := sql.Open("postgres", connectionString)

	return db, err
}
