fn OnMult(m_ar: usize, m_br: usize) {
    let mut pha = vec![1.0 as f64; m_ar * m_ar];
    let mut phb = vec![0.0 as f64; m_ar * m_ar];
    let mut phc = vec![0.0 as f64; m_ar * m_ar];

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

    let end = std::time::Instant::now();
    let duration = end - start;

    println!("Time: {:.3} seconds", duration.as_secs_f64());

    println!("Result matrix: ");

    for j in 0..std::cmp::min(10, m_br) {
        println!("{} ", phc[j]);
    }

    println!();
}

fn OnMultLine(m_ar: usize, m_br: usize) {
    let mut pha = vec![1.0 as f64; m_ar * m_ar];
    let mut phb = vec![0.0 as f64; m_ar * m_ar];
    let mut phc = vec![0.0 as f64; m_ar * m_ar];

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

    let end = std::time::Instant::now();
    let duration = end - start;

    println!("Time: {:.3} seconds", duration.as_secs_f64());

    println!("Result matrix: ");

    for j in 0..std::cmp::min(10, m_br) {
        println!("{} ", phc[j]);
    }

    println!();
}