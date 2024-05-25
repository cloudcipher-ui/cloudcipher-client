package com.cloudcipher.cloudcipher_client.utility.CloudCipher;

import java.security.SecureRandom;

public class KeyGenerator {
	Permutation perm;

	public KeyGenerator() throws Exception{
		perm = new Permutation();
	}

	int[][] generate(boolean g, int[] k1, int[] k2, int[] k3, int n) throws Exception {
		byte[] tempRandom1 = new byte[16];
		byte[] tempRandom2 = new byte[16];
		byte[] tempRandom3 = new byte[16];
		if (g) {
			SecureRandom random = new SecureRandom();
			random.nextBytes(tempRandom1);
			random.nextBytes(tempRandom2);
			random.nextBytes(tempRandom3);
			
			for (int i = 0; i < 16; i++) {
				k1[i] = tempRandom1[i] & 0xFF;
				k2[i] = tempRandom2[i] & 0xFF;
				k3[i] = tempRandom3[i] & 0xFF;
			}
			
			int[][] p = new int[6][16];
			
			p[0] = perm.keyGenerator(16, tempRandom1);
			p[2] = perm.keyGenerator(n, tempRandom3);
			p[3] = k1;
			p[4] = k2;
			p[5] = k3;
			return p;
		}
		
		int[][] p = new int[3][16];
		for (int i = 0; i < 16; i++) {
			tempRandom1[i] = (byte) (k1[i] & 0xFF);
			tempRandom2[i] = (byte) (k2[i] & 0xFF);
			tempRandom3[i] = (byte) (k3[i] & 0xFF);
		}
		
		p[0] = perm.keyGenerator(16, tempRandom1);
		p[1] = perm.keyGenerator(16, tempRandom2);
		p[2] = perm.keyGenerator(n, tempRandom3);
		return p;
	}

	public int[][] reGenerate(int[] k1, int[] k2, int[] k3, int n) throws Exception {
		// (P1, P2, P3)
		int[][] pa = generate(false, k1, k2, k3, n);
		int[] zeros1 = new int[16];
		int[] zeros2 = new int[16];
		int[] zeros3 = new int[16];
		
		// (P1', P2', P3')
		int[][] pb = generate(true, zeros1, zeros2, zeros3, n);
		
		int[] ck1 = perm.findConversionKey(pa[0], pb[0]);
		int[] ck3 = perm.findConversionKey(pa[2], pb[2]);
		
		int[][] res = new int[6][];
		res[0] = ck1;
		res[1] = k2;
		res[2] = pb[4];
		res[3] = ck3;
		res[4] = pb[3];
		res[5] = pb[5];
		return res;
	}
}
