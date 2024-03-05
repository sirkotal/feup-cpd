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

The problem consisted in analyzing the performance of matrix multiplication algorithms - particularly the effect on processor performance in the memory hierarchy when accessing large amounts of data (and not necessarily the algorithm's result itself).

Three different algorithms were used in this project - the goal being to evaluate their respective performances in a single-core implementation. These algorithms were developed in both C++ and Rust; we gathered data from multiple attempts with different square matrix sizes. 

Since we were required to utilize C++, we decided to use Rust as the secondary programming language - its similarities to C++, alongside its superior memory safety and concurrency features, made it a rather interesting choice from our point of view.

## Algorithms Explanation