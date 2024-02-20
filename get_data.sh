#!/usr/bin/env bash

mkdir temp

# Compile both the C++ and Rust code
cargo build --profile data
g++ -O2 src/matrix_mul.cpp -o main -lpapi

# Get C++ Output

##### TEST

./main -t line -d 1024 > temp/cpp_line_1024.txt

# Time in seconds
cat temp/cpp_line_1024.txt | head -n1 | cut -d ' ' -f 2

# L1 Cache Misses
cat temp/cpp_line_1024.txt | tail -n2 | head -n1 | cut -d ' ' -f 3

# L2 Cache Misses
cat temp/cpp_line_1024.txt | tail -n1 | cut -d ' ' -f 3

#####

rm -r temp
rm main
