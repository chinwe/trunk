target = SaftySprint
CC = clang

$(target) : $(target).o
	$(CC) -o $(target) $(target).o -lstdc++

$(target).o : $(target).cpp
	$(CC) -c $(target).cpp -std=c++11

.PHONY : clean
clean:
	-rm $(target) $(target).o
