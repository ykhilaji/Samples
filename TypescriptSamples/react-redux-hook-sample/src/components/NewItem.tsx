import { useDispatch } from 'react-redux'
import { add } from '../core/action'
import React from "react";
import { Value } from "../core/types";

// from SO ^_^
function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        // eslint-disable-next-line no-mixed-operators
        let r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

const NewItem: React.FunctionComponent = () => {
    const dispatch = useDispatch();

    const handleNewItem = () => {
        let name = prompt("Item name");

        if (name == null) {
            throw new Error("empty value");
        }

        let value: Value = {
            id: uuidv4(),
            name: name
        };

        dispatch(add(value));
    };

    return (
        <div>
            <button type="button" onClick={handleNewItem}>NEW ITEM</button>
        </div>
    )
};

export default NewItem;