package com.cloudcipher.cloudcipher_client.utility.CloudCipher;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Permutation {
	QuickSort sorter;
	Cipher eCipher;

	public Permutation() throws Exception {
		this.sorter = new QuickSort();
		eCipher = Cipher.getInstance("AES/ECB/NoPadding");
	}

	int[] keyGenerator(int n, byte[] key) throws Exception {
		int[] p = new int[n];
		int[][] tmp = new int[n][16];
		byte[] pForAES = new byte[n * 16];
		for (int i = 0; i < n; i++) {
			// p[i] = i
			p[i] = i;
			byte[] indexBytes = { (byte) ((i >>> 24) & 0xFF), (byte) ((i >>> 16) & 0xFF), (byte) ((i >>> 8) & 0xFF),
					(byte) (i & 0xFF) };
			System.arraycopy(indexBytes, 0, pForAES, i * 16, 4);
		}

		// encrypt i
		SecretKey secretKey3 = new SecretKeySpec(key, "AES");
		eCipher.init(Cipher.ENCRYPT_MODE, secretKey3);
		byte[] result = eCipher.doFinal(pForAES);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 16; j++) {
				tmp[i][j] = result[i*16+j] & 0xFF;
			}
		}

		// sort p & tmp
		sorter.sort(p, tmp, 0, n - 1);
		return p;
	}

	int[] findConversionKey(int[] pa, int[] pb) {
		int length = pa.length;
		int[] pc = new int[length];
		int[] indexPA = new int[length];
		int[] indexPB = new int[length];
		for (int i = 0; i < length; i++) {
			indexPA[i] = i;
			indexPB[i] = i;
		}
		sorter.sortOneArray(pa, indexPA, 0, length - 1);
		sorter.sortOneArray(pb, indexPB, 0, length - 1);

		for (int i = 0; i < length; i++) {
			pc[indexPB[i]] = indexPA[i];
		}
		return pc;
	}

	long[][] permutationArray(long[][] x, int[] p) {
		long[][] res = new long[x.length][2];
		for (int i = 0; i < x.length; i++) {
			res[i] = x[p[i]];
		}
		return res;

	}

	int[] permutation(int[] x, int[] p) {
		int[] res = new int[x.length];
		for (int i = 0; i < x.length; i++) {
			res[i] = x[p[i]];
		}
		return res;
	}

	long[][] dePermutationArray(long[][] x, int[] p) {
		long[][] res = new long[x.length][2];
		for (int i = 0; i < x.length; i++) {
			res[p[i]] = x[i];
		}
		return res;
	}

	int[] dePermutation(int[] x, int[] p) {
		int[] res = new int[x.length];
		for (int i = 0; i < x.length; i++) {
			res[p[i]] = x[i];
		}
		return res;
	}
}
