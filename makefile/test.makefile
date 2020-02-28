CC = gcc
CFLAG = -Wall -g

test: test.c
	$(CC) $(CFLAG) test.c -o test

.PHONY: clean
clean:
	rm -f *.o
	rm test
