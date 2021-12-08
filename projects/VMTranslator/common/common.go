package common

type CommandType int8

const (
	Arithmetic CommandType = iota
	Push
	Pop
	Label
	GoTo
	If
	Function
	Return
	Call
	Nil
)
