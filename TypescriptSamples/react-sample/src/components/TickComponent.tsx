import React from "react";

const tick = (): String => {
    let date = new Date();

    return date.toLocaleDateString() + " - " + date.toLocaleTimeString();
};

type State = {
    currentDate: String
}

export default class TickComponent extends React.Component<any, State> {
    timerId: any;

    state: State = {
        currentDate: tick()
    };

    componentDidMount(): void {
        this.timerId = setInterval(() => this.nextTick(), 1000);
    }

    componentWillUnmount(): void {
        clearInterval(this.timerId);
    }

    nextTick(): void {
        this.setState({
            currentDate: tick()
        })
    }

    render() {
        return <div>Time: {this.state.currentDate}</div>;
    }
}