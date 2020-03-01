import {createStore, applyMiddleware, Store, Middleware} from "redux";
import {reducer} from "./reducer";
import thunk from 'redux-thunk';
import {AppActions, State} from "./types";

const logger = (store: any) => (next: any) => (action: any) => {
    console.groupCollapsed("dispatching", action.type);
    console.log("Previous state", store.getState());
    console.log("Action", action);
    let result = next(action);
    console.log("Next state", store.getState());
    console.groupEnd();
    return result;
};

let initialState: State = {
    values: [],
    selected: null
};

// tsconfig: "strictFunctionTypes": false
const storeFactory = (state: State): Store<State, AppActions> => applyMiddleware(thunk, logger)(createStore)(
    reducer,
    state
);

const store = storeFactory(initialState);

export default store;
