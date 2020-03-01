import React from 'react';

type CounterState = {
    counter: number
}

export default class ClickCounter extends React.Component<any, CounterState> {
    constructor(props: any) {
        super(props);

        this.state = {
            counter: 0
        };

        this.increment = this.increment.bind(this);
    }

    increment() {
        this.setState((state, props) => ({
            counter: state.counter + 1
        }))
    }

    render() {
        return (
            <div>
                <p>Clicked: {this.state.counter}</p>
                <button onClick={this.increment}>CLICK</button>
            </div>
        )
    }

}
