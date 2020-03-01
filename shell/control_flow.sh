#!/bin/bash

# case
day=1

case $day in
    "1")
        echo Monday
        ;;
    "2")
        echo Tuesday
        ;;
    *)
        echo Other
        ;;
esac

# for
for i in $@
do
 echo $i
done

for((i=1; i<=100; i++))
do
    sum=$[$sum + $i]
done
echo $sum

# while
sum=0
i=1
while [ $i -lt 10 ]
do
    sum=$[$sum + $i]
    i=$[$i + 1]
done
echo $sum
