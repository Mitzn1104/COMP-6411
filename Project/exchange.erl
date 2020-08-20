%% @author Mitalee
%% @doc @todo Add description to exchange.

-module(exchange).

%% ====================================================================
%% API functions
%% ====================================================================
-export([start/0]).
%% ====================================================================
%% Internal functions
%% ====================================================================
start() ->
	io:format("** Calls to be made **\n"),
	{ok,Data} = file:consult("calls.txt"),
	%%display calls to be made
	[display_calls(Caller, Contacts) || {Caller, Contacts} <- Data],
	io:fwrite("\n"),
	
	MapofData = maps:from_list(Data),
	%%Spawn each contact
	PidList = [{Key,spawn(calling, listen, [Key])} || Key <- maps:keys(MapofData) ],
	PidMap = maps:from_list(PidList),
	%%Send receiverList to each Contact process
	foreach(MapofData, PidMap, maps:keys(MapofData) ),
	%%Start master process receiver
	get_feedback().

display_calls(Caller, Contacts) ->
    io:format("~w: ~w~n", [Caller, Contacts]).
	
 get_feedback() ->
 	receive
 		{ReceiverName, Msg, SenderName,MicroSecs} -> 
 			io:fwrite("~s ~s ~s [~w]\n", [ReceiverName,Msg,SenderName,MicroSecs]),	
 		    get_feedback()
 	after 10000 -> io:fwrite("\nMaster has received no replies for 10 seconds, ending...\n")
 	end.

foreach(MapofData,PidMap, [H|T]) ->
	maps:get(H,PidMap)! {self(), {H,maps:get(H,MapofData),PidMap}},
    foreach(MapofData,PidMap,T);
foreach(MapofData,PidMap, []) ->
    ok.