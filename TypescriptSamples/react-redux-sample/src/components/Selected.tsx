import {State, Value} from "../core/types";
import {connect} from "react-redux";
import React from "react";

type ComponentProps = {
    value: Value
}

const SelectedComponent: React.FC<ComponentProps> = (props: ComponentProps) => {
    return (
        <div>
            <h1>Selected value</h1>
            <p>id: <b>{props.value.id}</b></p>
            <p>name: <b>{props.value.name}</b></p>
        </div>
    )
};

const mapStateToProps = (state: State) => ({
    value: state.selected
});

type Props = ReturnType<typeof mapStateToProps>;

const SelectedComponentHOC = (props: Props) => {
   if (props.value == null) {
       return <h1>Value is not selected</h1>
   } else {
       return <SelectedComponent {...props as ComponentProps}/>
   }
};

const Selected = connect<Props, any, any>(mapStateToProps, null)(SelectedComponentHOC);

export default Selected;