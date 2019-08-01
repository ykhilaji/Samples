package web

import (
	"../model"
	"../service"
	"github.com/gin-gonic/gin"
	"net/http"
	"strconv"
	"time"
)

func Start(service service.Service) {
	rest := gin.Default()

	api := rest.Group("/api")
	{
		api.GET("/:id", func(context *gin.Context) {
			idParam := context.Param("id")
			id, err := strconv.ParseUint(idParam, 10, 64)

			if err != nil {
				context.String(http.StatusBadRequest, "Incorrect id")
			}

			model, err := service.Select(id)

			if err != nil {
				context.String(http.StatusBadRequest, "Error ", err)
			}

			context.JSON(http.StatusOK, model)
		})

		api.POST("/", func(context *gin.Context) {
			var entity model.Entity

			if err := context.ShouldBindJSON(&entity); err != nil {
				context.String(http.StatusBadRequest, "Incorrect json body. Correct format: {'value': '<value'>}")
			}

			updated, err := service.Update(&entity)
			if err != nil {
				context.String(http.StatusBadRequest, "Error ", err)
			}

			context.JSON(http.StatusOK, gin.H{
				"updated": updated,
			})
		})

		api.PUT("/", func(context *gin.Context) {
			var entity model.Entity

			if err := context.ShouldBindJSON(&entity); err != nil {
				context.String(http.StatusBadRequest, "Incorrect json body. Correct format: {'value': '<value'>}")
			}

			model, err := service.Insert(&entity)
			if err != nil {
				context.String(http.StatusBadRequest, "Error ", err)
			}

			context.JSON(http.StatusOK, model)
		})

		api.DELETE("/:id", func(context *gin.Context) {
			idParam := context.Param("id")
			id, err := strconv.ParseUint(idParam, 10, 64)

			if err != nil {
				context.String(http.StatusBadRequest, "Incorrect id")
			}

			rows, err := service.Delete(id)

			if err != nil {
				context.String(http.StatusBadRequest, "Error ", err)
			}

			context.JSON(http.StatusOK, gin.H{
				"deleted": rows,
			})
		})
	}

	server := &http.Server{
		Addr:           ":8080",
		Handler:        rest,
		ReadTimeout:    10 * time.Second,
		WriteTimeout:   10 * time.Second,
		MaxHeaderBytes: 1 << 20,
	}

	_ = server.ListenAndServe()
	defer server.Close()
}