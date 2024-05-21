import pandas as pd
import os
import matplotlib.pyplot as plt

os.makedirs('../doc/graphs', exist_ok=True)

cpp_default_data = pd.read_csv('../doc/cpp_default.csv')
cpp_line_data = pd.read_csv('../doc/cpp_line.csv')
cpp_line_data = cpp_line_data[cpp_line_data['dimension'].isin(cpp_default_data['dimension'])]

plt.figure()
plt.plot(cpp_default_data['dimension'], cpp_default_data['time'], label='Basic Algorithm')
plt.plot(cpp_line_data['dimension'], cpp_line_data['time'], label='Line Multiplication Algorithm')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Time (s)')
plt.title('Basic vs Line Multiplication Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_default_vs_line.png')
plt.close()

cpp_line_data = pd.read_csv('../doc/cpp_line.csv')
cpp_block_data = pd.read_csv('../doc/cpp_block.csv')
cpp_line_data = cpp_line_data[cpp_line_data['dimension'].isin(cpp_block_data['dimension'])]
cpp_block_data = cpp_block_data[cpp_block_data['block_dimension'] == 256]

plt.figure()
plt.plot(cpp_block_data['dimension'], cpp_block_data['time'], label='Block Multiplication Algorithm (Block Size = 256)')
plt.plot(cpp_line_data['dimension'], cpp_line_data['time'], label='Line Multiplication Algorithm')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Time (s)')
plt.title('Block vs Line Multiplication Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_block_vs_line.png')
plt.close()

cpp_block_data = pd.read_csv('../doc/cpp_block.csv')

plt.figure()
for block_dim in [128, 256, 512]:
    data = cpp_block_data[cpp_block_data['block_dimension'] == block_dim]
    plt.plot(data['dimension'], data['time'], label=f'Block Size = {block_dim}')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Time (s)')
plt.title('Block Multiplication Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_block.png')
plt.close()

cpp_block_data['flops'] = (2 * cpp_block_data['dimension']**3 / cpp_block_data['time']) * 1e-9

plt.figure()
for block_dim in [128, 256, 512]:
    data = cpp_block_data[cpp_block_data['block_dimension'] == block_dim]
    plt.plot(data['dimension'], data['flops'], label=f'Block Size = {block_dim}')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('GFLOPS')
plt.title('Block Multiplication Algorithm Efficiency - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_block_efficiency.png')
plt.close()

cpp_default_data = pd.read_csv('../doc/cpp_default.csv')
rust_default_data = pd.read_csv('../doc/rust_default.csv')

plt.figure()
plt.plot(cpp_default_data['dimension'], cpp_default_data['time'], label='C++')
plt.plot(rust_default_data['dimension'], rust_default_data['time'], label='Rust')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Time (s)')
plt.title('Basic Algorithm - C++ vs Rust')

# plt.show()
plt.savefig('../doc/graphs/cpp_vs_rust_default.png')
plt.close()

cpp_line_data = pd.read_csv('../doc/cpp_line.csv')
rust_line_data = pd.read_csv('../doc/rust_line.csv')

plt.figure()
plt.plot(cpp_line_data['dimension'], cpp_line_data['time'], label='C++')
plt.plot(rust_line_data['dimension'], rust_line_data['time'], label='Rust')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Time (s)')
plt.title('Line Multiplication Algorithm - C++ vs Rust')

# plt.show()
plt.savefig('../doc/graphs/cpp_vs_rust_line.png')
plt.close()

cpp_block_data = pd.read_csv('../doc/cpp_block.csv')
rust_block_data = pd.read_csv('../doc/rust_block.csv')

plt.figure()
data = cpp_block_data[cpp_block_data['block_dimension'] == 256]
plt.plot(data['dimension'], data['time'], label=f'C++ Block Size = 256')

data = rust_block_data[rust_block_data['block_dimension'] == 256]
plt.plot(data['dimension'], data['time'], label=f'Rust Block Size = 256')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Time (s)')
plt.title('Block Multiplication Algorithm - C++ vs Rust')

# plt.show()
plt.savefig('../doc/graphs/cpp_vs_rust_block.png')
plt.close()

cpp_default_data = pd.read_csv('../doc/cpp_default.csv')
cpp_line_data = pd.read_csv('../doc/cpp_line.csv')
cpp_line_data = cpp_line_data[cpp_line_data['dimension'].isin(cpp_default_data['dimension'])]

