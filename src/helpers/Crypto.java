package helpers;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Base64;

public class Crypto {
	
	public static String saltHashString(String salt, String password) {
		return sha512(salt+password);
	}
	
	public static String sha512(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger no = new BigInteger(1, messageDigest);
			String hashtext = no.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String generateCSRF(int targetStringLength) {
		int leftLimit = 33;
		int rightLimit = 125;
		SecureRandom random = new SecureRandom();
		StringBuilder buffer = new StringBuilder(targetStringLength);
		for (int i = 0; i < targetStringLength; i++) {
			int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		String generatedString = buffer.toString();
		return Base64.getEncoder().encodeToString(generatedString.getBytes());
	}
	
	public static String createSalt(int length) {
		String session = "";
		try {
			SecureRandom secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG", "SUN");
			int[] ints = secureRandomGenerator.ints(length, 0, Helpers.getAllowedSessionChars().size()).toArray();
			for (int i = 0; i < ints.length; i++) {		
				session += Helpers.getAllowedSessionChars().get(ints[i]); 
			}
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return session;
	}
}
