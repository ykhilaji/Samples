import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { add } from '../core/action'
import React from "react";
import { Value } from "../core/types";

// from SO ^_^
function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        let r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
    addNewItem(name: string | null) {
        if (name == null) {
            throw new Error("empty value");
        }

        let value: Value = {
            id: uuidv4(),
            name: name
        };

        dispatch(add(value))
    }
});

type Props = ReturnType<typeof mapDispatchToProps>;

const NewItemComponent: React.FunctionComponent<Props> = (props: Props) => {
    const handleNewItem = () => {
        props.addNewItem(prompt("Item name"));
    };

    return (
      <div>
          <button type="button" onClick={handleNewItem}>NEW ITEM</button>
      </div>
    )
};

const NewItem = connect(null, mapDispatchToProps)(NewItemComponent);

export default NewItem;