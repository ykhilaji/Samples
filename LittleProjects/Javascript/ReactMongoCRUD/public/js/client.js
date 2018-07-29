import React from 'react'
import ReactDOM from 'react-dom'
import {Dispatcher} from 'flux'
import {EventEmitter} from 'events'
import fetch from 'isomorphic-fetch'


class AppDispatcher extends Dispatcher {
    handleAction(action) {
        this.dispatch({
            source: 'TODO_API',
            action
        });
    }
}

class TodoStore extends EventEmitter {
    constructor(body={}, dispatcher) {
        super();
        this._body = body;
        this.dispatcherIndex = dispatcher.register(this.dispatch.bind(this));
    }

    dispatch(payload) {
        let {type, body} = payload.action;

        switch (type) {
            case 'SAVE_TODO':
                this._body = [...this._body, body];
                this.emit("SAVE_TODO", this._body);
                return true;
            case 'UPDATE_TODO':
                this._body = this._body.map(el => el._id === body._id ? body : el);
                this.emit("UPDATE_TODO", this._body);
                return true;
            case 'DELETE_TODO':
                this._body = this._body.filter(el => el._id !== body._id);
                this.emit("DELETE_TODO", this._body);
                return true;
            case 'GET_TODO_BY_ID':
                this._body = body;
                this.emit("GET_TODO_BY_ID", this._body);
                return true;
            case 'GET_TODOS_BY_TYPE':
                this._body = body;
                this.emit("GET_TODOS_BY_TYPE", this._body);
                return true;
            case 'ERROR':
                this._body = body;
                this.emit("ERROR", this._body);
                return true;
            default:
                return true;
        }
    }
}

const todoActions = dispatcher => ({
    save(type, body) {
        fetch(`http://localhost:8080/api/todo`, {
            method: "PUT",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({type: type, body: body})
        })
            .then(function (response) {
                if (response.ok) {
                    return response.json();
                } else {
                    throw response.json();
                }
            }).then(function (body) {
            dispatcher.handleAction({type: 'SAVE_TODO', body: body});
        }).catch(function (body) {
            dispatcher.handleAction({type: 'ERROR', body: body});
        });
    },

    update(id, newType, newBody) {
        fetch(`http://localhost:8080/api/todo`, {
            method: "POST",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({id: id, type: newType, body: newBody})
        })
            .then(function (response) {
                if (response.ok) {
                    return response.json();
                } else {
                    throw response.json();
                }
            }).then(function (body) {
            dispatcher.handleAction({type: 'UPDATE_TODO', body: body});
        }).catch(function (body) {
            dispatcher.handleAction({type: 'ERROR', body: body});
        });
    },

    deleteById(id) {
        fetch(`http://localhost:8080/api/todo`, {
            method: "DELETE",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({id: id})
        })
            .then(function (response) {
                if (response.ok) {
                    return response.json();
                } else {
                    throw response.json();
                }
            }).then(function (body) {
            dispatcher.handleAction({type: 'DELETE_TODO', body: body});
        }).catch(function (body) {
            dispatcher.handleAction({type: 'ERROR', body: body});
        });
    },

    getById(id) {
        fetch(`http://localhost:8080/api/todo/${id}`)
            .then(function (response) {
                if (response.ok) {
                    return response.json();
                } else {
                    throw response.json();
                }
            }).then(function (body) {
            dispatcher.handleAction({type: 'GET_TODO_BY_ID', body: body});
        }).catch(function (body) {
            dispatcher.handleAction({type: 'ERROR', body: body});
        });
    },

    getByType(type) {
        fetch(`http://localhost:8080/api/todo?type=${type}`)
            .then(function (response) {
                if (response.ok) {
                    return response.json();
                } else {
                    throw response.json();
                }
            }).then(function (body) {
            dispatcher.handleAction({type: 'GET_TODOS_BY_TYPE', body: body});
        }).catch(function (body) {
            dispatcher.handleAction({type: 'ERROR', body: body});
        });
    }
});

const TodoList = ({items, actions}) => (
    <table className="table table-hover">
        <thead>
        <tr>
            <th>Type</th>
            <th>Body</th>
            <th>Update</th>
            <th>Delete</th>
        </tr>
        </thead>
        <tbody>
            {Array.isArray(items) ? (items.map((item, i) => <TodoItem key={i} {...item} {...actions}/>)) : ("")}
        </tbody>
    </table>
);

