import * as R from 'ramda'
import {Lens} from "ramda";
import * as actions from "./action";

export type Value = {
    readonly id: string;
    name: string;
}

export type State = {
    values: Value[];
    selected: Value | null
}

export const valueNameLens: Lens = R.lens<Value, string, Value>(R.prop("name"), R.assoc("name"));

type InferType<T> = T extends { [key: string]: infer U} ? U : never;

export type AppActions = ReturnType<InferType<typeof actions>>
