import React from 'react'
import ReactDOM from 'react-dom'
import { Dispatcher } from 'flux'
import { EventEmitter } from 'events'

const Component = ({value, clear, setNewValue, setDefault}) => {
    return (
        <div>
            <button onClick={clear}>CLEAR</button>
            <button onClick={setDefault}>DEFAULT</button>
            <button onClick={() => setNewValue(prompt("Set new value"))}>NEW VALUE</button>
            <p>{value}</p>
        </div>
    )
};

const appActions = dispatcher => ({
    clear() {
        console.log("Clear value");
        dispatcher.handleAction({type: 'CLEAR'});
    },
    setNewValue(newValue) {
        console.log("Setting new value: ", newValue);
        dispatcher.handleAction({type: 'SET_NEW_VALUE', value: newValue});
    },
    setDefault() {
        console.log("Setting default value: ", "DEFAULT");
        dispatcher.handleAction({type: 'SET_DEFAULT_VALUE', value: "DEFAULT"});
    }
});

class AppDispatcher extends Dispatcher {
    handleAction(action) {
        console.log("Dispatching action: " + action);
        this.dispatch({
            source: 'COMPONENT',
            action
        })
    }
}

class AppStore extends EventEmitter {
    constructor(value, dispatcher) {
        super();
        this._value = value;
        this.dispatcherIndex = dispatcher.register(this.dispatch.bind(this));
    }

    dispatch(payload) {
        let type = payload.action.type;

        switch(type) {
            case "CLEAR":
                this._value = 'EMPTY';
                this.emit("CLEAR", this._value);
                return true;
            case "SET_NEW_VALUE":
                this._value = payload.action.value;
                this.emit("SET_NEW_VALUE", this._value);
                return true;
            case "SET_DEFAULT_VALUE":
                this._value = payload.action.value;
                this.emit("SET_DEFAULT_VALUE", this._value);
                return true;
        }
    }
}


const dispatcher = new AppDispatcher();
const actions = appActions(dispatcher);
const store = new AppStore("initial value", dispatcher);

const render = value => ReactDOM.render(
    <Component value={value} {...actions}/>,
    document.getElementById("root")
);

store.on('CLEAR', () => render(store._value));
store.on('SET_NEW_VALUE', () => render(store._value));
store.on('SET_DEFAULT_VALUE', () => render(store._value));

render(store._value);