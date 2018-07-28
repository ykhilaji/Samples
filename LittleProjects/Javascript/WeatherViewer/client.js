import React from 'react'
import ReactDOM from 'react-dom'
import { Dispatcher } from 'flux'
import { EventEmitter } from 'events'
import fetch from 'isomorphic-fetch'

const WeatherMainInfo = ({temp, pressure, humidity, temp_min, temp_max}) => (
    <div>
        <p>Temperature: {temp}</p>
        <p>Pressure: {pressure}</p>
        <p>Humidity: {humidity}</p>
        <p>Min temperature: {temp_min}</p>
        <p>Max temperature: {temp_max}</p>
    </div>
);

const WeatherDescription = ({weather}) => (
    weather ?
        (<div>
            <p>Description</p>
            {weather.map((x, i) => {
                let {main, description} = x;
                return (
                    <div key={i}>
                        <p>{main}</p>
                        <p>{description}</p>
                    </div>
                )
            })}
        </div>) : <p>No additional descriptions</p>
);

const WeatherWind = ({speed}) => (
    <div>
        <p>Wind speed: {speed}</p>
    </div>
);

const WeatherComponent = ({weather, main, wind, name}) => (
    !name ? (<p>Error</p>) : (<div>
        <p>Weather info for {name}</p>
        <WeatherMainInfo {...main}/>
        <WeatherDescription weather={weather}/>
        <WeatherWind {...wind}/>
    </div>)
);

class WeatherDispatcher extends Dispatcher {
    handleAction(action) {
        this.dispatch({
            source: 'WEATHER_API',
            action
        });
    }
}

class WeatherStore extends EventEmitter {
    constructor(body={}, dispatcher) {
        super();
        this._body = body;
        this.dispatcherIndex = dispatcher.register(this.dispatch.bind(this));
    }

    dispatch(payload) {
        let {type, body} = payload.action;

        switch (type) {
            case 'FETCH_WEATHER':
                this._body = body;
                this.emit("FETCH_WEATHER", this._body);
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

const FetchWrapper = (state={}, {fetchWeather}) =>
    class FetchWrapper extends React.Component {
        constructor(props) {
            super(props);
            this.fetchWeather = this.fetchWeather.bind(this);
        }

        fetchWeather() {
            let city = this.refs._input;

            fetchWeather(city.value);
        }

        render() {
            return (
                <div>
                    <input type="text" ref="_input"/>
                    <button onClick={this.fetchWeather}>Get</button>
                    { state.info !== null ? <WeatherComponent {...state.info}/> : <p>Select a city</p> }
                </div>
            )
        }
    };

const weatherActions = dispatcher => ({
    fetchWeather(city) {
        console.log("Fetch url: ", `http://api.openweathermap.org/data/2.5/weather?q=${city}&units=metric&appid=de89d8ca60022a688e0dfa15501e83fa`);
        fetch(`http://api.openweathermap.org/data/2.5/weather?q=${city}&units=metric&appid=de89d8ca60022a688e0dfa15501e83fa`)
            .then(function (response) {
                if (response.ok) {
                    return response.json();
                } else {
                    throw response.json();
                }
            }).then(function (body) {
            dispatcher.handleAction({type: 'FETCH_WEATHER', body: body});
        }).catch(function (body) {
            dispatcher.handleAction({type: 'ERROR', body: body});
        });
    }
});

const dispatcher = new WeatherDispatcher();
const actions = weatherActions(dispatcher);
const store = new WeatherStore({}, dispatcher);

const render = state => {
    let FetchWrapperComponent = FetchWrapper(state, actions);
    ReactDOM.render(
        <FetchWrapperComponent/>,
        document.getElementById("root")
    );
};

store.on('FETCH_WEATHER', () => render({info: store._body}));
store.on('ERROR', () => render({info: store._body}));

render({info: null});
