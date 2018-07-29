import { TodoModel } from '../entity/model'

function TodoRepository() {}

TodoRepository.prototype.save = function(type, body) {
    let todo = new TodoModel({
        type: type,
        body: body
    });

    return todo.save();
};

TodoRepository.prototype.deleteById = function(id) {
    return TodoModel.findByIdAndRemove(id).exec(); // exec returns fully-fledged promise: http://mongoosejs.com/docs/promises.html#queries-are-not-promises
};

TodoRepository.prototype.delete = function(todo) {
    return todo.remove();
};

TodoRepository.prototype.update = function(id, newType, newBody) {
    return TodoModel.findByIdAndUpdate(id, {"$set": {type: newType, body: newBody}}, {new: true}).exec();
};

TodoRepository.prototype.getById = function(id) {
    return TodoModel.findById(id).exec();
};

TodoRepository.prototype.getByType = function(type) {
    return TodoModel.find({type: type}).exec();
};

export default TodoRepository;