package at.hazm.math;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * derived from 1.5.1
 *
 * @see <a href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/SFMT/"></a>
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

    public SFMTRandom(SFMTParam param) {
        this.param = param;
        this.state = new W128T[param.SFMT_N];
        for (int i = 0; i < this.state.length; i++) {
            this.state[i] = new W128T();
        }
    }

    public SFMTRandom(SFMTParam param, int seed) {
        this(param);
        setSeed(seed);
    }

    public SFMTRandom(SFMTParam param, int[] seed) {
        this(param);
        init_by_array(seed);
    }

    private int psfmt32(int i) {
        return state[i / (W128T.BUFFER_SIZE / Integer.BYTES)].u(i % (W128T.BUFFER_SIZE / Integer.BYTES));
    }

    private void psfmt32(int i, int value) {
        state[i / (W128T.BUFFER_SIZE / Integer.BYTES)].u(i % (W128T.BUFFER_SIZE / Integer.BYTES), value);
    }

    private void psfmt32_add(int i, int value) {
        state[i / (W128T.BUFFER_SIZE / Integer.BYTES)].u_add(i % (W128T.BUFFER_SIZE / Integer.BYTES), value);
    }

    private void psfmt32_xor(int i, int value) {
        state[i / (W128T.BUFFER_SIZE / Integer.BYTES)].u_xor(i % (W128T.BUFFER_SIZE / Integer.BYTES), value);
    }

    private long psfmt64(int i) {
        return state[i / (W128T.BUFFER_SIZE / Long.BYTES)].u64(i % (W128T.BUFFER_SIZE / Long.BYTES));
    }

    /**
     * Generate and returns a 32-bit pseudorandom number.
     * setSeed or init_by_array must be called before this function.
     *
     * @return 32-bit pseudorandom number
     * @apiNote inline static uint32_t sfmt_genrand_uint32(sfmt_t * sfmt)
     */
    public int nextInt() {
        if (idx >= param.SFMT_N32) {
            gen_rand_all();
            idx = 0;
        }
        int r = psfmt32(idx);
        idx++;
        return r;
    }

    /**
     * Generate and return a 64-bit pseudorandom number.
     * setSeed or init_by_array must be called before this function.
     * The function gen_rand64 should not be called after gen_rand32,
     * unless an initialization is again executed.
     *
     * @return 64-bit pseudorandom number
     * @apiNote inline static uint64_t sfmt_genrand_uint64(sfmt_t * sfmt)
     */
    public long nextLong() {
        assert idx % 2 == 0 : idx;
        if (idx >= param.SFMT_N32) {
            gen_rand_all();
            idx = 0;
        }
        long r = psfmt64(idx / 2);
        idx += 2;
        return r;
    }