plt.figure()
plt.plot(cpp_default_data['dimension'], cpp_default_data['l1_misses'], label='Basic Algorithm L1 Misses', color='C0')
plt.plot(cpp_default_data['dimension'], cpp_default_data['l2_misses'], label='Basic Algorithm L2 Misses', linestyle='dashed', color='C0')
plt.plot(cpp_line_data['dimension'], cpp_line_data['l1_misses'], label='Line Multiplication Algorithm L1 Misses', color='C1')
plt.plot(cpp_line_data['dimension'], cpp_line_data['l2_misses'], label='Line Multiplication Algorithm L2 Misses', linestyle='dashed', color='C1')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Data Cache Misses')
plt.title('L1 and L2 Misses - Basic vs Line Multiplication Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_default_vs_line_misses.png')
plt.close()

cpp_line_data = pd.read_csv('../doc/cpp_line.csv')
cpp_block_data = pd.read_csv('../doc/cpp_block.csv')
cpp_line_data = cpp_line_data[cpp_line_data['dimension'].isin(cpp_block_data['dimension'])]
cpp_block_data = cpp_block_data[cpp_block_data['block_dimension'] == 256]

plt.figure()
plt.plot(cpp_block_data['dimension'], cpp_block_data['l1_misses'], label='Block Multiplication Algorithm L1 Misses (Block Size = 256)', color='C0')
plt.plot(cpp_block_data['dimension'], cpp_block_data['l2_misses'], label='Block Multiplication Algorithm L2 Misses (Block Size = 256)', linestyle='dashed', color='C0')
plt.plot(cpp_line_data['dimension'], cpp_line_data['l1_misses'], label='Line Multiplication Algorithm L1 Misses', color='C1')
plt.plot(cpp_line_data['dimension'], cpp_line_data['l2_misses'], label='Line Multiplication Algorithm L2 Misses', linestyle='dashed', color='C1')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Data Cache Misses')
plt.title('L1 and L2 Misses - Block vs Line Multiplication Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_block_vs_line_misses.png')
plt.close()

cpp_default_data = pd.read_csv('../doc/cpp_default.csv')
rust_default_data = pd.read_csv('../doc/rust_default.csv')

cpp_default_data['flops'] = (2 * cpp_default_data['dimension']**3 / cpp_default_data['time']) * 1e-9
rust_default_data['flops'] = (2 * rust_default_data['dimension']**3 / rust_default_data['time']) * 1e-9

plt.figure()
plt.plot(cpp_default_data['dimension'], cpp_default_data['flops'], label='C++')
plt.plot(rust_default_data['dimension'], rust_default_data['flops'], label='Rust')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('GFLOPS')
plt.title('Efficiency - Basic Algorithm - C++ vs Rust')

# plt.show()
plt.savefig('../doc/graphs/cpp_vs_rust_default_efficiency.png')
plt.close()

cpp_line_data = pd.read_csv('../doc/cpp_line.csv')
rust_line_data = pd.read_csv('../doc/rust_line.csv')

cpp_line_data['flops'] = (2 * cpp_line_data['dimension']**3 / cpp_line_data['time']) * 1e-9
rust_line_data['flops'] = (2 * rust_line_data['dimension']**3 / rust_line_data['time']) * 1e-9

plt.figure()
plt.plot(cpp_line_data['dimension'], cpp_line_data['flops'], label='C++')
plt.plot(rust_line_data['dimension'], rust_line_data['flops'], label='Rust')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('GFLOPS')
plt.title('Efficiency - Line Multiplication Algorithm - C++ vs Rust')

# plt.show()
plt.savefig('../doc/graphs/cpp_vs_rust_line_efficiency.png')
plt.close()

cpp_block_data = pd.read_csv('../doc/cpp_block.csv')
rust_block_data = pd.read_csv('../doc/rust_block.csv')

cpp_block_data['flops'] = (2 * cpp_block_data['dimension']**3 / cpp_block_data['time']) * 1e-9
rust_block_data['flops'] = (2 * rust_block_data['dimension']**3 / rust_block_data['time']) * 1e-9

plt.figure()
data = cpp_block_data[cpp_block_data['block_dimension'] == 128]
plt.plot(data['dimension'], data['flops'], label=f'C++ Block Size = 128', color='C0')

data = cpp_block_data[cpp_block_data['block_dimension'] == 256]
plt.plot(data['dimension'], data['flops'], label=f'C++ Block Size = 256', color='C0', linestyle='dashed')

data = cpp_block_data[cpp_block_data['block_dimension'] == 512]
plt.plot(data['dimension'], data['flops'], label=f'C++ Block Size = 512', color='C0', linestyle='dotted')

data = rust_block_data[rust_block_data['block_dimension'] == 128]
plt.plot(data['dimension'], data['flops'], label=f'Rust Block Size = 128', color='C1')

data = rust_block_data[rust_block_data['block_dimension'] == 256]
plt.plot(data['dimension'], data['flops'], label=f'Rust Block Size = 256', color='C1', linestyle='dashed')

