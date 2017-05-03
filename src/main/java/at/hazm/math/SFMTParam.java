package at.hazm.math;

enum SFMTParam {
    P607(607, 2, 15, 3, 13, 3,
            0xfdff37ff, 0xef7f3f7d, 0xff777b7d, 0x7ff7fb2f,
            0x00000001, 0x00000000, 0x00000000, 0x5986f054,
            "SFMT-607:2-15-3-13-3:fdff37ff-ef7f3f7d-ff777b7d-7ff7fb2f"),
    P19937(19937, 122, 18, 1, 11, 1,
            0xdfffffef, 0xddfecb7f, 0xbffaffff, 0xbffffff6,
            0x00000001, 0x00000000, 0x00000000, 0x13c9e684,
            "SFMT-19937:122-18-1-11-1:dfffffef-ddfecb7f-bffaffff-bffffff6");

    /**
     * Mersenne Exponent. The period of the sequence is a multiple of 2^MEXP-1.
     */
    public final int SFMT_MEXP;

    /**
     * SFMT generator has an internal state array of 128-bit integers, and N is its size.
     */
    public final int SFMT_N;

    /**
     * N32 is the size of internal state array when regarded as an array of 32-bit integers.
     */
    public final int SFMT_N32;

    /**
     * N64 is the size of internal state array when regarded as an array of 64-bit integers.
     */
    public final int SFMT_N64;

    public final int SFMT_POS1;
    public final int SFMT_SL1;
    public final int SFMT_SL2;
    public final int SFMT_SR1;
    public final int SFMT_SR2;
    public final int SFMT_MSK1;
    public final int SFMT_MSK2;
    public final int SFMT_MSK3;
    public final int SFMT_MSK4;
    public final int SFMT_PARITY1;
    public final int SFMT_PARITY2;
    public final int SFMT_PARITY3;
    public final int SFMT_PARITY4;
    public final String SFMT_IDSTR;

    SFMTParam(int SFMT_MEXP, int SFMT_POS1, int SFMT_SL1, int SFMT_SL2, int SFMT_SR1, int SFMT_SR2,
              int SFMT_MSK1, int SFMT_MSK2, int SFMT_MSK3, int SFMT_MSK4,
              int SFMT_PARITY1, int SFMT_PARITY2, int SFMT_PARITY3, int SFMT_PARITY4,
              String SFMT_IDSTR) {
        this.SFMT_MEXP = SFMT_MEXP;
        this.SFMT_N = SFMT_MEXP / 128 + 1;
        this.SFMT_N32 = SFMT_N * 4;
        this.SFMT_N64 = SFMT_N * 2;
        this.SFMT_POS1 = SFMT_POS1;
        this.SFMT_SL1 = SFMT_SL1;
        this.SFMT_SL2 = SFMT_SL2;
        this.SFMT_SR1 = SFMT_SR1;
        this.SFMT_SR2 = SFMT_SR2;
        this.SFMT_MSK1 = SFMT_MSK1;
        this.SFMT_MSK2 = SFMT_MSK2;
        this.SFMT_MSK3 = SFMT_MSK3;
        this.SFMT_MSK4 = SFMT_MSK4;
        this.SFMT_PARITY1 = SFMT_PARITY1;
        this.SFMT_PARITY2 = SFMT_PARITY2;
        this.SFMT_PARITY3 = SFMT_PARITY3;
        this.SFMT_PARITY4 = SFMT_PARITY4;
        this.SFMT_IDSTR = SFMT_IDSTR;
    }

}

