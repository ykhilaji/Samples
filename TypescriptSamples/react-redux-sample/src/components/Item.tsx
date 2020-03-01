import {Dispatch} from "redux";
import {Value} from "../core/types";
import {select, remove, update} from "../core/action";
import {connect} from "react-redux";
import React from "react";

const mapDispatchToProps = (dispatch: Dispatch) => ({
    selectItem(id: string) {
        dispatch(select(id))
    },

    updateItem(id: string, name: string | null) {
        if (name == null) {
            throw new Error("empty value");
        }

        let value: Value = {
            id: id,
            name: name
        };

        dispatch(update(value))
    },

    removeItem(id: string) {
        dispatch(remove(id))
    }
});

type Props = ReturnType<typeof mapDispatchToProps> & {
    value: Value
};

const ItemComponent: React.FC<Props> = (props: Props) => {
    const updateItem = (id: string) => {
        let newName = prompt("Update item name");
        props.updateItem(id, newName);
    };

    const removeItem = (id: string) => {
        props.removeItem(id);
    };

    const selectItem = (id: string) => {
        props.selectItem(id);
    };

    return (
        <tr>
            <td onClick={() => selectItem(props.value.id)}>{props.value.name}</td>
            <td><button type="button" onClick={() => removeItem(props.value.id)}>Remove</button></td>
            <td><button type="button" onClick={() => updateItem(props.value.id)}>Update</button></td>
        </tr>
    )
};

const Item = connect(null, mapDispatchToProps)(ItemComponent);

export default Item;