package model

type Book struct {
	Id    uint64
	Title string
}

type Author struct {
	Id       uint64
	FistName string
	LastName string
}

type Library struct {
	Id   uint64
	City string
}
