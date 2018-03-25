package main

import (
	"fmt"
	"math"
)

// Circle struct
type Circle struct {
	x, y, r float64
}

// Circle area method
func (c *Circle) area() float64 {
	return math.Pi * c.r * c.r
}

// Rectangle struct
type Rectangle struct {
	x, y, w, h float64
}

// Rectangle area method
func (r *Rectangle) area() float64 {
	return r.w * r.h
}

// Shape Interface
type Shape interface {
	area() float64
}

func totalArea(shapes ...Shape) float64 {
	var area float64
	for _, s := range shapes {
		area += s.area()
	}
	return area
}

func main() {

	c := Circle{0, 0, 5}

	fmt.Println("circle : ", c.x, c.y, c.r)
	fmt.Println("circle area : ", c.area())

	r := Rectangle{0, 0, 5, 5}
	fmt.Println("totalArea : ", totalArea(&c, &r))
}
