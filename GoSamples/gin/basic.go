package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"net/http"
	"time"
)

type Message struct {
	Id   uint64 `json:"id" binding:"required"`
	Body string `json:"body" binding:"required"`
}

func main() {
	rest := gin.Default()

	rest.GET("/", func(ctx *gin.Context) {
		fmt.Println("GET - return 200")
		ctx.Status(http.StatusOK)
	})

	rest.GET("/user/:username", func(ctx *gin.Context) {
		name := ctx.Param("username")
		fmt.Println("GET /username param - ", name)
		ctx.String(http.StatusOK, "Username: %s", name)
	})

	// /query?username=???
	rest.GET("/query", func(ctx *gin.Context) {
		name := ctx.Query("username")
		fmt.Println("GET query param - ", name)
		ctx.String(http.StatusOK, "Username: %s", name)
	})

	rest.POST("/echo", func(ctx *gin.Context) {
		var msg Message
		if err := ctx.ShouldBindJSON(&msg); err != nil {
			fmt.Println("Bad request")
			ctx.Status(http.StatusBadRequest)
			return
		}

		fmt.Println("Message: ", msg.Body)

		ctx.JSON(http.StatusOK, gin.H{"id": msg.Id, "body": "echo: " + msg.Body})
	})

	server := &http.Server{
		Addr:           ":8080",
		Handler:        rest,
		ReadTimeout:    10 * time.Second,
		WriteTimeout:   10 * time.Second,
		MaxHeaderBytes: 1 << 20,
	}

	server.ListenAndServe()
}
