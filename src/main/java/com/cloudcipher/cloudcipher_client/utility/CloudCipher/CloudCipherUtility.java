package com.cloudcipher.cloudcipher_client.utility.CloudCipher;

import java.security.SecureRandom;

public class CloudCipherUtility {
    AONTH aonth;
    Permutation perm;
    KeyGenerator g;
    public int[] iv;
    long time1 = 0;
    long time2 = 0;
    long time3 = 0;
    long time4 = 0;
    long time5 = 0;

    public CloudCipherUtility() throws Exception {
        aonth = new AONTH();
        perm = new Permutation();
        g = new KeyGenerator();
        iv = new int[16];
    }

    public long[][] encrypt(int[] k1, int[] k2, int[] k3, int ctr, long[][] m) throws Exception {
        // (P1, P2, P3) = G(0, K1, K2, K3, l, n)
        int[][] p = g.generate(false, k1, k2, k3, m.length);

        // randomize 16 bytes iv
        SecureRandom random = new SecureRandom();
        byte[] tempRandom = new byte[16];
        random.nextBytes(tempRandom);
        for (int i = 0; i < 16; i++) {
            iv[i] = tempRandom[i] & 0xFF;
        }

        // m'[1]...m'[n + 1] = E-AONTH(ctr, m[1]...m[n])
        long[][] mAONTH = aonth.Eaonth(ctr, m);
        long[][] temp2 = new long[m.length][2];
        System.arraycopy(mAONTH, 0, temp2, 0, m.length);

        // m''[1]...m''[n] = PE (m'[1]...m'[n], P3)
        long[][] mPermuted = perm.permutationArray(temp2, p[2]);

        // PE (iv[1...l], P2)
        int[] permuted2 = perm.permutation(iv, p[1]);

        // PE (m'[n + 1][1...l], P1)
        int[] temp1 = new int[16];
        for (int i = 0; i < 2; i++) {
            int[] temp = new int[] { (int) ((mAONTH[m.length][i] >> 56) & 0xFF),
                    (int) ((mAONTH[m.length][i] >> 48) & 0xFF), (int) ((mAONTH[m.length][i] >> 40) & 0xFF),
                    (int) ((mAONTH[m.length][i] >> 32) & 0xFF), (int) ((mAONTH[m.length][i] >> 24) & 0xFF),
                    (int) ((mAONTH[m.length][i] >> 16) & 0xFF), (int) ((mAONTH[m.length][i] >> 8) & 0xFF),
                    (int) (mAONTH[m.length][i] & 0xFF) };
            System.arraycopy(temp, 0, temp1, i * 8, temp.length);
        }
        int[] permuted1 = perm.permutation(temp1, p[0]);

        // c[0] = PE (m'[n + 1][1...l], P1) xor PE (iv[1...l], P2)
        int[][] c = new int[m.length + 1][16];
        for (int i = 0; i < 16; i++) {
            c[0][i] = permuted1[i] ^ permuted2[i];
        }

        // convert m''[1...n] words into bytes for permutation
        int[][] mBytes = new int[m.length][16];
        for (int i = 0; i < mPermuted.length; i++) {
            for (int j = 0; j < 2; j++) {
                int[] temp = new int[] { (int) ((mPermuted[i][j] >> 56) & 0xFF), (int) ((mPermuted[i][j] >> 48) & 0xFF),
                        (int) ((mPermuted[i][j] >> 40) & 0xFF), (int) ((mPermuted[i][j] >> 32) & 0xFF),
                        (int) ((mPermuted[i][j] >> 24) & 0xFF), (int) ((mPermuted[i][j] >> 16) & 0xFF),
                        (int) ((mPermuted[i][j] >> 8) & 0xFF), (int) (mPermuted[i][j] & 0xFF) };
                System.arraycopy(temp, 0, mBytes[i], j * 8, temp.length);
            }
        }

        // c[i] = (PE (m[i][1...l], P1) xor PE (c[i − 1][1...l]), P2)
        for (int i = 0; i < m.length; i++) {
            permuted2 = perm.permutation(c[i], p[1]);
            permuted1 = perm.permutation(mBytes[i], p[0]);
            for (int j = 0; j < 16; j++) {
                c[i + 1][j] = permuted1[j] ^ permuted2[j];
            }
        }

        // convert c to array of long
        long[][] res = new long[m.length + 1][2];
        for (int i = 0; i <= m.length; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 8; k++) {
                    res[i][j] <<= 8;
                    res[i][j] |= (c[i][j * 8 + k] & 0xFF);
                }
            }
        }

        return res;
    }

    public long[][] decrypt(int[] k1, int[] k2, int[] k3, int ctr, long[][] c) throws Exception {
        // (P1, P2, P3) = G(0, K1, K2, K3, l, n)
        int[][] p = g.generate(false, k1, k2, k3, c.length - 1);

        // convert c into bytes for permutation
        int[][] cBytes = new int[c.length][16];
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < 2; j++) {
                int[] temp = new int[] { (int) ((c[i][j] >> 56) & 0xFF), (int) ((c[i][j] >> 48) & 0xFF),
                        (int) ((c[i][j] >> 40) & 0xFF), (int) ((c[i][j] >> 32) & 0xFF), (int) ((c[i][j] >> 24) & 0xFF),
                        (int) ((c[i][j] >> 16) & 0xFF), (int) ((c[i][j] >> 8) & 0xFF), (int) (c[i][j] & 0xFF) };
                System.arraycopy(temp, 0, cBytes[i], j * 8, temp.length);
            }
        }

        // m''[i] = DP (c[i] xor PE (c[i − 1][1...l], P2), P1)
        int[] x = new int[16];
        int[] y;
        int[][] mBytes = new int[cBytes.length - 1][16];
        for (int i = cBytes.length - 1; i > 0; i--) {
            y = perm.permutation(cBytes[i - 1], p[1]);
            for (int j = 0; j < 16; j++) {
                x[j] = cBytes[i][j] ^ y[j];
            }
            mBytes[i - 1] = perm.dePermutation(x, p[0]);
        }

        // m'[n + 1] = DP (c[0][1...l] xor PE (iv[1...l], P2), P1)
        int[] permuted2 = perm.permutation(iv, p[1]);
        int[] mAONTHBytes;
        int[] permuted1 = new int[16];
        for (int i = 0; i < 16; i++) {
            permuted1[i] = cBytes[0][i] ^ permuted2[i];
        }
        mAONTHBytes = perm.dePermutation(permuted1, p[0]);

        // convert mBytes into array of words
        long[][] mPermuted = new long[mBytes.length][2];
        for (int i = 0; i < mBytes.length; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 8; k++) {
                    mPermuted[i][j] <<= 8;
                    mPermuted[i][j] |= (mBytes[i][j * 8 + k] & 0xFF);
                }
            }
        }

        // m'[1]...m'[n] = DP (m''[1]...m''[n], P3)
        long[][] temp = perm.dePermutationArray(mPermuted, p[2]);

        long[][] mAONTH = new long[mBytes.length + 1][2];
        System.arraycopy(temp, 0, mAONTH, 0, temp.length);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                mAONTH[mBytes.length][i] <<= 8;
                mAONTH[mBytes.length][i] |= (mAONTHBytes[i * 8 + j] & 0xFF);
            }
        }

        // m[1]...m[n] = D-AONTH(ctr, m'[1]...m'[n + 1])
        return aonth.Daonth(ctr, mAONTH);
    }

    public long[][] reEncrypt(int[] ck1, int[] k2, int[] k2Random, int[] ck3, long[][] c) throws Exception {
        // P2 = PG (l, K2), P2' = PG(l, K2')
        long start = System.nanoTime();
        byte[] tempKey = new byte[16];
        byte[] tempKeyRandom = new byte[16];
        for (int i = 0; i < 16; i++) {
            tempKey[i] = (byte) (k2[i] & 0xFF);
            tempKeyRandom[i] = (byte) (k2Random[i] & 0xFF);
        }
        int[] p2 = perm.keyGenerator(16, tempKey);
        int[] p2Random = perm.keyGenerator(16, tempKeyRandom);
        long end = System.nanoTime();
        time1 += (end - start) / 100;

        start = System.nanoTime();
        int[][] cBytes = new int[c.length][16];
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < 2; j++) {
                int[] temp = new int[] { (int) ((c[i][j] >> 56) & 0xFF), (int) ((c[i][j] >> 48) & 0xFF),
                        (int) ((c[i][j] >> 40) & 0xFF), (int) ((c[i][j] >> 32) & 0xFF), (int) ((c[i][j] >> 24) & 0xFF),
                        (int) ((c[i][j] >> 16) & 0xFF), (int) ((c[i][j] >> 8) & 0xFF), (int) (c[i][j] & 0xFF) };
                System.arraycopy(temp, 0, cBytes[i], j * 8, temp.length);
            }
        }
        int[][] cPermutedBytes = new int[c.length - 1][16];
        int[] temp1;
        int[] temp2 = new int[16];
        // c'[i] = PE (c[i] xor PE (c[i − 1][1...l], P2), CK1)
        for (int i = cBytes.length - 1; i > 0; i--) {
            temp1 = perm.permutation(cBytes[i - 1], p2);
            for (int j = 0; j < 16; j++) {
                temp2[j] = cBytes[i][j] ^ temp1[j];
            }
            cPermutedBytes[i - 1] = perm.permutation(temp2, ck1);
        }
        end = System.nanoTime();
        time2 += (end - start) / 100;

        start = System.nanoTime();
        // c''[1]...c''[n] = PE (c'[1]...c'[n], CK3)
        long[][] cPermuted = new long[cPermutedBytes.length][2];
        for (int i = 0; i < cPermutedBytes.length; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 8; k++) {
                    cPermuted[i][j] <<= 8;
                    cPermuted[i][j] |= (cPermutedBytes[i][j * 8 + k] & 0xFF);
                }
            }
        }
        end = System.nanoTime();
        time3 += (end - start) / 100;

        start = System.nanoTime();
        long[][] cPermuted2 = perm.permutationArray(cPermuted, ck3);
        // c''[0] = (PE (c[0] xor PE (iv[1...l], P2), CK1)) xor (PE(iv[1...l], P2'))
        int[] ivPermuted = perm.permutation(iv, p2);
        int[] ivRandomPermuted = perm.permutation(iv, p2Random);
        int[] temp = new int[16];
        for (int i = 0; i < 16; i++) {
            temp[i] = ivPermuted[i] ^ cBytes[0][i];
        }
        temp = perm.permutation(temp, ck1);
        for (int i = 0; i < 16; i++) {
            temp[i] = temp[i] ^ ivRandomPermuted[i];
        }
        int[][] cAppend = new int[c.length][16];
        cAppend[0] = temp;
        end = System.nanoTime();
        time4 += (end - start) / 100;

        start = System.nanoTime();
        // c[i] = c''[i] xor PE(c''[i − 1][1...l], P2')
        for (int i = 0; i < c.length - 1; i++) {
            for (int j = 0; j < 2; j++) {
                int[] a = new int[] { (int) ((cPermuted2[i][j] >> 56) & 0xFF), (int) ((cPermuted2[i][j] >> 48) & 0xFF),
                        (int) ((cPermuted2[i][j] >> 40) & 0xFF), (int) ((cPermuted2[i][j] >> 32) & 0xFF),
                        (int) ((cPermuted2[i][j] >> 24) & 0xFF), (int) ((cPermuted2[i][j] >> 16) & 0xFF),
                        (int) ((cPermuted2[i][j] >> 8) & 0xFF), (int) (cPermuted2[i][j] & 0xFF) };
                System.arraycopy(a, 0, cAppend[i + 1], j * 8, a.length);
            }
        }

        for (int i = 0; i < c.length - 1; i++) {
            temp = perm.permutation(cAppend[i], p2Random);
            for (int j = 0; j < 16; j++) {
                cAppend[i + 1][j] = cAppend[i + 1][j] ^ temp[j];
            }
        }

        // convert c into array of long
        long[][] res = new long[c.length][2];
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 8; k++) {
                    res[i][j] <<= 8;
                    res[i][j] |= (cAppend[i][j * 8 + k] & 0xFF);
                }
            }
        }
        end = System.nanoTime();
        time5 += (end - start) / 100;

        return res;
    }
}
