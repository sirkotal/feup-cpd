fn OnMult(m_ar: usize, m_br: usize) {
    let mut pha = vec![1.0; m_ar * m_ar];
    let mut phb = vec![0.0; m_ar * m_ar];
    let mut phc = vec![0.0; m_ar * m_ar];

    for i in 0..m_br {
        for j in 0..m_br {
            phb[i * m_br + j] = (i + 1) as f64;
        }
    }
}