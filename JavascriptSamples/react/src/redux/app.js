import React from 'react'
import ReactDOM from 'react-dom'
import { createStore, combineReducers, applyMiddleware } from 'redux'
import { Provider, connect } from 'react-redux'

const mapStateToProps = state => ({
    state: state
});

const mapDispatchToProps = dispatch => ({
    dispatch: dispatch
});

const Container = () => {
    return (
        <div>
            <InputConnect/>
            <ItemTableConnect/>
        </div>
    )
};

const ContainerConnect = connect(
    mapStateToProps,
    mapDispatchToProps
)(Container);

const Input = ({dispatch}) => {
    let _category, _body;

    const addNewItem = () => {
        dispatch(addItem(_category.value, _body.value));
        _category.value = '';
        _body.value = '';
    };

    return (
        <div>
            <p>Category:</p>
            <input type="text" ref={input => _category = input}/>
            <p>Body:</p>
            <input type="text" ref={input => _body = input}/>
            <button type="button" onClick={addNewItem}>Add</button>
        </div>
    )
};

const InputConnect = connect(
    null,
    mapDispatchToProps
)(Input);

const sortFunction = type => {
    switch (type) {
        case sortTypes.SORT_BY_BODY:
            return (a, b) => a.body.localeCompare(b.body);
        case sortTypes.SORT_BY_CATEGORY:
            return (a, b) =>  a.category.localeCompare(b.category);
    }
};

const ItemTable = ({state, dispatch}) => {
    let {items, sort} = state;

    console.log(items);

    return (
        <div>
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th onClick={() => dispatch(sortBy(sortTypes.SORT_BY_CATEGORY))}>Category</th>
                    <th onClick={() => dispatch(sortBy(sortTypes.SORT_BY_BODY))}>Body</th>
                    <th>Edit</th>
                    <th>Remove</th>
                </tr>
                </thead>
                <tbody>
                    {items.sort(sortFunction(sort)).map((el, i) => <ItemConnect key={i} {...el}/>)}
                </tbody>
            </table>
        </div>
    )
};


const ItemTableConnect = connect(
    mapStateToProps,
    mapDispatchToProps
)(ItemTable);


const Item = ({id, category, body, dispatch}) => {
    return (
        <tr>
            <td>{id}</td>
            <td>{category}</td>
            <td>{body}</td>
            <td onClick={() => dispatch(editItem(id, prompt("New body")))}>Edit</td>
            <td onClick={() => dispatch(removeItem(id))}>Remove</td>
        </tr>
    )
};

const ItemConnect = connect(
    null,
    mapDispatchToProps
)(Item);

const actions = {
    ADD_ITEM: "ADD_ITEM",
    REMOVE_ITEM: "REMOVE_ITEM",
    EDIT_ITEM: "EDIT_ITEM",
    SORT_ITEMS: "SORT_ITEMS",
};

const sortTypes = {
    SORT_BY_BODY: "SORT_BY_BODY",
    SORT_BY_CATEGORY: "SORT_BY_CATEGORY"
};

let globalItemId = 0;

const item = (state = {}, action) => {
    switch (action.type) {
        case actions.ADD_ITEM:
            return {
                id: ++globalItemId,
                category: action.category,
                body: action.body
            };
        case actions.EDIT_ITEM:
            return action.id === state.id ? {
                ...state,
                body: action.body
            } : state;
        default:
            return state;
    }
};

const items = (state = [], action) => {
    switch (action.type) {
        case actions.ADD_ITEM:
            return [
                ...state,
                item({}, action)
            ];
        case actions.EDIT_ITEM:
            return state.map(el => item(el, action));
        case actions.REMOVE_ITEM:
            return state.filter(el => el.id !== action.id);
        default:
            return state;
    }
};

const sort = (state = sortTypes.SORT_BY_CATEGORY, action) => {
    switch (action.type) {
        case actions.SORT_ITEMS:
            return action.sortBy;
        default:
            return state;
    }
};

const addItem = (category, body) => ({
    type: actions.ADD_ITEM,
    category: category,
    body: body
});

const removeItem = id => ({
    type: actions.REMOVE_ITEM,
    id: id
});

const editItem = (id, body) => ({
    type: actions.EDIT_ITEM,
    id: id,
    body: body
});

const sortBy = type => ({
    type: actions.SORT_ITEMS,
    sortBy: type === sortTypes.SORT_BY_BODY ? sortTypes.SORT_BY_BODY : sortTypes.SORT_BY_CATEGORY
});

const around = store => next => action => {
    console.log(`Before action: ${action}`);
    let result = next(action);
    console.log(`After action: ${action}`);

    return result;
};

const storeFactory = (initialState = {items: [], sort: sortTypes.SORT_BY_BODY}) => applyMiddleware(around)(createStore)(combineReducers({
    items,
    sort
}), initialState);

const store = storeFactory();

const render = () => ReactDOM.render(
    <Provider store={store}>
        <ContainerConnect/>
    </Provider>,
    document.getElementById("root")
);

store.subscribe(() => console.log("Some action was performed"));
store.subscribe(() => render);

render();