/* =================================================
   The following real versions are due to Isaku Wada
   ================================================= */

    /**
     * converts a signed 32-bit number to a double on [0,1]-real-interval.
     *
     * @param v 32-bit signed integer
     * @return double on [0,1]-real-interval
     * @apiNote inline static double sfmt_to_real1(uint32_t v)
     */
    public static double to_real1(int v) {
        // divided by 2^32-1
        return ((long) v - Integer.MIN_VALUE) / (double) 0xFFFFFFFFL;
    }

    /**
     * generates a random number on [0,1]-real-interval
     *
     * @return double on [0,1]-real-interval
     * @apiNote inline static double sfmt_genrand_real1(sfmt_t * sfmt)
     */
    public double genrand_real1() {
        return to_real1(nextInt());
    }

    /**
     * converts a signed 32-bit integer to a double on [0,1)-real-interval.
     *
     * @param v 32-bit signed integer
     * @return double on [0,1)-real-interval
     * @apiNote inline static double sfmt_to_real2(uint32_t v)
     */
    public static double to_real2(int v) {
        // divided by 2^32
        return ((long) v - Integer.MIN_VALUE) / (double) 0x100000000L;
    }

    /**
     * generates a random number on [0,1)-real-interval
     *
     * @return double on [0,1)-real-interval
     * @apiNote inline static double sfmt_genrand_real2(sfmt_t * sfmt)
     */
    public double to_real2() {
        return to_real2(nextInt());
    }

    /**
     * converts a signed 32-bit integer to a double on (0,1)-real-interval.
     *
     * @param v 32-bit signed integer
     * @return double on (0,1)-real-interval
     */
    public static double to_real3(int v) {
        // divided by 2^32
        return ((long) v - Integer.MIN_VALUE + 0.5) / (double) 0x100000000L;
    }

    /**
     * generates a random number on (0,1)-real-interval
     *
     * @return double on (0,1)-real-interval
     * @apiNote inline static double sfmt_genrand_real3(sfmt_t * sfmt)
     */
    public double genrand_real3() {
        return to_real3(nextInt());
    }

    /**
     * converts an unsigned 32-bit integer to double on [0,1)
     * with 53-bit resolution.
     *
     * @param v 32-bit unsigned integer
     * @return double on [0,1)-real-interval with 53-bit resolution.
     * @apiNote inline static double sfmt_to_res53(uint64_t v)
     */
    public static double to_res53(long v) {
        return (v >>> 11) * (1.0 / 9007199254740992.0);
    }

    /**
     * generates a random number on [0,1) with 53-bit resolution
     *
     * @return double on [0,1) with 53-bit resolution
     * @apiNote inline static double sfmt_genrand_res53(sfmt_t * sfmt)
     */
    public double genrand_res53() {
        return to_res53(nextLong());
    }

    /* =================================================
    The following function are added by Saito.
    ================================================= */

    /**
     * generates a random number on [0,1) with 53-bit resolution from two
     * 32 bit integers
     *
     * @apiNote inline static double sfmt_to_res53_mix(uint32_t x, uint32_t y)
     */
    private static double to_res53_mix(int x, int y) {
        return to_res53(x | ((long) y << 32));
    }

    /**
     * generates a random number on [0,1) with 53-bit resolution
     * using two 32bit integers.
     *
     * @return double on [0,1) with 53-bit resolution
     * @apiNote inline static double sfmt_genrand_res53_mix(sfmt_t * sfmt)
     */
    private double genrand_res53_mix() {
        int x = nextInt();
        int y = nextInt();
        return to_res53_mix(x, y);
    }

    /**
     * This function represents the recursion formula.
     *
     * @param r output
     * @param a a 128-bit part of the internal state array
     * @param b a 128-bit part of the internal state array
     * @param c a 128-bit part of the internal state array
     * @param d a 128-bit part of the internal state array
     * @apiNote inline static void do_recursion(w128_t *r, w128_t *a, w128_t *b, w128_t *c,
     */
    private void do_recursion(W128T r, W128T a, W128T b, W128T c, W128T d) {
        W128T x = a.lshift128(param.SFMT_SL2);
        W128T y = c.rshift128(param.SFMT_SR2);

        for (int i = 0; i < 4; i++) {
            r.u(i, a.u(i) ^ (x.u(i) ^ (((b.u(i) >>> param.SFMT_SR1) & param.SFMT_MSK(i)) ^ (y.u(i) ^ (d.u(i) << param.SFMT_SL1)))));
        }
    }

    /**
     * This function simulate a 64-bit index of LITTLE ENDIAN
     * in BIG ENDIAN machine.
     *
     * @apiNote inline static int idxof(int i)
     */
    private static int idxof(int i) {
        return i;
    }

    /**
     * This function represents a function used in the initialization
     * by init_by_array
     *
     * @param x 32-bit integer
     * @return 32-bit integer
     * @apiNote static uint32_t func1(uint32_t x)
     */
    private static int func1(int x) {
        return (x ^ (x >>> 27)) * (int) 1664525L;
    }

    /**
     * This function represents a function used in the initialization
     * by init_by_array
     *
     * @param x 32-bit integer
     * @return 32-bit integer
     * @apiNote static uint32_t func2(uint32_t x)
     */
    private static int func2(int x) {
        return (x ^ (x >>> 27)) * (int) 1566083941L;
    }

    /**
     * This function certificate the period of 2^{MEXP}
     *
     * @apiNote static void period_certification(sfmt_t * sfmt)
     */
    private void period_certification() {
        int inner = 0;
        int work;

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
        /*
        uint32_t inner = 0;
        int i, j;
        uint32_t work;
        uint32_t *psfmt32 = &sfmt->state[0].u[0];
    const uint32_t parity[4] = {SFMT_PARITY1, SFMT_PARITY2,
                SFMT_PARITY3, SFMT_PARITY4};

        for (i = 0; i < 4; i++) {
            inner ^= psfmt32[idxof(i)] & parity[i];
        }
        for (i = 16; i > 0; i >>= 1) {
            inner ^= inner >> i;
        }
        inner &= 1;
    // check OK
        if (inner == 1) {
            return;
        }
    // check NG, and modification
        for (i = 0; i < 4; i++) {
            work = 1;
            for (j = 0; j < 32; j++) {
                if ((work & parity[i]) != 0) {
                    psfmt32[idxof(i)] ^= work;
                    return;
                }
                work = work << 1;
            }
        }
        */
    }

    /**
     * Return the identification string. The string shows the word size, the Mersenne exponent, and all parameters of
     * this generator.
     *
     * @return {@link SFMTParam#SFMT_IDSTR}
     * @apiNote const char *sfmt_get_idstring(sfmt_t * sfmt)
     */
    public String getId() {
        return param.SFMT_IDSTR;
    }


    /**
     * Return the minimum size of array used for {@link #fill_array32(int[])}.
     *
     * @return minimum size of array used for fill_array32().
     * @apiNote int sfmt_get_min_array_size32(sfmt_t * sfmt)
     */
    public int get_min_array_size32() {
        return param.SFMT_N32;
    }

    /**
     * Return the minimum size of array used for {@link #fill_array64(long[])}.
     *
     * @return minimum size of array used for fill_array64().
     * @apiNote int sfmt_get_min_array_size64(sfmt_t * sfmt)
     */
    public int get_min_array_size64() {
        return param.SFMT_N64;
    }

    /**
     * This function fills the internal state array with pseudorandom
     * integers.
     *
     * @apiNote void sfmt_gen_rand_all(sfmt_t * sfmt)
     */
    private void gen_rand_all() {
        W128T r1 = state[param.SFMT_N - 2];
        W128T r2 = state[param.SFMT_N - 1];

        int i = 0;
        for (; i < param.SFMT_N - param.SFMT_POS1; i++) {
            do_recursion(state[i], state[i], state[i + param.SFMT_POS1], r1, r2);
            r1 = r2;
            r2 = state[i];
        }
        for (; i < param.SFMT_N; i++) {
            do_recursion(state[i], state[i], state[i + param.SFMT_POS1 - param.SFMT_N], r1, r2);
            r1 = r2;
            r2 = state[i];
        }
        /*
        int i;
        w128_t *r1, *r2;

        r1 = &sfmt->state[SFMT_N - 2];
        r2 = &sfmt->state[SFMT_N - 1];
        for (i = 0; i < SFMT_N - SFMT_POS1; i++) {
            do_recursion(&sfmt->state[i], &sfmt->state[i],
                     &sfmt->state[i + SFMT_POS1], r1, r2);
            r1 = r2;
            r2 = &sfmt->state[i];
        }
        for (; i < SFMT_N; i++) {
            do_recursion(&sfmt->state[i], &sfmt->state[i],
                     &sfmt->state[i + SFMT_POS1 - SFMT_N], r1, r2);
            r1 = r2;
            r2 = &sfmt->state[i];
        }
        */
    }

    /**
     * This function generates pseudorandom 32-bit integers in the
     * specified array[] by one call. The number of pseudorandom integers
     * is specified by the argument size, which must be at least 624 and a
     * multiple of four.  The generation by this function is much faster
     * than the following gen_rand function.
     * <p>
     * For initialization, setSeed or init_by_array must be called
     * before the first call of this function. This function can not be
     * used after calling gen_rand function, without initialization.
     *
     * @param array an array where pseudorandom 32-bit integers are filled
     *              by this function.  The pointer to the array must be \b "aligned"
     *              (namely, must be a multiple of 16) in the SIMD version, since it
     *              refers to the address of a 128-bit integer.  In the standard C
     *              version, the pointer is arbitrary.
     * @param size  the number of 32-bit pseudorandom integers to be
     *              generated.  size must be a multiple of 4, and greater than or equal
     *              to (MEXP / 128 + 1) * 4.
     * @note \b memalign or \b posix_memalign is available to get aligned
     * memory. Mac OSX doesn't have these functions, but \b malloc of OSX
     * returns the pointer to the aligned memory block.
     * @apiNote void sfmt_fill_array32(sfmt_t * sfmt, uint32_t *array, int size)
     */
    public void fill_array32(int[] array) {
        assert (idx == param.SFMT_N32);
        assert (array.length % 4 == 0);
        assert (array.length >= param.SFMT_N32);

        W128T[] temp = new W128T[array.length / 4];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = new W128T();
        }
        gen_rand_array(temp);
        for (int i = 0; i < temp.length; i++) {
            array[i * 4] = temp[i].u(0);
            array[i * 4 + 1] = temp[i].u(1);
            array[i * 4 + 2] = temp[i].u(2);
            array[i * 4 + 3] = temp[i].u(3);
        }
        idx = param.SFMT_N32;
    }


    /**
     * This function generates pseudorandom 64-bit integers in the
     * specified array[] by one call. The number of pseudorandom integers
     * is specified by the argument size, which must be at least 312 and a
     * multiple of two.  The generation by this function is much faster
     * than the following gen_rand function.
     * <p>
     * For initialization, setSeed or init_by_array must be called
     * before the first call of this function. This function can not be
     * used after calling gen_rand function, without initialization.
     *
     * @param array an array where pseudorandom 64-bit integers are filled
     *              by this function.  The pointer to the array must be "aligned"
     *              (namely, must be a multiple of 16) in the SIMD version, since it
     *              refers to the address of a 128-bit integer.  In the standard C
     *              version, the pointer is arbitrary.
     * @param size  the number of 64-bit pseudorandom integers to be
     *              generated.  size must be a multiple of 2, and greater than or equal
     *              to (MEXP / 128 + 1) * 2
     * @note \b memalign or \b posix_memalign is available to get aligned
     * memory. Mac OSX doesn't have these functions, but \b malloc of OSX
     * returns the pointer to the aligned memory block.
     * @apiNote void sfmt_fill_array64(sfmt_t * sfmt, uint64_t *array, int size)
     */
    public void fill_array64(long[] array) {
        assert (idx == param.SFMT_N32);
        assert (array.length % 2 == 0);
        assert (array.length >= param.SFMT_N64);

        W128T[] temp = new W128T[array.length / 2];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = new W128T();
        }
        gen_rand_array(temp);
        for (int i = 0; i < temp.length; i++) {
            array[i * 2] = temp[i].u64(0);
            array[i * 2 + 1] = temp[i].u64(1);
        }
        idx = param.SFMT_N32;
    }

    /**
     * This function fills the user-specified array with pseudorandom integers.
     *
     * @param array an 128-bit array to be filled by pseudorandom numbers.
     * @apiNote inline static void gen_rand_array(sfmt_t * sfmt, w128_t *array, int size)
     */
    private void gen_rand_array(W128T[] array) {
        W128T r1 = state[param.SFMT_N - 2];
        W128T r2 = state[param.SFMT_N - 1];

        int i = 0;
        for (; i < param.SFMT_N - param.SFMT_POS1; i++) {
            do_recursion(array[i], state[i], state[i + param.SFMT_POS1], r1, r2);
            r1 = r2;
            r2 = array[i];
        }
        for (; i < param.SFMT_N; i++) {
            do_recursion(array[i], state[i], array[i + param.SFMT_POS1 - param.SFMT_N], r1, r2);
            r1 = r2;
            r2 = array[i];
        }
        for (; i < array.length - param.SFMT_N; i++) {
            do_recursion(array[i], array[i - param.SFMT_N], array[i + param.SFMT_POS1 - param.SFMT_N], r1, r2);
            r1 = r2;
            r2 = array[i];
        }
        int j = 0;
        for (; j < 2 * param.SFMT_N - array.length; j++) {
            state[j] = array[j + array.length - param.SFMT_N];
        }
        for (; i < array.length; i++, j++) {
            do_recursion(array[i], array[i - param.SFMT_N], array[i + param.SFMT_POS1 - param.SFMT_N], r1, r2);
            r1 = r2;
            r2 = array[i];
            state[j] = array[i];
        }
    }


    /**
     * This function initializes the internal state array with a 32-bit
     * integer seed.
     *
     * @param seed a 32-bit integer used as the seed.
     * @apiNote void sfmt_init_gen_rand(sfmt_t * sfmt, uint32_t seed)
     */
    public void setSeed(int seed) {
        state[0].u(idxof(0), seed);
        for (int i = 1; i < param.SFMT_N32; i++) {
            psfmt32(i, 1812433253 * (psfmt32(idxof(i - 1)) ^ (psfmt32(idxof(i - 1)) >>> 30)) + i);
        }
        idx = param.SFMT_N32;
        period_certification();
    }

    /**
     * This function initializes the internal state array,
     * with an array of 32-bit integers used as the seeds
     *
     * @param init_key   the array of 32-bit integers, used as a seed.
     * @param key_length the length of init_key.
     * @apiNote void sfmt_init_by_array(sfmt_t * sfmt, uint32_t *init_key, int key_length)
     */
    public void init_by_array(int... init_key) {
        int count;
        int r;
        int lag;
        int mid;
        int size = param.SFMT_N * 4;

        if (size >= 623) {
            lag = 11;
        } else if (size >= 68) {
            lag = 7;
        } else if (size >= 39) {
            lag = 5;
        } else {
            lag = 3;
        }
        mid = (size - lag) / 2;

//        struct SFMT_T {
//            w128_t state[SFMT_N];
//            int idx;
//        };
        // memset(sfmt, 0x8b, sizeof(sfmt_t));
        for (W128T aState : state) {
            aState.fill((byte) 0x8b);
        }

        if (init_key.length + 1 > param.SFMT_N32) {
            count = init_key.length + 1;
        } else {
            count = param.SFMT_N32;
        }
        r = func1(psfmt32(idxof(0)) ^ psfmt32(idxof(mid)) ^ psfmt32(idxof(param.SFMT_N32 - 1)));
        psfmt32_add(idxof(mid), r);
        r += init_key.length;
        psfmt32_add(idxof(mid + lag), r);
        psfmt32(idxof(0), r);

        int i = 1;
        int j = 0;
        count--;
        for (; (j < count) && (j < init_key.length); j++) {
            r = func1(psfmt32(idxof(i)) ^ psfmt32(idxof((i + mid) % param.SFMT_N32))
                    ^ psfmt32(idxof((i + param.SFMT_N32 - 1) % param.SFMT_N32)));
            psfmt32_add(idxof((i + mid) % param.SFMT_N32), r);
            r += init_key[j] + i;
            psfmt32_add(idxof((i + mid + lag) % param.SFMT_N32), r);
            psfmt32(idxof(i), r);
            i = (i + 1) % param.SFMT_N32;
        }
        for (; j < count; j++) {
            r = func1(psfmt32(idxof(i)) ^ psfmt32(idxof((i + mid) % param.SFMT_N32))
                    ^ psfmt32(idxof((i + param.SFMT_N32 - 1) % param.SFMT_N32)));
            psfmt32_add(idxof((i + mid) % param.SFMT_N32), r);
            r += i;
            psfmt32_add(idxof((i + mid + lag) % param.SFMT_N32), r);
            psfmt32(idxof(i), r);
            i = (i + 1) % param.SFMT_N32;
        }
        for (j = 0; j < param.SFMT_N32; j++) {
            r = func2(psfmt32(idxof(i)) + psfmt32(idxof((i + mid) % param.SFMT_N32))
                    + psfmt32(idxof((i + param.SFMT_N32 - 1) % param.SFMT_N32)));
            psfmt32_xor(idxof((i + mid) % param.SFMT_N32), r);
            r -= i;
            psfmt32_xor(idxof((i + mid + lag) % param.SFMT_N32), r);
            psfmt32(idxof(i), r);
            i = (i + 1) % param.SFMT_N32;
        }

        idx = param.SFMT_N32;
        period_certification();
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
         * @apiNote inline static void lshift128(w128_t *out, w128_t const *in, int shift) {
         */
        W128T lshift128(int shift) {
            long th, tl, oh, ol;
            W128T out = new W128T();

            th = ((this.u(3) & 0xFFFFFFFFL) << 32) | (this.u(2) & 0xFFFFFFFFL);
            tl = ((this.u(1) & 0xFFFFFFFFL) << 32) | (this.u(0) & 0xFFFFFFFFL);

            oh = th << (shift * 8);
            ol = tl << (shift * 8);
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
         * @apiNote inline static void rshift128(w128_t *out, w128_t const *in, int shift)
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
