package metrics

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"net/http"
)

var (
	ErrorCount = promauto.NewCounter(prometheus.CounterOpts{
		Name: "crud_error_count",
		Help: "Total errors count",
	})

	SelectCount = promauto.NewCounter(prometheus.CounterOpts{
		Name: "crud_select_count",
		Help: "Total selects count",
	})

	SelectTimer = promauto.NewHistogram(prometheus.HistogramOpts{
		Name: "crud_select_time",
		Help: "Select time",
	})

	InsertCount = promauto.NewCounter(prometheus.CounterOpts{
		Name: "crud_insert_count",
		Help: "Total inserts count",
	})

	InsertTimer = promauto.NewHistogram(prometheus.HistogramOpts{
		Name: "crud_insert_time",
		Help: "Insert time",
	})

	DeleteCount = promauto.NewCounter(prometheus.CounterOpts{
		Name: "crud_delete_count",
		Help: "Total deletes count",
	})

	DeleteTimer = promauto.NewHistogram(prometheus.HistogramOpts{
		Name: "crud_delete_time",
		Help: "Delete time",
	})

	UpdateCount = promauto.NewCounter(prometheus.CounterOpts{
		Name: "crud_update_count",
		Help: "Total updates count",
	})

	UpdateTimer = promauto.NewHistogram(prometheus.HistogramOpts{
		Name: "crud_update_time",
		Help: "Update time",
	})
)

func Timer(observer prometheus.Observer) *prometheus.Timer {
	return prometheus.NewTimer(observer)
}

func ExposeMetrics() {
	http.Handle("/metrics", promhttp.Handler())
	http.ListenAndServe(":2112", nil)
}