data = rust_block_data[rust_block_data['block_dimension'] == 512]
plt.plot(data['dimension'], data['flops'], label=f'Rust Block Size = 512', color='C1', linestyle='dotted')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('GFLOPS')
plt.title('Efficiency - Block Multiplication Algorithm - C++ vs Rust')

# plt.show()
plt.savefig('../doc/graphs/cpp_vs_rust_block_efficiency.png')
plt.close()

cpp_default_data = pd.read_csv('../doc/cpp_default.csv')
cpp_default_p1_data = pd.read_csv('../doc/cpp_default_p1.csv')
cpp_default_p2_data = pd.read_csv('../doc/cpp_default_p2.csv')

plt.figure()
plt.plot(cpp_default_data['dimension'], cpp_default_data['time'], label='Basic Algorithm')
plt.plot(cpp_default_p1_data['dimension'], cpp_default_p1_data['time'], label='Basic Algorithm (P1)')
plt.plot(cpp_default_p2_data['dimension'], cpp_default_p2_data['time'], label='Basic Algorithm (P2)')
plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Time (s)')
plt.title('Basic Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_default_p1_p2.png')
plt.close()

plt.figure()
plt.plot(cpp_default_data['dimension'], cpp_default_data['l1_misses'], label='Basic Algorithm L1 Misses', color='C0')
plt.plot(cpp_default_data['dimension'], cpp_default_data['l2_misses'], label='Basic Algorithm L2 Misses', linestyle='dashed', color='C0')
plt.plot(cpp_default_p1_data['dimension'], cpp_default_p1_data['l1_misses'], label='Basic Algorithm (P1) L1 Misses', color='C1')
plt.plot(cpp_default_p1_data['dimension'], cpp_default_p1_data['l2_misses'], label='Basic Algorithm (P1) L2 Misses', linestyle='dashed', color='C1')
plt.plot(cpp_default_p2_data['dimension'], cpp_default_p2_data['l1_misses'], label='Basic Algorithm (P2) L1 Misses', color='C2')
plt.plot(cpp_default_p2_data['dimension'], cpp_default_p2_data['l2_misses'], label='Basic Algorithm (P2) L2 Misses', linestyle='dashed', color='C2')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Data Cache Misses')
plt.title('L1 and L2 Misses - Basic Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_default_p1_p2_misses.png')
plt.close()

# same as above but with flops

cpp_default_data['flops'] = (2 * cpp_default_data['dimension']**3 / cpp_default_data['time']) * 1e-9
cpp_default_p1_data['flops'] = (2 * cpp_default_p1_data['dimension']**3 / cpp_default_p1_data['time']) * 1e-9
cpp_default_p2_data['flops'] = (2 * cpp_default_p2_data['dimension']**3 / cpp_default_p2_data['time']) * 1e-9

plt.figure()
plt.plot(cpp_default_data['dimension'], cpp_default_data['flops'], label='Basic Algorithm')
plt.plot(cpp_default_p1_data['dimension'], cpp_default_p1_data['flops'], label='Basic Algorithm (P1)')
plt.plot(cpp_default_p2_data['dimension'], cpp_default_p2_data['flops'], label='Basic Algorithm (P2)')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('GFLOPS')
plt.title('Efficiency - Basic Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_default_p1_p2_efficiency.png')
plt.close()

cpp_line_data = pd.read_csv('../doc/cpp_line.csv')
cpp_line_p1_data = pd.read_csv('../doc/cpp_line_p1.csv')
cpp_line_p2_data = pd.read_csv('../doc/cpp_line_p2.csv')

plt.figure()
plt.plot(cpp_line_data['dimension'], cpp_line_data['time'], label='Line Multiplication Algorithm')
plt.plot(cpp_line_p1_data['dimension'], cpp_line_p1_data['time'], label='Line Multiplication Algorithm (P1)')
plt.plot(cpp_line_p2_data['dimension'], cpp_line_p2_data['time'], label='Line Multiplication Algorithm (P2)')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Time (s)')
plt.title('Line Multiplication Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_line_p1_p2.png')
plt.close()

