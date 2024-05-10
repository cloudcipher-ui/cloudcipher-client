package com.cloudcipher.cloudcipher_client_v2.utility;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AONTH {
	Sha256 sha;
	Cipher eCipher;

	public AONTH() throws Exception {
		sha = new Sha256();
		eCipher = Cipher.getInstance("AES/ECB/NoPadding");
	}

	long[][] Eaonth(int ctr, long[][] m) throws Exception {
		// K = random(l)
		SecureRandom random = new SecureRandom();
		byte[] tempRandom = new byte[16];
		random.nextBytes(tempRandom);

		long[][] x = new long[m.length][2];
		long[][] res = new long[m.length + 1][2];
		long[] encrypted = new long[2];
		SecretKey secretKey3 = new SecretKeySpec(tempRandom, "AES");
		eCipher.init(Cipher.ENCRYPT_MODE, secretKey3);
		for (int i = 0; i < m.length; i++) {
			// FK' (ctr + i)
			byte[] indexBytes = { (byte) ((i >>> 24) & 0xFF), (byte) ((i >>> 16) & 0xFF), (byte) ((i >>> 8) & 0xFF),
					(byte) (i & 0xFF), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			byte[] result = eCipher.doFinal(indexBytes);
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 8; k++) {
					encrypted[j] <<= 8;
					encrypted[j] |= (result[j * 8 + k] & 0xFF);
				}
			}

			// x[i] = m[i] xor FK' (ctr + i)
			for (int j = 0; j < 2; j++) {
				x[i][j] = (m[i][j] ^ encrypted[j]);
			}
		}
		// H(x[1]...x[n])
		int[] xBytes = new int[x.length * 16];
		for (int i = 0; i < x.length * 2; i++) {
			int[] temp2 = new int[] { (int) ((x[i / 2][i % 2] >> 56) & 0xFF), (int) ((x[i / 2][i % 2] >> 48) & 0xFF),
					(int) ((x[i / 2][i % 2] >> 40) & 0xFF), (int) ((x[i / 2][i % 2] >> 32) & 0xFF),
					(int) ((x[i / 2][i % 2] >> 24) & 0xFF), (int) ((x[i / 2][i % 2] >> 16) & 0xFF),
					(int) ((x[i / 2][i % 2] >> 8) & 0xFF), (int) (x[i / 2][i % 2] & 0xFF) };
			System.arraycopy(temp2, 0, xBytes, i * 8, temp2.length);
		}

		int[] digestBytes = sha.hash(xBytes);
		long[] digest = new long[4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 8; j++) {
				digest[i] <<= 8;
				digest[i] |= (digestBytes[j + (i * 8)] & 0xFF);
			}
		}

		// m'[n + 1] = K xor H(x[1]...x[n])
		for (int i = 0; i < 2; i++) {
			long keyDecs = 0L;
			for (int j = 0; j < 8; j++) {
				keyDecs <<= 8;
				keyDecs |= (tempRandom[j + (i * 8)] & 0xFF);
			}
			res[m.length][i] = digest[i] ^ keyDecs;
		}

		for (int i = 0; i < m.length; i++) {
			// m'[n + 1] xor (ctr + i)
			long[] temp = new long[2];
			System.arraycopy(res[m.length], 0, temp, 0, 2);
			temp[1] = res[m.length][1] ^ (ctr + i + 1);

			int[] tempBytes = new int[16];
			for (int j = 0; j < 2; j++) {
				int[] temp2 = new int[] { (int) ((temp[j] >> 56) & 0xFF), (int) ((temp[j] >> 48) & 0xFF),
						(int) ((temp[j] >> 40) & 0xFF), (int) ((temp[j] >> 32) & 0xFF), (int) ((temp[j] >> 24) & 0xFF),
						(int) ((temp[j] >> 16) & 0xFF), (int) ((temp[j] >> 8) & 0xFF), (int) (temp[j] & 0xFF) };
				System.arraycopy(temp2, 0, tempBytes, j * 8, temp2.length);
			}

			// H(m'[n + 1] xor (ctr + i))
			digestBytes = sha.hash(tempBytes);
			digest = new long[4];
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 8; k++) {
					digest[j] <<= 8;
					digest[j] |= (digestBytes[k + (j * 8)] & 0xFF);
				}
			}

			// m'[i] = x[i] xor H(m'[n + 1] xor (ctr + i))
			for (int j = 0; j < 2; j++) {
				res[i][j] = x[i][j] ^ digest[j];
			}
		}
		return res;
	}

	long[][] Daonth(int ctr, long[][] m) throws Exception {
		long[][] x = new long[m.length - 1][2];
		for (int i = 0; i < m.length - 1; i++) {
			// m'[n + 1] xor (ctr + i)
			long[] temp = new long[2];
			System.arraycopy(m[m.length - 1], 0, temp, 0, 2);
			temp[1] = m[m.length - 1][1] ^ (ctr + i + 1);

			// H(m'[n + 1] xor (ctr + i))
			int[] tempBytes = new int[16];
			for (int j = 0; j < 2; j++) {
				int[] temp2 = new int[] { (int) ((temp[j] >> 56) & 0xFF), (int) ((temp[j] >> 48) & 0xFF),
						(int) ((temp[j] >> 40) & 0xFF), (int) ((temp[j] >> 32) & 0xFF), (int) ((temp[j] >> 24) & 0xFF),
						(int) ((temp[j] >> 16) & 0xFF), (int) ((temp[j] >> 8) & 0xFF), (int) (temp[j] & 0xFF) };
				System.arraycopy(temp2, 0, tempBytes, j * 8, temp2.length);
			}

			int[] digestBytes = sha.hash(tempBytes);
			long[] digest = new long[4];
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 8; k++) {
					digest[j] <<= 8;
					digest[j] |= (digestBytes[k + (j * 8)] & 0xFF);
				}
			}

			// x[i] = m'[i] xor H(m'[n + 1] xor (ctr + i))
			for (int j = 0; j < 2; j++) {
				x[i][j] = m[i][j] ^ digest[j];
			}
		}

		// H(x[1]...x[n])
		int[] xBytes = new int[x.length * 16];
		for (int i = 0; i < x.length * 2; i++) {
			int[] temp2 = new int[] { (int) ((x[i / 2][i % 2] >> 56) & 0xFF), (int) ((x[i / 2][i % 2] >> 48) & 0xFF),
					(int) ((x[i / 2][i % 2] >> 40) & 0xFF), (int) ((x[i / 2][i % 2] >> 32) & 0xFF),
					(int) ((x[i / 2][i % 2] >> 24) & 0xFF), (int) ((x[i / 2][i % 2] >> 16) & 0xFF),
					(int) ((x[i / 2][i % 2] >> 8) & 0xFF), (int) (x[i / 2][i % 2] & 0xFF) };
			System.arraycopy(temp2, 0, xBytes, i * 8, temp2.length);
		}

		int[] digestBytes = sha.hash(xBytes);
		long[] digest = new long[4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 8; j++) {
				digest[i] <<= 8;
				digest[i] |= (digestBytes[j + (i * 8)] & 0xFF);
			}
		}

		// K = m'[n + 1] xor H(x[1]...x[n])
		byte[] key = new byte[16];
		long[] keyDecs = new long[2];
		for (int i = 0; i < 2; i++) {
			keyDecs[i] = m[m.length - 1][i] ^ digest[i];
		}
		for (int i = 0; i < 2; i++) {
			byte[] temp2 = { (byte) ((keyDecs[i] >> 56) & 0xFF), (byte) ((keyDecs[i] >> 48) & 0xFF),
					(byte) ((keyDecs[i] >> 40) & 0xFF), (byte) ((keyDecs[i] >> 32) & 0xFF),
					(byte) ((keyDecs[i] >> 24) & 0xFF), (byte) ((keyDecs[i] >> 16) & 0xFF),
					(byte) ((keyDecs[i] >> 8) & 0xFF), (byte) (keyDecs[i] & 0xFF) };
			System.arraycopy(temp2, 0, key, i * 8, temp2.length);
		}

		long[][] res = new long[m.length - 1][2];
		long[] encrypted = new long[2];
		SecretKey secretKey3 = new SecretKeySpec(key, "AES");
		eCipher.init(Cipher.ENCRYPT_MODE, secretKey3);
		for (int i = 0; i < m.length - 1; i++) {
			// FK' (ctr + i)
			byte[] indexBytes = { (byte) ((i >>> 24) & 0xFF), (byte) ((i >>> 16) & 0xFF), (byte) ((i >>> 8) & 0xFF),
					(byte) (i & 0xFF), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			byte[] result = eCipher.doFinal(indexBytes);
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 8; k++) {
					encrypted[j] <<= 8;
					encrypted[j] |= (result[j * 8 + k] & 0xFF);
				}
			}

			// m[i] = x[i] xor FK' (ctr + i)
			for (int j = 0; j < 2; j++) {
				res[i][j] = (encrypted[j] ^ x[i][j]);
			}
		}
		return res;
	}
}
