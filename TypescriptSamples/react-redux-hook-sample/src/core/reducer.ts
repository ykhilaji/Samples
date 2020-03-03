import {AppActions, State} from "./types";
import {ADD, REMOVE, SELECT, UPDATE} from "./actionType";
import _ from 'lodash'
import {Reducer} from "redux";

export const reducer: Reducer<State, AppActions> = (state: State, action: AppActions): State => {
    switch (action.type) {
        case ADD: return {
            ...state,
            values: [...state.values, action.value]
        };
        case REMOVE: return {
            ...state,
            values: _.filter(state.values, value => value.id !== action.id)
        };
        case UPDATE: return {
            ...state,
            values: [..._.filter(state.values, value => value.id !== action.value.id), action.value]
        };
        case SELECT: return {
            ...state,
            selected: _.find(state.values, value => value.id === action.id) || null
        };
        default: return state;
    }
};