%%%-------------------------------------------------------------------
%%% @author grifon
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 02. Mar 2019 12:38
%%%-------------------------------------------------------------------
-module(basic).
-author("grifon").

%% API
-export([length_of_list/1, length_of_list/2, sum_even/1, sum_even/2, reverse/1, sum/2]).

length_of_list(L) -> length_of_list(L, 0).

length_of_list([], Acc) -> Acc;

length_of_list([_ | T], Acc) -> length_of_list(T, Acc + 1).

sum_even(L) -> sum_even(L, 0).

sum_even([], Acc) -> Acc;

sum_even([D | T], Acc) when D rem 2 /= 0 -> sum_even(T, Acc);

sum_even([D | T], Acc) when D rem 2 == 0 -> sum_even(T, Acc + D).

reverse([], Acc) -> Acc;

reverse([H | T], Acc) -> reverse(T, [H | Acc]).

reverse(L) -> reverse(L, []).

sum(A, B) -> A + B.