const TodoItem = ({_id, type, body, deleteById, update}) => (
    <tr>
        <td>{type}</td>
        <td>{body}</td>
        <td>
            <span className="glyphicon glyphicon-pencil" onClick={() => {
                let idInput = document.getElementById("updateId");
                let typeInput = document.getElementById("updateType");
                let bodyInput = document.getElementById("updateBody");

                idInput.value = _id;
                typeInput.value = type;
                bodyInput.value = body;
            }} data-toggle="modal" data-target="#updateModal"></span>
        </td>
        <td>
            <span className="glyphicon glyphicon-remove" onClick={() => deleteById(_id)}></span>
        </td>
    </tr>
);

const SearchByType = ({getByType}) => (
    <div className="input-group">
        <input type="text" className="form-control" placeholder="Search by type" id="typeInput"/>
            <div className="input-group-btn">
                <button className="btn btn-default" type="button" onClick={() => getByType(document.getElementById("typeInput").value)}>
                    <i className="glyphicon glyphicon-search"></i>
                </button>
            </div>
    </div>
);

const Panel = ({state, actions}) => (
    <div className="panel panel-success">
        <div className="panel-heading">
                <button type="button" className="btn btn-success" data-toggle="modal" data-target="#saveModal">Add new</button>
        </div>
        <div className="panel-body"><TodoList items={state} actions={actions}/></div>
        <div className="panel-footer"><SearchByType getByType={actions.getByType}/></div>
    </div>
);

class SaveModal extends React.Component {
    constructor({save}) {
        super();

        this.saveAPI = save;
        this.save = this.save.bind(this);
    }

    save() {
        let type = this.refs._type;
        let body = this.refs._body;

        this.saveAPI(type.value, body.value);

        type.value = '';
        body.value = '';
    }

    render() {
        return (
            <div id="saveModal" className="modal fade" role="dialog">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal">&times;</button>
                            <h4 className="modal-title">Save new todo item</h4>
                        </div>
                        <div className="modal-body">
                            <div className="form-group">
                                <label htmlFor="type">Type:</label>
                                <input type="text" className="form-control" id="type" ref="_type"/>
                            </div>
                            <div className="form-group">
                                <label htmlFor="body">Body:</label>
                                <input type="text" className="form-control" id="body" ref="_body"/>
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-default" data-dismiss="modal" onClick={this.save}>Ok</button>
                            <button type="button" className="btn btn-default" data-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

class UpdateModal extends React.Component {
    constructor({update}) {
        super();

        this.updateAPI = update;
        this.update = this.update.bind(this);
    }

    update() {
        let id = this.refs._id;
        let type = this.refs._type;
        let body = this.refs._body;

        this.updateAPI(id.value, type.value, body.value);

        id.value = '';
        type.value = '';
        body.value = '';
    }

    render() {
        return (
            <div id="updateModal" className="modal fade" role="dialog">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal">&times;</button>
                            <h4 className="modal-title">Save new todo item</h4>
                        </div>
                        <div className="modal-body">
                            <div className="form-group">
                                <input type="hidden" className="form-control" id="updateId" ref="_id"/>
                            </div>
                            <div className="form-group">
                                <label htmlFor="updateType">Type:</label>
                                <input type="text" className="form-control" id="updateType" ref="_type"/>
                            </div>
                            <div className="form-group">
                                <label htmlFor="updateBody">Body:</label>
                                <input type="text" className="form-control" id="updateBody" ref="_body"/>
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-default" data-dismiss="modal" onClick={this.update}>Ok</button>
                            <button type="button" className="btn btn-default" data-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

const Wrapper = ({state, actions}) => (
    <div>
        <SaveModal save={actions.save}/>
        <UpdateModal update={actions.update}/>
        <Panel state={state} actions={actions}/>
    </div>
);

const dispatcher = new AppDispatcher();
const actions = todoActions(dispatcher);
const store = new TodoStore({}, dispatcher);

const render = state => ReactDOM.render(
    <Wrapper state={state} actions={actions}/>,
    document.getElementById("root")
);

store.on('SAVE_TODO', () => render(store._body));
store.on('UPDATE_TODO', () => render(store._body));
store.on('DELETE_TODO', () => render(store._body));
store.on('GET_TODO_BY_ID', () => render(store._body));
store.on('GET_TODOS_BY_TYPE', () => render(store._body));
store.on('ERROR', () => render(store._body));

render([]);