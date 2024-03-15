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

### Simple Matrix Multiplication

A simple matrix multiplication algorithm implementation was provided to us in C++ - we later decided to implement the second version of it in Rust.
The algorithm itself consists in obtaining the result in the *i-th* row and *j-th* column of matrix C via the product of the elements in the *i-th* row of matrix A and the *j-th* column of matrix B.

If we consider a square matrix with *n* lines and columns, the time complexity of this algorithm should be O(n<sup>3</sup>).

```cpp
for(i = 0; i < m_ar; i++) {	
    for(j = 0; j < m_br; j++) {	
        temp = 0;
		for(k = 0; k < m_ar; k++) {	
			temp += pha[i*m_ar+k] * phb[k*m_br+j];
		}
		phc[i*m_ar+j]=temp;
	}
}
```

### Line Matrix Multiplication

This algorithm, having been implemented in both C++ and Rust, uses the one mentioned earlier as a basis, although with a key difference: the order of the second and third ```for``` loops is switched. This results in overall better performance (less execution time and cache misses).

This algorithm obtains the result in the *i-th* row and *j-th* column of matrix C by calculating the product of the elements in the *i-th* row of matrix A and the *j-th* column of matrix B; however, the line-by-line version differs from the the simple version because it directly accumulates the result in the specified matrix C position.

If we consider a square matrix with *n* lines and columns, the time complexity of this algorithm should be similar to the time complexity of the simple algorithm - O(n<sup>3</sup>).

```cpp
for (i = 0; i < m_ar; i++) {
    for (k = 0; k < m_ar; k++) {
        for (j = 0; j < m_br; j++) {
            phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
        }
    }
}
```

### Block Matrix Multiplication

Unlike the previous algorithms, the block matrix multiplication algorithm starts by dividing both of the matrices that are meant to be multiplied into blocks of size ```bkSize```.
The blocks themselves are treated as elements of each matrix - they are calculated individually. This allows for an increase in the overall algorithm performance.

Just like the previous two algorithms, considering square matrices with *n* lines and columns, the time complexity of this algorithm should be O(n<sup>3</sup>).

```cpp
for (int b1 = 0; b1 < m_ar; b1 += bkSize) {
    for (int b3 = 0; b3 < m_ar; b3 += bkSize) {
        for (int b2 = 0; b2 < m_br; b2 += bkSize) {
            for (int i = b1; i < b1 + bkSize; i++) {
                for (int k = b3; k < b3 + bkSize; k++) {
                    for (int j = b2; j < b2 + bkSize; j++) {
                            phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
                    }
                }
            }
        }
    }
}
```

## Performance Metrics

## Results and Analysis

## Conclusion