-- AppleScript 权威指南

tell application "Finder"
	get name of every disk
end tell

(* repeat 3 times
	display dialog "Howdy"
end repeat *)

set x to 3
if x = 3 then
	-- display dialog "x" buttons {"cancle", "ok"} 
end if

-- Chapter 7

set x to 5

set L to {1, 2, 3}
set LL to L
set end of L to 4
LL

set {x, y, z} to {1, 2, 3}
set x to x + 1
set x to (get z) + 1

set x to string
set x to (path to current user folder)
set X to {2, 3, 4}

set |is| to "ought"

-- Chapter 8 Script Object

Script s
	property x : 10
	display dialog x
	set x to 5
	display dialog x 
end Script
-- run s -- howdy
(*
tell s
	run --howdy
end tell
*)

-- tell s to run -- howdy

-- load script "file"
-- run script "script"
-- store script s