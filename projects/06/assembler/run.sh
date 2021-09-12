go run entry/main.go ../add/Add.asm
go run entry/main.go ../max/Max.asm
go run entry/main.go ../max/MaxL.asm
go run entry/main.go ../pong/Pong.asm
go run entry/main.go ../pong/PongL.asm
go run entry/main.go ../rect/Rect.asm
go run entry/main.go ../rect/RectL.asm

git diff ../add/Add.hack ../add/Add2.hack
git diff ../max/Max.hack ../max/Max2.hack
git diff ../max/MaxL.hack ../max/MaxL2.hack
git diff ../pong/Pong.hack ../pong/Pong2.hack
git diff ../pong/PongL.hack ../pong/PongL2.hack
git diff ../rect/Rect.hack ../rect/Rect2.hack
git diff ../rect/RectL.hack ../rect/RectL2.hack



