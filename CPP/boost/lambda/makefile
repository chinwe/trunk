target = example
CC = clang

$(target).o : $(target).cpp
	$(CC) $(target).cpp -o $(target).o -lstdc++

.PHONY : clean
clean:
	-rm $(target).o
