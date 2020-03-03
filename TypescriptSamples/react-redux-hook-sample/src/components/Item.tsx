import {Value} from "../core/types";
import {select, remove, update} from "../core/action";
import { useDispatch } from 'react-redux'
import React from "react";

type Props = {
    value: Value
};

const Item: React.FC<Props> = (props: Props) => {
    const dispatch = useDispatch();

    const updateItem = (id: string) => {
        let newName = prompt("Update item name");

        if (newName == null) {
            throw new Error("empty value");
        }

        let value: Value = {
            id: id,
            name: newName
        };

        dispatch(update(value));
    };

    const removeItem = (id: string) => {
        dispatch(remove(id));
    };

    const selectItem = (id: string) => {
        dispatch(select(id));
    };

    return (
        <tr>
            <td onClick={() => selectItem(props.value.id)}>{props.value.name}</td>
            <td><button type="button" onClick={() => removeItem(props.value.id)}>Remove</button></td>
            <td><button type="button" onClick={() => updateItem(props.value.id)}>Update</button></td>
        </tr>
    )
};

export default Item;