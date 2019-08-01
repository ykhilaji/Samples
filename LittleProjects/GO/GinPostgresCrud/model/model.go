package model

type Entity struct {
	Id uint64 `json:"id" binding:"optional"`
	Value string `json:"value" binding:"required"`
}
