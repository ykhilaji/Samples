import {State, Value} from "../core/types";
import { useSelector } from 'react-redux'
import React from "react";
import _ from "lodash"
import Item from "./Item";


const List: React.FC = () => {
    const values = useSelector<State, Value[]>(state => state.values);

    return (
        <table>
            <caption>Items list</caption>
            <tr>
                <th>Name</th>
                <th>Remove</th>
                <th>Update</th>
            </tr>

            {_.map(values, value => <Item value = {value}/>)}
        </table>
    )
};

export default List;