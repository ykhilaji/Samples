import mongoose from 'mongoose'

const Schema = mongoose.Schema;

const TodoSchema = new Schema({
    type: { type : String , required : true},
    body: { type : String , required : true},
});

TodoSchema.index({type: 1, body: 1}, {unique: true});

export const TodoModel = mongoose.model('TodoModel', TodoSchema);

