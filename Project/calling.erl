%% @author Mitalee
%% @doc @todo Add description to calling.


-module(calling).

%% ====================================================================
%% API functions
%% ====================================================================
-export([listen/1]).



%% ====================================================================
%% Internal functions
%% ====================================================================

listen(Key) ->
	receive 
		%% Patter to check message from master
		{Sender, {SenderName,ReceiverList,PidMap}} ->
			timer:sleep(random:uniform(100)),
			%%Send intro message to each receiver
			lists:foreach(fun(Receiver) ->
                      timer:sleep(random:uniform(100)),
                      maps:get(Receiver, PidMap) ! {self(),{Sender,SenderName,Receiver,erlang:element(3, erlang:now()),intro}}
              end, ReceiverList),
			listen(Key);
		
		%%Pattern to chech Intro message
		{Sender, {MasterPid,SenderName,ReceiverName,MicroSecs,intro}} ->
			%%Send msg to master for printing
			MasterPid ! {ReceiverName ,"received intro message from",SenderName,MicroSecs},
			timer:sleep(random:uniform(100)),
			%%Send reply message to Sender
			Sender ! {MasterPid,ReceiverName,SenderName,MicroSecs,reply},
			listen(Key);

		%%Pattern to chech Reply message
		{MasterPid,SenderName,ReceiverName,MicroSecs,reply} ->
			MasterPid ! {ReceiverName ,"received reply message from",SenderName,MicroSecs},
			listen(Key)
	    after 5000 -> io:fwrite("\nProcess ~w has received no calls for 5 seconds, ending...\n",[Key])
	end.