import React, {ErrorInfo} from "react";

type ErrorHandlerState = {
    hasError: false
}

export class ThrowableComponent extends React.Component<any, any> {
    render() {
        throw new Error("Error!!!");
        // eslint-disable-next-line no-unreachable
        return undefined;
    }
}

export default class ErrorHandler extends React.Component<any, ErrorHandlerState>{
    constructor(props: any) {
        super(props);
        this.state = { hasError: false };
    }

    static getDerivedStateFromError(error: Error) {
        return { hasError: true };
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        console.log(error, errorInfo);
    }

    render() {
        if (this.state.hasError) {
            return <p>Error</p>;
        }

        return this.props.children;
    }
}