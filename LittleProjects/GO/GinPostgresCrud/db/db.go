package db

import (
	"database/sql"
	_ "github.com/lib/pq"
)

func Connect(driver string, url string) *sql.DB {
	connection, err := sql.Open(driver, url)
	if err != nil {
		panic(err)
	}

	connection.SetMaxIdleConns(4)
	connection.SetMaxOpenConns(8)

	return connection
}
