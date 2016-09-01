target = example
CC = clang

$(target).o : $(target).cpp
	$(CC) $(target).cpp -o $(target).o -lstdc++ -lboost_regex 

.PHONY : clean
clean:
	-rm $(target).o
