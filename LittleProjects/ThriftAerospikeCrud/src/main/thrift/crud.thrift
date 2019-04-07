namespace java crud

typedef i32 int
typedef i64 long

exception CrudError {
  1: string msg
}

struct Entity {
 1: required long id,
 2: required string value,
 3: required long timestamp
}

service CrudService {
  // there is not option type, so, we will use list with at most 1 element
  list<Entity> findOne(1:long id)
  list<Entity> findAll()
  Entity create(1: required Entity entity)
  void update(1: required Entity entity) throws (1: CrudError e)
  bool remove(1: required long id)
  void removeAll() throws (1: CrudError e)
}