%%%-------------------------------------------------------------------
%%% @author grifon
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 02. Mar 2019 13:07
%%%-------------------------------------------------------------------
-module(basic_tests).
-author("grifon").

-include_lib("eunit/include/eunit.hrl").
-import(basic, [length_of_list/1, sum_even/1, reverse/1, sum/2]).
-import(lists, [all/2, any/2, append/2, delete/2, droplast/1]).
%% header file
-include("header.hrl").

length_of_list_test() -> ?assertEqual(5, length_of_list([1, 2, 3, 4, 5])).

length_of_empty_list_test() -> ?assertEqual(0, length_of_list([])).

sum_even_test() -> ?assertEqual(2, sum_even([1, 2, 3])).

sum_even_zero_test() -> ?assertEqual(0, sum_even([1, 3])).

sum_even_empty_test() -> ?assertEqual(0, sum_even([])).

reverse_test() -> ?assertEqual([3, 2, 1], reverse([1, 2, 3])).

lists_function_test() ->
  ?assert(all(fun(N) -> N > 0 end, [1, 2, 3])),
  ?assert(not all(fun(N) -> N rem 2 == 0 end, [1, 2, 3])),
  ?assert(any(fun(N) -> N == 3 end, [1, 2, 3])),
  ?assertEqual([1, 2, 3, 4], append([1, 2, 3], [4])),
  ?assertEqual([1, 2, 3], delete(4, [1, 2, 3, 4])),
  ?assertEqual([1, 2, 3], droplast([1, 2, 3, 4])).

map_test() ->
  M = #{'1' => 1, '2' => 2, '3' => 3},
  ?assertEqual(1, maps:get('1', M)).

tuple_test() ->
  ?assert(not is_tuple(1)),
  ?assert(is_tuple({1, 2, 3})).

-record(myrecord, {'i' = ""}).
record_test() ->
  R = #myrecord{'i' = "value"},
  ?assertEqual("value", R#myrecord.i).

-define(m, 1).
constant_macros_test() -> ?assertEqual(1, ?m).

-define(sum(A, B), A + B).
function_macros_test() -> ?assertEqual(3, ?sum(1, 2)).

-ifdef(false).
-define(n, 1).
-else.
-define(n, 2).
-endif.
if_else_macros_test() -> ?assertEqual(2, ?n).

header_macros_test() ->
  ?assertEqual(1, ?a),
  ?assertEqual(4, ?mul(2, 2)).

higher_order_function_test() ->
  Adder = fun(A) -> fun(B) -> A + B end end,
  Incr = Adder(1),
  ?assertEqual(2, Incr(1)),
  ?assertEqual(3, Incr(2)).

process_test() ->
  spawn(?MODULE, sum, [1, 2]).
