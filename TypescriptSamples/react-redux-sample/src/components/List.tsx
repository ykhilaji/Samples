import {State} from "../core/types";
import {connect} from "react-redux";
import React from "react";
import _ from "lodash"
import Item from "./Item";

const mapStateToProps = (state: State) => ({
    values: state.values
});

type Props = ReturnType<typeof mapStateToProps>;

const ListComponent: React.FC<Props> = (props: Props) => {
    return (
        <table>
            <caption>Items list</caption>
            <tr>
                <th>Name</th>
                <th>Remove</th>
                <th>Update</th>
            </tr>

            {_.map(props.values, value => <Item value = {value}/>)}
        </table>
    )
};

const List = connect<Props, any, any>(mapStateToProps, null)(ListComponent);

export default List;