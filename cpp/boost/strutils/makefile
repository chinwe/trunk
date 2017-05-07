target = example
CC = clang

$(target).o : $(target).cpp
	$(CC) $(target).cpp -o $(target).o -std=c++11 -lstdc++ -I /usr/local/include

.PHONY : clean
clean:
	-rm $(target).o
