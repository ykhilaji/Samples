import {Value} from "./types";
import {ADD, REMOVE, SELECT, UPDATE} from "./actionType";

export const add = (value: Value) => {
    return {
        type: ADD,
        value: value
    }
};

export const remove = (id: string) => {
    return {
        type: REMOVE,
        id: id
    }
};

export const update = (value: Value) => {
    return {
        type: UPDATE,
        value: value
    }
};

export const select = (id: string) => {
    return {
        type: SELECT,
        id: id
    }
};