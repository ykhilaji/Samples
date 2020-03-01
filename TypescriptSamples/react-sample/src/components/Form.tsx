import React, {ChangeEvent} from "react";

export default class Form extends React.Component<any, any>{
    constructor(props: any) {
        super(props);

        this.state = {};
        this.handleEvent = this.handleEvent.bind(this);
    }

    handleEvent(e: ChangeEvent<HTMLInputElement>) {
        e.preventDefault();

        const target = e.target;

        this.setState({
            [target.name]: target.value // dynamic name resolution
        });
    }

    render() {
        return (
            <div>
                <label>
                    A1:
        <input name="a1" type="text" value={this.state.a1} onChange={this.handleEvent} />
        </label>
        <br />
        <label>
            A2:
        <input name="a2" type="number" value={this.state.a2} onChange={this.handleEvent} />
        </label>
        </div>
    )
    }
}