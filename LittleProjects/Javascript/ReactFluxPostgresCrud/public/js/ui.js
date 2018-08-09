import React from 'react'
import ReactDOM from 'react-dom'
import {EventEmitter} from 'events'
import {Dispatcher} from 'flux'
import fetch from 'isomorphic-fetch'

class AppDispatcher extends Dispatcher {
    handleAction(action) {
        this.dispatch({
            source: 'PgCrud',
            action
        });
    }
}

const actions = dispatcher => ({
    getById(id) {
        fetch(`http://localhost:8080/api/${id}`)
            .then(response => {
                if (response.ok) {
                    return response.json()
                } else {
                    throw response.json()
                }
            }).then(body => {
            dispatcher.handleAction({type: 'GET_BY_ID', body: body});
        }).catch(error => dispatcher.handleAction({type: 'ERROR', body: error}));
    },

    getAll() {
        fetch(`http://localhost:8080/api`)
            .then(response => {
                if (response.ok) {
                    return response.json()
                } else {
                    throw response.json()
                }
            }).then(body => {
            dispatcher.handleAction({type: 'GET_ALL', body: body});
        }).catch(error => dispatcher.handleAction({type: 'ERROR', body: error}));
    },

    deleteById(id) {
        fetch(`http://localhost:8080/api/${id}`, {
            method: "DELETE",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
        }).then(response => {
            if (response.ok) {
                return response.json()
            } else {
                throw response.statusText;
            }
        }).then(body => {
            dispatcher.handleAction({type: 'DELETE_BY_ID', body: body});
        }).catch(error => dispatcher.handleAction({type: 'ERROR', body: error}));
    },

    create(value) {
        fetch(`http://localhost:8080/api`, {
            method: "PUT",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({value: value})
        }).then(response => {
            if (response.ok) {
                return response.json()
            } else {
                throw response.statusText;
            }
        }).then(body => {
            dispatcher.handleAction({type: 'CREATE', body: body});
        }).catch(error => dispatcher.handleAction({type: 'ERROR', body: error}));
    },

    update(id, value) {
        fetch(`http://localhost:8080/api`, {
            method: "POST",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({id: id, value: value})
        }).then(response => {
            if (response.ok) {
                return response.json()
            } else {
                throw response.statusText;
            }
        }).then(body => {
            dispatcher.handleAction({type: 'UPDATE', body: body});
        }).catch(error => dispatcher.handleAction({type: 'ERROR', body: error}));
    }
});

class Store extends EventEmitter {
    constructor(items=[], dispatcher) {
        super();
        this.items = items;
        this.dispatcherIndex = dispatcher.register(this.dispatch.bind(this));
    }

    dispatch(payload) {
        let {type, body} = payload.action;

        switch (type) {
            case 'GET_BY_ID':
                this.items = body; // an array with one element or an empty array
                this.emit("GET_BY_ID", this.items);
                return true;
            case 'GET_ALL':
                this.items = body;
                this.emit("GET_ALL", this.items);
                return true;
            case 'DELETE_BY_ID':
                this.items = this.items.filter(el => el.id !== parseInt(body.id));
                this.emit("DELETE_BY_ID", this.items);
                return true;
            case 'CREATE':
                this.items = [...this.items, body];
                this.emit("CREATE", this.items);
                return true;
            case 'UPDATE':
                this.items = this.items.map(el => el.id === body.id ? {id: el.id, value: body.value} : el);
                this.emit("UPDATE", this.items);
                return true;
            default:
                return true;
        }
    }
}


const Item = ({id, value, deleteById, update}) => (
    <tr>
        <td>{id}</td>
        <td>{value}</td>
        <td onClick={() => deleteById(id)}>Delete</td>
        <td onClick={() => update(id, prompt('New value'))}>Edit</td>
    </tr>
);

const ItemList = ({items, deleteById, update}) => (
    <div>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Value</th>
                    <th>Delete</th>
                    <th>Update</th>
                </tr>
            </thead>
            <tbody>
                {items ? items.map((el, i) => <Item key={i} {...el} deleteById={deleteById} update={update}/>) : null}
            </tbody>
        </table>
    </div>
);

const Actions = ({getById, getAll, create}) => (
    <div>
        <button onClick={getAll}>Get all</button>
        <button onClick={() => getById(prompt("ID"))}>Get by id</button>
        <button onClick={() => create(prompt("Value"))}>Create</button>
    </div>
);

const App = ({items, actions}) => (
    <div>
        <Actions {...actions}/>
        <ItemList items={items} {...actions}/>
    </div>
);

const dispatcher = new AppDispatcher();
const appActions = actions(dispatcher);
const store = new Store([], dispatcher);

const render = (items=[]) => ReactDOM.render(
    <App items={items} actions={appActions}/>,
    document.getElementById("root")
);

render(store.items);

store.on('GET_BY_ID', () => render(store.items));
store.on('GET_ALL', () => render(store.items));
store.on('DELETE_BY_ID', () => render(store.items));
store.on('CREATE', () => render(store.items));
store.on('UPDATE', () => render(store.items));
store.on('ERROR', () => render(store.items));