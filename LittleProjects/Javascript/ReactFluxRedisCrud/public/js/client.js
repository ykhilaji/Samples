import React from 'react'
import ReactDOM from 'react-dom'
import {EventEmitter} from 'events'
import {Dispatcher} from 'flux'
import fetch from 'isomorphic-fetch'


class AppDispatcher extends Dispatcher {
    handleAction(action) {
        this.dispatch({
            source: 'RedisCrud',
            action
        });
    }
}

const actions = dispatcher => ({
    getById(id) {
        console.log("Get by id");
        fetch(`http://localhost:8080/api/${id}`).then(res => {
            if (res.ok) {
                return res.json();
            } else {
                throw res.json();
            }
        }).then(body => {
            dispatcher.handleAction({type: 'GET_BY_ID', body: body})
        }).catch(err => {
            dispatcher.handleAction({type: 'ERROR', body: err})
        })
    },

    insert(value) {
        console.log("Insert new entity");
        fetch(`http://localhost:8080/api`, {
            method: 'put',
            headers: {
                'Accept': 'application/json',
                'Content-type': 'application/json'
            },
            body: JSON.stringify({id: 0, value: value})
        }).then(res => {
            if (res.ok) {
                return res.json();
            } else {
                throw res.json();
            }
        }).then(body => {
            dispatcher.handleAction({type: 'INSERT', body: body})
        }).catch(err => {
            dispatcher.handleAction({type: 'ERROR', body: err})
        })
    },

    update(id, newValue) {
        console.log("Update entity");
        fetch(`http://localhost:8080/api`, {
            method: 'post',
            headers: {
                'Accept': 'application/json',
                'Content-type': 'application/json'
            },
            body: JSON.stringify({id: id, value: newValue})
        }).then(res => {
            if (res.ok) {
                return res.json();
            } else {
                throw res.json();
            }
        }).then(body => {
            dispatcher.handleAction({type: 'UPDATE', body: body})
        }).catch(err => {
            dispatcher.handleAction({type: 'ERROR', body: err})
        })
    },

    deleteById(id) {
        console.log("Delete entity");
        fetch(`http://localhost:8080/api?id=${id}`, {
            method: 'delete',
            headers: {
                'Accept': 'application/json',
                'Content-type': 'application/json'
            },
        }).then(res => {
            if (res.ok) {
                return res.json();
            } else {
                throw res.json();
            }
        }).then(body => {
            dispatcher.handleAction({type: 'DELETE_BY_ID', body: body})
        }).catch(err => {
            dispatcher.handleAction({type: 'ERROR', body: err})
        })
    }
});

class Store extends EventEmitter {
    constructor(items = [], dispatcher) {
        super();
        this.items = items;
        this.dispatcherIndex = dispatcher.register(this.dispatch.bind(this));
    }

    dispatch(payload) {
        let {type, body} = payload.action;

        switch (type) {
            case 'GET_BY_ID':
                if (this.items.findIndex((entity, i) => entity.id === body.id) === -1) {
                    this.items = [...this.items, body];
                    this.emit('GET_BY_ID', this.items);
                }
                return true;
            case 'INSERT':
                this.items = [...this.items, body];
                this.emit('INSERT', this.items);
                return true;
            case 'UPDATE':
                this.items = this.items.map(entity => entity.id === body.id ? {id: entity.id, value: body.value} : entity);
                this.emit('UPDATE', this.items);
                return true;
            case 'DELETE_BY_ID':
                this.items = this.items.filter(entity => entity.id !== parseInt(body.id));
                this.emit('DELETE_BY_ID', this.items);
                return true;
            default:
                return true;
        }
    }
}

const Item = ({item, deleteById, update}) => (
    <tr>
        <td>{item.id}</td>
        <td>{item.value}</td>
        <td onClick={() => update(item.id, prompt("New value"))}>Edit</td>
        <td onClick={() => deleteById(item.id)}>Delete</td>
    </tr>
);

const Table = ({items, deleteById, update}) => (
    <div>
        <table className="table table-hover">
            <thead>
            <tr>
                <th>ID</th>
                <th>Value</th>
                <th>Edit</th>
                <th>Delete</th>
            </tr>
            </thead>
            <tbody>
                {(items || []).map((item, i) => <Item key={i} item={item} deleteById={deleteById} update={update}/>)}
            </tbody>
        </table>
    </div>
);

const GetById = ({getById}) => {
    let id;
    const beforeGetById = () => {
        if (id.value <= 0) {
            alert("ID is incorrect");
        } else {
            getById(id.value);
            id.value = '';
        }
    };

    return (<div>
        <div className="form-group">
            <label htmlFor="entityId">ID:</label>
            <input type="number" min="0" className="form-control" ref={input => id = input} id="entityId"/>
        </div>
        <button type="button" className="btn btn-success" onClick={beforeGetById}>Get by id</button>
    </div>)
};

const Insert = ({insert}) => {
    let newValue;
    const beforeInsert = () => {
        if (newValue.value.trim().length === 0) {
            alert("Value is empty");
        } else {
            insert(newValue.value);
            newValue.value = '';
        }
    };

    return (<div>
        <div className="form-group">
            <label htmlFor="entityValue">Value:</label>
            <input type="text" className="form-control" ref={input => newValue = input} id="entityValue"/>
        </div>
        <button type="button" className="btn btn-success" onClick={beforeInsert}>Create new record</button>
    </div>)
};

const ControlPanel = ({getById, insert}) => (
    <div>
        <GetById getById={getById}/>
        <Insert insert={insert}/>
    </div>
);

const App = ({items = {}, actions}) => (
    <div>
        <ControlPanel {...actions}/>
        <Table items={items} {...actions}/>
    </div>
);


const render = (items = []) => ReactDOM.render(
    <App items={items} actions={appActions}/>,
    document.getElementById("app")
);


const dispatcher = new AppDispatcher();
const appActions = actions(dispatcher);
const store = new Store([], dispatcher);

store.on('GET_BY_ID', () => render(store.items));
store.on('INSERT', () => render(store.items));
store.on('UPDATE', () => render(store.items));
store.on('DELETE_BY_ID', () => render(store.items));

render(store.items);
