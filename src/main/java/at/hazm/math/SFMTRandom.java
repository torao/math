package at.hazm.math;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class is a Java implementation of pseudo-random number generator using <b>SFMT</b> (SIMD-oriented Fast Mersenne
 * Twister).
 * <p>SFMT is a Linear Feedbacked Shift Register (LFSR) pseudo-random number generator and improved algorithm of
 * conventional MT (Mersenne Twister) in speed and equidistributions. Like MT, the SFMT supports extremely long periods
 * from 2<sup>607</sup>-1 to 2<sup>216091</sup>-1.</p>
 * <p>NOTE: This implementation doesn't support the native SIMD features such as SEE2 because, same as Standard C
 * version of original source, this is written pure Java.</p>
 * <p>NOTE: Thread unsafe.</p>
 * <p>
 * The original SFMT sources are written in C by Makoto Matsumoto and Takuji Nishimura in 2007 and their license is here:
 * </p>
 * <blockquote>
 * <p>Copyright (c) 2006,2007 Mutsuo Saito, Makoto Matsumoto and Hiroshima University. Copyright (c) 2012 Mutsuo Saito,
 * Makoto Matsumoto, Hiroshima University and The University of Tokyo. All rights reserved.</p>
 * <p>Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:</p>
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the names of Hiroshima University, The University of Tokyo nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </p>
 * <a href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/SFMT/LICENSE.txt">http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/SFMT/LICENSE.txt</a>
 * </blockquote>
 * derived from 1.5.1
 *
 * @author Takami Torao
 * @see <a href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/SFMT/">SFMT</a>
 */
