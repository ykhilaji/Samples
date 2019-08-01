package main

import (
	"./db"
	"./metrics"
	"./repository"
	"./service"
	"./web"
)

func main() {
	connection := db.Connect("postgres", "postgres://postgres:postgres@192.168.99.100:5432/postgres?sslmode=disable")
	repositoryImpl := new(repository.RepositoryImpl)
	serviceImpl := service.NewService(connection, repositoryImpl)
	go metrics.ExposeMetrics()
	web.Start(serviceImpl)
}