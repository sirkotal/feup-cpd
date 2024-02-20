use clap::Parser;

fn on_mult(m_ar: usize, m_br: usize) {
    let pha = vec![1.0; m_ar * m_ar];
    let mut phb = vec![0.0; m_ar * m_ar];
    let mut phc = vec![0.0; m_ar * m_ar];

    for i in 0..m_br {
        for j in 0..m_br {
            phb[i * m_br + j] = (i + 1) as f64;
        }
    }

    let start = std::time::Instant::now();

    for i in 0..m_ar {
        for j in 0..m_br {
            let mut temp = 0.0;
            for k in 0..m_ar {
                temp += pha[i * m_ar + k] * phb[k * m_br + j];
            }

            phc[i * m_ar + j] = temp;
        }
    }

    let end = start.elapsed();
    println!("Time: {:?}", end);

    println!("Result Matrix:");
    for j in 0..std::cmp::min(m_br, 10) {
        print!("{:.2} ", phc[j]);
    }
    println!();
}

fn on_mult_line(m_ar: usize, m_br: usize) {
    let pha = vec![1.0; m_ar * m_ar];
    let mut phb = vec![0.0; m_ar * m_ar];
    let mut phc = vec![0.0; m_ar * m_ar];

    for i in 0..m_br {
        for j in 0..m_br {
            phb[i * m_br + j] = (i + 1) as f64;
        }
    }

    let start = std::time::Instant::now();

    for i in 0..m_ar {
        for k in 0..m_ar {
            for j in 0..m_br {
                phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
            }
        }
    }

    let end = start.elapsed();
    println!("Time: {:?}", end);

    println!("Result Matrix:");
    for j in 0..std::cmp::min(m_br, 10) {
        print!("{:.2} ", phc[j]);
    }
    println!();
}

fn on_mult_block(m_ar: usize, m_br: usize, block_size: usize) {
    let pha = vec![1.0; m_ar * m_ar];
    let mut phb = vec![0.0; m_ar * m_ar];
    let mut phc = vec![0.0; m_ar * m_ar];

    for i in 0..m_br {
        for j in 0..m_br {
            phb[i * m_br + j] = (i + 1) as f64;
        }
    }

    let start = std::time::Instant::now();

    for b1 in (0..m_ar).step_by(block_size) {
        for b3 in (0..m_ar).step_by(block_size) {
            for b2 in (0..m_br).step_by(block_size) {
                for i in b1..(b1 + block_size) {
                    for k in b3..(b3 + block_size) {
                        for j in b2..(b2 + block_size) {
                            phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
                        }
                    }
                }
            }
        }
    }

    let end = start.elapsed();
    println!("Time: {:?}", end);

    println!("Result Matrix:");
    for j in 0..std::cmp::min(m_br, 10) {
        print!("{:.2} ", phc[j]);
    }
    println!();
}

#[derive(clap::ValueEnum, Clone, Default)]
enum Type {
    #[default]
    Default,
    Line,
    Block,
}

#[derive(Parser)]
struct Args {
    #[clap(short, long)]
    type_: Type,

    #[clap(short, long, default_value = "1024")]
    dimension: usize,

    #[clap(short, long)]
    block_size: Option<usize>,
}

fn main() -> Result<(), &'static str> {
    let args: Args = Args::parse();

    match args.type_ {
        Type::Default => Ok(on_mult(args.dimension, args.dimension)),
        Type::Line => Ok(on_mult_line(args.dimension, args.dimension)),
        Type::Block => {
            if let Some(block_size) = args.block_size {
                Ok(on_mult_block(args.dimension, args.dimension, block_size))
            } else {
                return Err("Block size is required for block multiplication");
            }
        }
    }
}
