package main

import (
	"fmt"
)

func main() {
	http.Handle("/", http.FileServer(http.Dir("./")))
	http.ListenAndServe(":8123", nil)
}
