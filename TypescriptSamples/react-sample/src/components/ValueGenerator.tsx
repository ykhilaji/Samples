import React from "react";

type ContainerProps = {
    readonly value: number;
}

type ContainerState = {
    previousValue: number | undefined;
}

class PreviousAndCurrentValueContainer extends React.Component<ContainerProps, ContainerState> {
    state: ContainerState = {
        previousValue: undefined
    };

    componentWillReceiveProps(nextProps: Readonly<ContainerProps>, nextContext: any): void {
        this.setState({
            previousValue: this.props.value
        });
    }

    render() {
        return <div>
            <p>Previous value: {this.state.previousValue}</p>
            <p>Current value: {this.props.value}</p>
        </div>;
    }
}

type GeneratorState = {
    readonly newValue: number
}

export default class ValueGenerator extends React.Component<any, GeneratorState> {
    timerId: any;

    state: GeneratorState = {
        newValue: Math.random()
    };

    componentDidMount(): void {
        this.timerId = setInterval(() => this.nextTick(), 1000);
    }

    componentWillUnmount(): void {
        clearInterval(this.timerId);
    }

    nextTick(): void {
        this.setState({
            newValue: Math.random()
        })
    }

    render() {
        return <div>
            <PreviousAndCurrentValueContainer value={this.state.newValue}/>
        </div>;
    }
}
