package main

import (
	"bytes"
	"os"
	"os/exec"
	"strings"
	"testing"
)

// TestMainFunction tests the main function by running the program as a subprocess
func TestMainFunction(t *testing.T) {
	// Build the program first
	cmd := exec.Command("go", "build", "-o", "first_test_binary", "first.go")
	err := cmd.Run()
	if err != nil {
		t.Fatalf("Failed to build program: %v", err)
	}
	
	// Clean up the binary after the test
	defer os.Remove("first_test_binary")
	
	// Run the built program and capture output
	cmd = exec.Command("./first_test_binary")
	var out bytes.Buffer
	cmd.Stdout = &out
	
	err = cmd.Run()
	if err != nil {
		t.Fatalf("Failed to run program: %v", err)
	}
	
	// Check the output
	expectedOutput := "Hello, World.\n"
	actualOutput := out.String()
	
	if actualOutput != expectedOutput {
		t.Errorf("Expected output %q, got %q", expectedOutput, actualOutput)
	}
}

// TestMainFunctionExitCode tests that the program exits with code 0
func TestMainFunctionExitCode(t *testing.T) {
	// Build the program first
	cmd := exec.Command("go", "build", "-o", "first_test_binary", "first.go")
	err := cmd.Run()
	if err != nil {
		t.Fatalf("Failed to build program: %v", err)
	}
	
	// Clean up the binary after the test
	defer os.Remove("first_test_binary")
	
	// Run the program and check exit code
	cmd = exec.Command("./first_test_binary")
	err = cmd.Run()
	
	// Check that the program exited successfully (exit code 0)
	if err != nil {
		t.Errorf("Program should exit with code 0, but got error: %v", err)
	}
}

// TestProgramCompiles tests that the program compiles without errors
func TestProgramCompiles(t *testing.T) {
	cmd := exec.Command("go", "build", "first.go")
	var stderr bytes.Buffer
	cmd.Stderr = &stderr
	
	err := cmd.Run()
	if err != nil {
		t.Errorf("Program failed to compile: %v\nStderr: %s", err, stderr.String())
	}
	
	// Clean up the binary
	os.Remove("first")
}

// TestProgramRunsWithoutPanic tests that the program runs without panicking
func TestProgramRunsWithoutPanic(t *testing.T) {
	// This test runs the program using 'go run' and ensures it doesn't panic
	cmd := exec.Command("go", "run", "first.go")
	var out, errOut bytes.Buffer
	cmd.Stdout = &out
	cmd.Stderr = &errOut
	
	err := cmd.Run()
	if err != nil {
		t.Errorf("Program panicked or failed to run: %v\nStderr: %s", err, errOut.String())
	}
	
	// Check that stderr is empty (no panic messages)
	if errOut.String() != "" {
		t.Errorf("Program produced error output: %s", errOut.String())
	}
	
	// Check that we got the expected output
	if !strings.Contains(out.String(), "Hello, World.") {
		t.Errorf("Expected output to contain 'Hello, World.', got: %s", out.String())
	}
}
