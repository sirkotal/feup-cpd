#!/usr/bin/env bash

# Create a directory to store the data
rm -r data 2> /dev/null
mkdir data

# Compile both the C++ and Rust code
cargo build --profile data
g++ -O2 src/matrix_mul.cpp -o main -lpapi

# TODO: When added cache misses to the Rust code,
#       remove the "cpp" or "rust" argument from the functions

# Function to extract the data from the output file
# $1 - file to extract data from
# $2 - file to store the data
# $3 - "cpp" or "rust"
get_data () {
    # Time in seconds
    time=$(cat $1 | head -n1 | cut -d ' ' -f 2)

    if [ $3 = "cpp" ]; then
        # L1 Cache Misses
        l1_misses=$(cat $1 | tail -n2 | head -n1 | cut -d ' ' -f 3)

        # L2 Cache Misses
        l2_misses=$(cat $1 | tail -n1 | cut -d ' ' -f 3)


        echo "$time,$l1_misses,$l2_misses" >> $2
    elif [ $3 = "rust" ]; then
        echo "$time" >> $2
    fi
}

# Function to run the code and extract the data
# $1 - command to run the code
# $2 - file to store the data
# $3 - "cpp" or "rust"
run_code () {
    if [ $3 = "cpp" ]; then
        # Create a .csv file
        echo "time,l1_misses,l2_misses" > $2

        for i in {1..3}; do
            sh -c "$1" > temp.txt

            get_data "temp.txt" $2 "cpp"
        done
    elif [ $3 = "rust" ]; then
        # Create a .csv file
        echo "time" > $2

        for i in {1..3}; do
            sh -c "$1" > temp.txt

            get_data "temp.txt" $2 "rust"
        done
    fi
}

run_code "./main -t line -d 1024" "data/cpp_line_1024.csv" "cpp"

run_code "target/data/cpd -t line -d 1024" "data/rust_line_1024.csv" "rust"

rm temp.txt
rm main