plt.figure()
plt.plot(cpp_line_data['dimension'], cpp_line_data['l1_misses'], label='Line Multiplication Algorithm L1 Misses', color='C0')
plt.plot(cpp_line_data['dimension'], cpp_line_data['l2_misses'], label='Line Multiplication Algorithm L2 Misses', linestyle='dashed', color='C0')
plt.plot(cpp_line_p1_data['dimension'], cpp_line_p1_data['l1_misses'], label='Line Multiplication Algorithm (P1) L1 Misses', color='C1')
plt.plot(cpp_line_p1_data['dimension'], cpp_line_p1_data['l2_misses'], label='Line Multiplication Algorithm (P1) L2 Misses', linestyle='dashed', color='C1')
plt.plot(cpp_line_p2_data['dimension'], cpp_line_p2_data['l1_misses'], label='Line Multiplication Algorithm (P2) L1 Misses', color='C2')
plt.plot(cpp_line_p2_data['dimension'], cpp_line_p2_data['l2_misses'], label='Line Multiplication Algorithm (P2) L2 Misses', linestyle='dashed', color='C2')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Data Cache Misses')
plt.title('L1 and L2 Misses - Line Multiplication Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_line_p1_p2_misses.png')
plt.close()

cpp_line_data['flops'] = (2 * cpp_line_data['dimension']**3 / cpp_line_data['time']) * 1e-9
cpp_line_p1_data['flops'] = (2 * cpp_line_p1_data['dimension']**3 / cpp_line_p1_data['time']) * 1e-9
cpp_line_p2_data['flops'] = (2 * cpp_line_p2_data['dimension']**3 / cpp_line_p2_data['time']) * 1e-9

plt.figure()
plt.plot(cpp_line_data['dimension'], cpp_line_data['flops'], label='Line Multiplication Algorithm')
plt.plot(cpp_line_p1_data['dimension'], cpp_line_p1_data['flops'], label='Line Multiplication Algorithm (P1)')
plt.plot(cpp_line_p2_data['dimension'], cpp_line_p2_data['flops'], label='Line Multiplication Algorithm (P2)')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('GFLOPS')
plt.title('Efficiency - Line Multiplication Algorithm - C++')

# plt.show()
plt.savefig('../doc/graphs/cpp_line_p1_p2_efficiency.png')
plt.close()

exit()

### SPEEDUP

# Read the CSV files
cpp_line_data = pd.read_csv('../doc/cpp_line.csv')
cpp_line_p1_data = pd.read_csv('../doc/cpp_line_p1.csv')
cpp_line_p2_data = pd.read_csv('../doc/cpp_line_p2.csv')
cpp_line_p1_data['dimension'] = cpp_line_p1_data['dimension'].apply(lambda x: f'{x}x{x}')
cpp_line_p2_data['dimension'] = cpp_line_p2_data['dimension'].apply(lambda x: f'{x}x{x}')

#cpp_line_data = cpp_line_data.iloc[:21]
#cpp_line_p1_data = cpp_line_p1_data.iloc[:21]
#cpp_line_p2_data = cpp_line_p2_data.iloc[:21]
speedup_p1 = cpp_line_data['time']/cpp_line_p1_data['time']
speedup_p2 = cpp_line_data['time']/cpp_line_p2_data['time']

plt.figure()

plt.plot(cpp_line_p1_data['dimension'], speedup_p1, label='P1')

plt.plot(cpp_line_p2_data['dimension'], speedup_p2, label='P2')

plt.legend()

plt.xlabel('Matrix Dimension')
plt.ylabel('Speedup')
plt.title('Speedup - P1 vs P2')
plt.xticks(rotation=45)

plt.savefig('../doc/graphs/speedup.png', bbox_inches='tight')
plt.show()

### EFFICIENCY

# Read the CSV files
cpp_line_data = pd.read_csv('../doc/cpp_line.csv')
cpp_line_p1_data = pd.read_csv('../doc/cpp_line_p1.csv')
cpp_line_p2_data = pd.read_csv('../doc/cpp_line_p2.csv')
cpp_line_p1_data['dimension'] = cpp_line_p1_data['dimension'].apply(lambda x: f'{x}x{x}')
cpp_line_p2_data['dimension'] = cpp_line_p2_data['dimension'].apply(lambda x: f'{x}x{x}')

#cpp_line_data = cpp_line_data.iloc[:21]
#cpp_line_p1_data = cpp_line_p1_data.iloc[:21]
#cpp_line_p2_data = cpp_line_p2_data.iloc[:21]

n_cores = 12

plt.figure()

efficiency_p1 = speedup_p1/n_cores
efficiency_p2 = speedup_p2/n_cores

print(efficiency_p1.mean())
print(efficiency_p2.mean())

plt.plot(cpp_line_p1_data['dimension'], efficiency_p1, label='P1')
plt.plot(cpp_line_p2_data['dimension'], efficiency_p2, label='P2')

plt.legend()
plt.xlabel('Matrix Dimension')
plt.ylabel('Efficiency')
plt.title('Efficiency - P1 vs P2')
plt.xticks(rotation=45)

plt.savefig('../doc/graphs/efficiency.png', bbox_inches='tight')
plt.show()