public strictfp class SFMTRandom {

    private final SFMTParam param;

    /**
     * the 128-bit internal state array
     */
    private final W128T[] state;

    /**
     * index counter to the 32-bit internal state array
     */
    private int idx = 0;

    /**
     * Default contructor uses P19937 and initializes its seed in current timestamp.
     */
    public SFMTRandom() {
        this(SFMTParam.P19937);
    }

    /**
     * Construct with specified parameters, and initialize its seed in current timestamp.
     *
     * @param param SFMT parameters
     */
    public SFMTRandom(SFMTParam param) {
        this.param = param;
        this.state = new W128T[param.SFMT_N];
        for (int i = 0; i < this.state.length; i++) {
            this.state[i] = new W128T();
        }
        long tm = System.currentTimeMillis();
        setSeed((int) (tm << Integer.SIZE), (int) tm);
    }

    /**
     * Construct with specified parameters and seed.
     *
     * @param param SFMT parameters
     * @param seed  seed
     */
    public SFMTRandom(SFMTParam param, int seed) {
        this(param);
        setSeed(seed);
    }

    /**
     * Construct with specified parameters and seed.
     *
     * @param param SFMT parameters
     * @param seed  seed
     */
    public SFMTRandom(SFMTParam param, int... seed) {
        this(param);
        setSeed(seed);
    }

    /**
     * Construct with P19937 and specified parameters and seed.
     *
     * @param seed seed
     */
    public SFMTRandom(int seed) {
        this(SFMTParam.P19937, seed);
    }

    /**
     * Construct with P19937 and specified parameters and seed.
     *
     * @param seed seed
     */
    public SFMTRandom(int... seed) {
        this(SFMTParam.P19937, seed);
    }

    private int getInt(int i) {
        return state[i / (W128T.BUFFER_SIZE / Integer.BYTES)].u(i % (W128T.BUFFER_SIZE / Integer.BYTES));
    }

    private void setInt(int i, int value) {
        state[i / (W128T.BUFFER_SIZE / Integer.BYTES)].u(i % (W128T.BUFFER_SIZE / Integer.BYTES), value);
    }

    private void addInt(int i, int value) {
        state[i / (W128T.BUFFER_SIZE / Integer.BYTES)].u_add(i % (W128T.BUFFER_SIZE / Integer.BYTES), value);
    }

    private void xorInt(int i, int value) {
        state[i / (W128T.BUFFER_SIZE / Integer.BYTES)].u_xor(i % (W128T.BUFFER_SIZE / Integer.BYTES), value);
    }

    private long getLong(int i) {
        return state[i / (W128T.BUFFER_SIZE / Long.BYTES)].u64(i % (W128T.BUFFER_SIZE / Long.BYTES));
    }

    /**
     * Generate and return a 32 bit pseudo-random integer.
     *
     * @return 32 bit pseudo-random integer
     * @since inline static uint32_t sfmt_genrand_uint32(sfmt_t * sfmt)
     */
    public int nextInt() {
        if (idx >= param.SFMT_N32) {
            fillStateToRandom();
            idx = 0;
        }
        int r = getInt(idx);
        idx++;
        return r;
    }

    /**
     * Generate and return a 64 bit pseudo-random integer.
     *
     * @return 64 bit pseudo-random integer
     * @since inline static uint64_t sfmt_genrand_uint64(sfmt_t * sfmt)
     */
    public long nextLong() {
        if (idx % 2 == 0) {
            nextInt();
        }
        if (idx >= param.SFMT_N32) {
            fillStateToRandom();
            idx = 0;
        }
        long r = getLong(idx / 2);
        idx += 2;
        return r;
    }

    /**
     * Generate and return a pseudo-random double-precision real in [0,1).
     *
     * @return double-precision pseudo-random real
     * @since inline static double sfmt_to_real2(sfmt_t * sfmt)
     */
    public double nextDouble() {
        long value = nextInt();
        return (value - Integer.MIN_VALUE) / (double) 0x100000000L;
    }

    /**
     * This function represents the recursion formula.
     *
     * @param r output
     * @param a a 128-bit part of the internal state array
     * @param b a 128-bit part of the internal state array
     * @param c a 128-bit part of the internal state array
     * @param d a 128-bit part of the internal state array
     * @since inline static void doRecursion(w128_t *r, w128_t *a, w128_t *b, w128_t *c,
     */
    private void doRecursion(W128T r, W128T a, W128T b, W128T c, W128T d) {
        W128T x = a.lshift128(param.SFMT_SL2);
        W128T y = c.rshift128(param.SFMT_SR2);

        for (int i = 0; i < 4; i++) {
            r.u(i, a.u(i) ^ (x.u(i) ^ (((b.u(i) >>> param.SFMT_SR1) & param.SFMT_MSK(i)) ^ (y.u(i) ^ (d.u(i) << param.SFMT_SL1)))));
        }
    }

    /**
     * This function simulate a 64-bit index of LITTLE ENDIAN in BIG ENDIAN machine.
     *
     * @since inline static int idxof(int i)
     */
    private static int idxof(int i) {
        return i;
    }

    /**
     * This function represents a function used in the initialization by setSeed.
     *
     * @param x 32-bit integer
     * @return 32-bit integer
     * @since static uint32_t func1(uint32_t x)
     */
    private static int func1(int x) {
        return (x ^ (x >>> 27)) * (int) 1664525L;
    }

    /**
     * This function represents a function used in the initialization by setSeed.
     *
     * @param x 32-bit integer
     * @return 32-bit integer
     * @since static uint32_t func2(uint32_t x)
     */
    private static int func2(int x) {
        return (x ^ (x >>> 27)) * (int) 1566083941L;
    }

    /**
     * This function certificate the period of 2^{MEXP}
     *
     * @since static void periodCertification(sfmt_t * sfmt)
     */
    private void periodCertification() {
        int inner = 0;

        for (int i = 0; i < 4; i++) {
            inner ^= state[0].u(idxof(i)) & param.SFMT_PARITY(i);
        }
        for (int i = 16; i > 0; i >>>= 1) {
            inner ^= inner >>> i;
        }
        inner &= 1;
        // check OK
        if (inner == 1) {
            return;
        }
        // check NG, and modification
        int work;
        for (int i = 0; i < 4; i++) {
            work = 1;
            for (int j = 0; j < 32; j++) {
                if ((work & param.SFMT_PARITY(i)) != 0) {
                    state[0].u_xor(idxof(i), work);
                    return;
                }
                work = work << 1;
            }
        }
    }

    /**
     * Return the parameter identification string used by this instance. The string shows the word size, the Mersenne
     * exponent, and all parameters of this generator.
     *
     * @return {@link SFMTParam#SFMT_IDSTR}
     * @since const char *sfmt_get_idstring(sfmt_t * sfmt)
     */
    public String getId() {
        return param.SFMT_IDSTR;
    }

    /**
     * This function fills the internal state array with pseudo-random integers.
     *
     * @since void sfmt_gen_rand_all(sfmt_t * sfmt)
     */
    private void fillStateToRandom() {
        W128T r1 = state[param.SFMT_N - 2];
        W128T r2 = state[param.SFMT_N - 1];

        int i = 0;
        for (; i < param.SFMT_N - param.SFMT_POS1; i++) {
            doRecursion(state[i], state[i], state[i + param.SFMT_POS1], r1, r2);
            r1 = r2;
            r2 = state[i];
        }
        for (; i < param.SFMT_N; i++) {
            doRecursion(state[i], state[i], state[i + param.SFMT_POS1 - param.SFMT_N], r1, r2);
            r1 = r2;
            r2 = state[i];
        }
    }

    /**
     * Generate bulk 32bit pseudo-random integers in the specified return buffer {@code array[]} by one call. NOTE that
     * this function cannot use after calling {@link #nextInt()} or {@link #nextLong()} function without initialization.
     * <p>
     * It is recommended that the length of array is <u>at least {@link SFMTParam#SFMT_N32}</u> and <u>a multiple of
     * four</u> for the best performance.
     *
     * @param array a return buffer where pseudo-random 32bit integers are filled.
     * @return the specified array
     * @throws IllegalStateException in case this function is used after calling nextInt() or nextLong()
     * @since void sfmt_fill_array32(sfmt_t * sfmt, uint32_t *array, int size)
     */
    public int[] newRandomInt(int[] array) throws IllegalStateException, IllegalArgumentException {
        if (idx != param.SFMT_N32) {
            throw new IllegalStateException("newRandomInt() cannot use after calling nextInt() or nextLong()");
        }

        int len;
        if (array.length < param.SFMT_N32) {
            len = param.SFMT_N32;
        } else {
            len = array.length;
        }
        if (len % 4 != 0) {
            len += 4 - (len % 4);
        }

        W128T[] temp = new W128T[len / 4];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = new W128T();
        }
        newRandomW128T(temp);
        for (int i = 0; i < array.length; i++) {
            array[i] = temp[i / 4].u(i % 4);
        }
        idx = param.SFMT_N32;
        return array;
    }


    /**
     * Generate bulk 64bit pseudo-random integers in the specified return buffer {@code array[]} by one call. NOTE that
     * this function cannot use after calling {@link #nextInt()} or {@link #nextLong()} function without initialization.
     * <p>
     * It is recommended that the length of array is <u>at least {@link SFMTParam#SFMT_N64}</u> and <u>a multiple of
     * two</u> for the best performance.
     *
     * @param array a return buffer where presudo-random 64bit integers are filled.
     * @return the specified array
     * @throws IllegalStateException in case this function is used after calling nextInt() or nextLong()
     * @since void sfmt_fill_array64(sfmt_t * sfmt, uint64_t *array, int size)
     */
    public long[] newRandomLong(long[] array) {
        if (idx != param.SFMT_N32) {
            throw new IllegalStateException("newRandomInt() cannot use after calling nextInt() or nextLong()");
        }

        int len;
        if (array.length < param.SFMT_N64) {
            len = param.SFMT_N64;
        } else {
            len = array.length;
        }
        if (len % 2 != 0) {
            len += 2 - (len % 2);
        }

        W128T[] temp = new W128T[len / 2];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = new W128T();
        }
        newRandomW128T(temp);
        for (int i = 0; i < array.length; i++) {
            array[i] = temp[i / 2].u64(i % 2);
        }
        idx = param.SFMT_N32;
        return array;
    }

    /**
     * This function fills the user-specified array with pseudo-random integers.
     *
     * @param array an 128bit array to be filled by pseudo-random numbers.
     * @since inline static void newRandomW128T(sfmt_t * sfmt, w128_t *array, int size)
     */
    private void newRandomW128T(W128T[] array) {
        W128T r1 = state[param.SFMT_N - 2];
        W128T r2 = state[param.SFMT_N - 1];

        int i = 0;
        for (; i < param.SFMT_N - param.SFMT_POS1; i++) {
            doRecursion(array[i], state[i], state[i + param.SFMT_POS1], r1, r2);
            r1 = r2;
            r2 = array[i];
        }
        for (; i < param.SFMT_N; i++) {
            doRecursion(array[i], state[i], array[i + param.SFMT_POS1 - param.SFMT_N], r1, r2);
            r1 = r2;
            r2 = array[i];
        }
        for (; i < array.length - param.SFMT_N; i++) {
            doRecursion(array[i], array[i - param.SFMT_N], array[i + param.SFMT_POS1 - param.SFMT_N], r1, r2);
            r1 = r2;
            r2 = array[i];
        }
        int j = 0;
        for (; j < 2 * param.SFMT_N - array.length; j++) {
            state[j] = array[j + array.length - param.SFMT_N];
        }
        for (; i < array.length; i++, j++) {
            doRecursion(array[i], array[i - param.SFMT_N], array[i + param.SFMT_POS1 - param.SFMT_N], r1, r2);
            r1 = r2;
            r2 = array[i];
            state[j] = array[i];
        }
    }


    /**
     * Initialize internal random state with specified 32bit integer seed.
     *
     * @param seed a 32-bit integer used as the seed.
     * @since void sfmt_init_gen_rand(sfmt_t * sfmt, uint32_t seed)
     */
    public void setSeed(int seed) {
        state[0].u(idxof(0), seed);
        for (int i = 1; i < param.SFMT_N32; i++) {
            setInt(i, 1812433253 * (getInt(idxof(i - 1)) ^ (getInt(idxof(i - 1)) >>> 30)) + i);
        }
        idx = param.SFMT_N32;
        periodCertification();
    }

    /**
     * Initialize internal random state with speicified 32bit integer array seed.
     *
     * @param seed an array of 32bit integers as seed
     * @since void sfmt_init_by_array(sfmt_t * sfmt, uint32_t *init_key, int key_length)
     */
    public void setSeed(int... seed) {
        int size = param.SFMT_N * 4;

        int lag;
        if (size >= 623) {
            lag = 11;
        } else if (size >= 68) {
            lag = 7;
        } else if (size >= 39) {
            lag = 5;
        } else {
            lag = 3;
        }

        int mid = (size - lag) / 2;

        // memset(sfmt, 0x8b, sizeof(sfmt_t));
        for (W128T aState : state) {
            aState.fill((byte) 0x8b);
        }

        int count;
        if (seed.length + 1 > param.SFMT_N32) {
            count = seed.length + 1;
        } else {
            count = param.SFMT_N32;
        }
        int r = func1(getInt(idxof(0)) ^ getInt(idxof(mid)) ^ getInt(idxof(param.SFMT_N32 - 1)));
        addInt(idxof(mid), r);
        r += seed.length;
        addInt(idxof(mid + lag), r);
        setInt(idxof(0), r);

        int i = 1;
        int j = 0;
        count--;
        for (; (j < count) && (j < seed.length); j++) {
            r = func1(getInt(idxof(i)) ^ getInt(idxof((i + mid) % param.SFMT_N32))
                    ^ getInt(idxof((i + param.SFMT_N32 - 1) % param.SFMT_N32)));
            addInt(idxof((i + mid) % param.SFMT_N32), r);
            r += seed[j] + i;
            addInt(idxof((i + mid + lag) % param.SFMT_N32), r);
            setInt(idxof(i), r);
            i = (i + 1) % param.SFMT_N32;
        }
        for (; j < count; j++) {
            r = func1(getInt(idxof(i)) ^ getInt(idxof((i + mid) % param.SFMT_N32))
                    ^ getInt(idxof((i + param.SFMT_N32 - 1) % param.SFMT_N32)));
            addInt(idxof((i + mid) % param.SFMT_N32), r);
            r += i;
            addInt(idxof((i + mid + lag) % param.SFMT_N32), r);
            setInt(idxof(i), r);
            i = (i + 1) % param.SFMT_N32;
        }
        for (j = 0; j < param.SFMT_N32; j++) {
            r = func2(getInt(idxof(i)) + getInt(idxof((i + mid) % param.SFMT_N32))
                    + getInt(idxof((i + param.SFMT_N32 - 1) % param.SFMT_N32)));
            xorInt(idxof((i + mid) % param.SFMT_N32), r);
            r -= i;
            xorInt(idxof((i + mid + lag) % param.SFMT_N32), r);
            setInt(idxof(i), r);
            i = (i + 1) % param.SFMT_N32;
        }

        idx = param.SFMT_N32;
        periodCertification();
    }


    /**
     * 128-bit data structure
     */
    public static class W128T {
        static final int BUFFER_SIZE = 128 / 8;
        private ByteBuffer b = ByteBuffer.allocate(BUFFER_SIZE);

        W128T() {
            b.order(ByteOrder.BIG_ENDIAN);
        }

        int u(int i) {
            assert i < BUFFER_SIZE : i + ">=" + BUFFER_SIZE;
            return b.getInt(i * Integer.BYTES);
        }

        void u(int i, int value) {
            assert i < BUFFER_SIZE : i + ">=" + BUFFER_SIZE;
            b.putInt(i * Integer.BYTES, value);
        }

        void u_add(int i, int increment) {
            b.putInt(i * Integer.BYTES, b.getInt(i * Integer.BYTES) + increment);
        }

        void u_xor(int i, int x) {
            b.putInt(i * Integer.BYTES, b.getInt(i * Integer.BYTES) ^ x);
        }

        long u64(int i) {
            return (b.getInt(i * Long.BYTES) & 0xFFFFFFFFL) | ((b.getInt(i * Long.BYTES + Integer.BYTES) & 0xFFFFFFFFL) << 32);
        }

        private BigInteger u64bi(int i) {
            BigInteger bl = BigInteger.valueOf(b.getInt(i * Long.BYTES) & 0xFFFFFFFFL);
            BigInteger bh = BigInteger.valueOf(b.getInt(i * Long.BYTES + Integer.BYTES) & 0xFFFFFFFFL);
            return bh.shiftLeft(32).or(bl);
        }

        private void fill(byte bx) {
            for (int i = 0; i < BUFFER_SIZE; i++) {
                b.put(i, bx);
            }
        }

        /**
         * This function simulates SIMD 128-bit left shift by the standard C.
         * The 128-bit integer given in in is shifted by (shift * 8) bits.
         * This function simulates the LITTLE ENDIAN SIMD.
         *
         * @param shift the shift value
         * @since inline static void lshift128(w128_t *out, w128_t const *in, int shift) {
         */
        W128T lshift128(int shift) {
            W128T out = new W128T();

            long th = ((this.u(3) & 0xFFFFFFFFL) << 32) | (this.u(2) & 0xFFFFFFFFL);
            long tl = ((this.u(1) & 0xFFFFFFFFL) << 32) | (this.u(0) & 0xFFFFFFFFL);

            long oh = th << (shift * 8);
            long ol = tl << (shift * 8);
            oh |= tl >>> (64 - shift * 8);
            out.u(1, (int) (ol >>> 32));
            out.u(0, (int) ol);
            out.u(3, (int) (oh >>> 32));
            out.u(2, (int) oh);
            return out;
        }

        /**
         * This function simulates SIMD 128-bit right shift by the standard C.
         * The 128-bit integer given in in is shifted by (shift * 8) bits.
         * This function simulates the LITTLE ENDIAN SIMD.
         *
         * @param shift the shift value
         * @since inline static void rshift128(w128_t *out, w128_t const *in, int shift)
         */
        W128T rshift128(int shift) {
            long th, tl, oh, ol;
            W128T out = new W128T();

            th = ((this.u(3) & 0xFFFFFFFFL) << 32) | ((this.u(2) & 0xFFFFFFFFL));
            tl = ((this.u(1) & 0xFFFFFFFFL) << 32) | ((this.u(0) & 0xFFFFFFFFL));

            oh = th >>> (shift * 8);
            ol = tl >>> (shift * 8);
            ol |= th << (64 - shift * 8);
            out.u(1, (int) (ol >>> 32));
            out.u(0, (int) ol);
            out.u(3, (int) (oh >>> 32));
            out.u(2, (int) oh);
            return out;
        }

        public String toString() {
            return String.format("u={%d, %d,  %d, %d}, u64={%s, %s}",
                    u(0) & 0xFFFFFFFFL, u(1) & 0xFFFFFFFFL, u(2) & 0xFFFFFFFFL, u(3) & 0xFFFFFFFFL,
                    u64bi(0), u64bi(1));
        }

    }
}
