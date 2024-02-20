# CPD Practical Work - Performance Evaluation of Single-Core and Multi-Core Matrix Multiplication Algorithm Implementations

## Compilation & Running

- C++
```sh
g++ -O2 src/matrix_mul.cpp -o main -lpapi
./main -h
```

- Rust
```sh
cargo build --profile data
target/data/cpd -h
```

## Introduction

The goal of this project was to implement matrix multiplication algorithms, compare them, document them and analyzing the performance results of the code for each implementation - 3 single-core and 2 multi-core. This allowed us to study the toll the access/usage of large amounts of data in memory took on the CPU's performance.

We decided to compare the different algorithms' performance in C++ and Rust.

## Problem Description

