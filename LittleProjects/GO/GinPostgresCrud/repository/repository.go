package repository

import (
	"../model"
	"database/sql"
	"fmt"
)

type Repository interface {
	Select(id uint64, db *sql.DB) (*model.Entity, error)
	Insert(entity *model.Entity, db *sql.DB) (*model.Entity, error)
	Delete(id uint64, db *sql.DB) (int64, error)
	Update(entity *model.Entity, db *sql.DB) (bool, error)
}

type RepositoryImpl struct{}

func (r *RepositoryImpl) Select(id uint64, db *sql.DB) (*model.Entity, error) {
	row := db.QueryRow("select value from entity where id = $1", id)
	var value string
	switch err := row.Scan(&value); err {
	case sql.ErrNoRows:
		fmt.Println("No rows were returned")
		return nil, sql.ErrNoRows
	case nil:
		entity := new(model.Entity)
		entity.Id = id
		entity.Value = value
		return entity, nil
	default:
		panic(err)
	}
}

func (r *RepositoryImpl) Insert(entity *model.Entity, db *sql.DB) (*model.Entity, error) {
	row, err := db.Exec("insert into entity(value) values($1)", entity.Value)
	if err != nil {
		fmt.Println("Error while inserting new entity")
		return nil, err
	}

	id, err := row.LastInsertId()
	if err != nil {
		fmt.Println("Error while getting id")
		return nil, err
	}

	entity.Id = uint64(id)
	return entity, nil
}

func (r *RepositoryImpl) Delete(id uint64, db *sql.DB) (int64, error) {
	result, err := db.Exec("delete from entity where id = $1", id)
	if err != nil {
		fmt.Println("Error while deleting entity")
		return 0, err
	}

	rows, err := result.RowsAffected()
	if err != nil {
		fmt.Println("Error while deleting entity")
		return 0, err
	}

	return rows, nil
}

func (r *RepositoryImpl) Update(entity *model.Entity, db *sql.DB) (bool, error) {
	result, err := db.Exec("update entity set value = $1", entity.Value)
	if err != nil {
		fmt.Println("Error while updating entity")
		return false, err
	}

	rows, err := result.RowsAffected()
	if err != nil {
		fmt.Println("Error while updating entity")
		return false, err
	}

	return rows > 0, nil
}
