package service

import (
	"../metrics"
	"../model"
	"../repository"
	"database/sql"
)

type Service interface {
	Select(id uint64) (*model.Entity, error)
	Insert(entity *model.Entity) (*model.Entity, error)
	Delete(id uint64) (int64, error)
	Update(entity *model.Entity) (bool, error)
}

type ServiceImpl struct {
	db sql.DB
	repository repository.Repository
}

func NewService(db *sql.DB, repository repository.Repository) *ServiceImpl {
	return &ServiceImpl{
		db: *db,
		repository: repository,
	}
}

func (s *ServiceImpl) Select(id uint64) (*model.Entity, error) {
	metrics.SelectCount.Inc()
	timer := metrics.Timer(metrics.SelectTimer)
	defer timer.ObserveDuration()

	tx, err := s.db.Begin()
	defer tx.Rollback()
	if err != nil {
		metrics.ErrorCount.Inc()
		return nil, err
	}

	e, err := s.repository.Select(id, &s.db)
	if err != nil {
		metrics.ErrorCount.Inc()
		return nil, err
	}

	err = tx.Commit()
	if err != nil {
		metrics.ErrorCount.Inc()
		return nil, err
	}

	return e, nil
}

func (s *ServiceImpl) Insert(entity *model.Entity) (*model.Entity, error) {
	metrics.InsertCount.Inc()
	timer := metrics.Timer(metrics.InsertTimer)
	defer timer.ObserveDuration()

	tx, err := s.db.Begin()
	defer tx.Rollback()
	if err != nil {
		return nil, err
	}

	e, err := s.repository.Insert(entity, &s.db)
	if err != nil {
		metrics.ErrorCount.Inc()
		return nil, err
	}

	err = tx.Commit()
	if err != nil {
		metrics.ErrorCount.Inc()
		return nil, err
	}

	return e, nil
}

func (s *ServiceImpl) Delete(id uint64) (int64, error) {
	metrics.DeleteCount.Inc()
	timer := metrics.Timer(metrics.DeleteTimer)
	defer timer.ObserveDuration()

	tx, err := s.db.Begin()
	defer tx.Rollback()
	if err != nil {
		metrics.ErrorCount.Inc()
		return 0, err
	}

	e, err := s.repository.Delete(id, &s.db)
	if err != nil {
		metrics.ErrorCount.Inc()
		return 0, err
	}

	err = tx.Commit()
	if err != nil {
		metrics.ErrorCount.Inc()
		return 0, err
	}

	return e, nil
}

func (s *ServiceImpl) Update(entity *model.Entity) (bool, error) {
	metrics.UpdateCount.Inc()
	timer := metrics.Timer(metrics.UpdateTimer)
	defer timer.ObserveDuration()

	tx, err := s.db.Begin()
	defer tx.Rollback()
	if err != nil {
		return false, err
	}

	e, err := s.repository.Update(entity, &s.db)
	if err != nil {
		metrics.ErrorCount.Inc()
		return false, err
	}

	err = tx.Commit()
	if err != nil {
		metrics.ErrorCount.Inc()
		return false, err
	}

	return e, nil
}