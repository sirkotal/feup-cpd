fn OnMult(m_ar: i64, m_br: i64) {
    let mut pha = vec![f64; m_ar * m_ar];
    let mut phb = vec![f64; m_br * m_br];
    let mut phc = vec![f64; m_ar * m_br];

    for i in 0..m_ar {
        for j in 0..m_ar {
            phb[i * m_ar + j] = (1.0) as f64;
        }
    }

    for i in 0..m_br {
        for j in 0..m_br {
            phb[i * m_br + j] = (i + 1) as f64;
        }
    }
}