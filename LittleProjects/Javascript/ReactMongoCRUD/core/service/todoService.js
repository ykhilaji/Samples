import TodoRepository from './../repository/todoRepository'

function TodoService() {
    this.repository = new TodoRepository();
}

TodoService.prototype.save = function (type, body) {
    console.log(`Saving new todo with type: ${type} and body: ${body}`);
    return this.repository.save(type, body);
};

TodoService.prototype.deleteById = function (id) {
    console.log(`Deleting todo by id: ${id}`);
    return this.repository.deleteById(id);
};

TodoService.prototype.delete = function (todo) {
    console.log(`Deleting todo with id: ${todo._id}`);
    return this.repository.delete(todo);
};

TodoService.prototype.update = function (id, newType, newBody) {
    console.log(`Updating todo with id: ${id}. New type: ${newType} new body: ${newBody}`);
    return this.repository.update(id, newType, newBody);
};

TodoService.prototype.getById = function (id) {
    console.log(`Getting todo by id: ${id}`);
    return this.repository.getById(id);
};

TodoService.prototype.getByType = function (type) {
    console.log(`Getting todos by type: ${type}`);
    return this.repository.getByType(type);
};

export default TodoService;