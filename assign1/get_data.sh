#!/usr/bin/env bash

# Create a directory to store the data
rm -r data 2> /dev/null
mkdir data

# Compile both the C++ and Rust code
cargo build --profile data
g++ -O2 src/matrix_mul.cpp -o main -lpapi -fopenmp

# TODO: When added cache misses to the Rust code,
#       remove the "cpp" or "rust" argument from the functions

# Function to extract the data from the output file
# $1 - dimension of the matrix
# $2 - file to extract data from
# $3 - file to store the data
# $4 - "cpp" or "rust"
# $5 - block size (optional)
get_data () {
    # Time in seconds
    time=$(cat $2 | head -n1 | cut -d ' ' -f 2)

    if [ $4 = "cpp" ]; then
        # L1 Cache Misses
        l1_misses=$(cat $2 | tail -n2 | head -n1 | cut -d ' ' -f 3)

        # L2 Cache Misses
        l2_misses=$(cat $2 | tail -n1 | cut -d ' ' -f 3)

        if [[ -n "$5" ]]; then
            echo "$1,$5,$time,$l1_misses,$l2_misses" >> $3
        else
            echo "$1,$time,$l1_misses,$l2_misses" >> $3
        fi
    elif [ $4 = "rust" ]; then
        if [[ -n "$5" ]]; then
            echo "$1,$5,$time" >> $3
        else
            echo "$1,$time" >> $3
        fi
    fi
}

# Function to run the code and extract the data
# $1 - command to run the code
# $2 - dimension of the matrix
# $3 - file to store the data
# $4 - "cpp" or "rust"
# $5 - block size (optional)
run_code () {
    if [ $4 = "cpp" ]; then
        for i in {1..3}; do
            sh -c "$1" > temp.txt
            get_data $2 "temp.txt" $3 "cpp" $5
        done
    elif [ $4 = "rust" ]; then
        for i in {1..3}; do
            sh -c "$1" > temp.txt
            get_data $2 "temp.txt" $3 "rust" $5
        done
    fi
}


# Create .csv files
echo "dimension,time,l1_misses,l2_misses" > data/cpp_default.csv
echo "dimension,time,l1_misses,l2_misses" > data/cpp_default_p1.csv
echo "dimension,time,l1_misses,l2_misses" > data/cpp_default_p2.csv
echo "dimension,time" > data/rust_default.csv

echo "dimension,time,l1_misses,l2_misses" > data/cpp_line.csv
echo "dimension,time,l1_misses,l2_misses" > data/cpp_line_p1.csv
echo "dimension,time,l1_misses,l2_misses" > data/cpp_line_p2.csv
echo "dimension,time" > data/rust_line.csv

echo "dimension,block_dimension,time,l1_misses,l2_misses" > data/cpp_block.csv
echo "dimension,block_dimension,time" > data/rust_block.csv

# Run with type "default"
for dimension in {600..3000..400}; do
    echo "Running type \`default\` with dimension $dimension/3000"

    run_code "./main -t default -d $dimension" $dimension "data/cpp_default.csv" "cpp"
    run_code "target/data/cpd -t default -d $dimension" $dimension "data/rust_default.csv" "rust"
done

# Run with type "line"
for dimension in {600..3000..400}; do
    echo "Running type \`line\` with dimension $dimension/3000"

    run_code "./main -t line -d $dimension" $dimension "data/cpp_line.csv" "cpp"
    run_code "target/data/cpd -t line -d $dimension" $dimension "data/rust_line.csv" "rust"
done

for dimension in {4096..10240..2048}; do
    echo "Running type \`line\` with dimension $dimension/10240"

    run_code "./main -t line -d $dimension" $dimension "data/cpp_line.csv" "cpp"
    run_code "target/data/cpd -t line -d $dimension" $dimension "data/rust_line.csv" "rust"
done

# Run with type "block"
for dimension in {4096..10240..2048}; do
    for ((block = 128; block <= 512; block *= 2)); do
        echo "Running type \`block\` with dimension $dimension/10240 and block size $block/512"

        run_code "./main -t block -b $block -d $dimension" $dimension "data/cpp_block.csv" "cpp" $block
        run_code "target/data/cpd -t block -b $block -d $dimension" $dimension "data/rust_block.csv" "rust" $block
    done
done

echo "Running parallel C++ code"

# Run with type "default"
for dimension in {600..3000..400}; do
    echo "Running type \`default\` with dimension $dimension/3000"

    run_code "./main -t default -d $dimension -p 1" $dimension "data/cpp_default_p1.csv" "cpp"
    run_code "./main -t default -d $dimension -p 2" $dimension "data/cpp_default_p2.csv" "cpp"
done

# Run with type "line"
for dimension in {600..3000..400}; do
    echo "Running type \`line\` with dimension $dimension/3000"

    run_code "./main -t line -d $dimension -p 1" $dimension "data/cpp_line_p1.csv" "cpp"
    run_code "./main -t line -d $dimension -p 2" $dimension "data/cpp_line_p2.csv" "cpp"
done

for dimension in {4096..10240..2048}; do
    echo "Running type \`line\` with dimension $dimension/10240"

    run_code "./main -t line -d $dimension -p 1" $dimension "data/cpp_line_p1.csv" "cpp"
    run_code "./main -t line -d $dimension -p 2" $dimension "data/cpp_line_p2.csv" "cpp"
done

rm temp.txt
rm main
