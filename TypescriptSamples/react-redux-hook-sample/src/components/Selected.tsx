import {State, Value} from "../core/types";
import { useSelector } from 'react-redux'
import React from "react";


const Selected: React.FC = () => {
    const selected = useSelector<State, Value | null>(state => state.selected);

    if (selected == null) {
        return <h1>Value is not selected</h1>
    } else {
        return (
            <div>
                <h1>Selected value</h1>
                <p>id: <b>{selected.id}</b></p>
                <p>name: <b>{selected.name}</b></p>
            </div>
        )
    }
};

export default Selected;