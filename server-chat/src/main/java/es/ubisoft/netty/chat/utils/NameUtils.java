package es.ubisoft.netty.chat.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 
 * This singleton class is used to generate simple random nick names based on a
 * random combination of alphanumeric values and store all nick names that are
 * in use
 * 
 */
public class NameUtils {

	private final String ALPHANUMERIC_VALUES = "abcdefghijklmnñopqrstuvwxyz1234567890";
	private static NameUtils nameUtils;
	private Random randSeed;
	private static Set<String> nickNames;

	private NameUtils() {
		nickNames = new HashSet<>();
	}

	public static NameUtils geSingletonInstance() {
		return nameUtils == null ? nameUtils = new NameUtils() : nameUtils;
	}

	public String generateRandomNickName() {
		StringBuilder nickName = new StringBuilder();
		this.randSeed = new Random();
		while (nickName.toString().length() == 0) {
			int length = this.randSeed.nextInt(5) + 5;
			for (int i = 0; i < length; i++) {
				nickName.append(
						this.ALPHANUMERIC_VALUES.charAt(this.randSeed.nextInt(this.ALPHANUMERIC_VALUES.length())));
			}
			if (nickNames.contains(nickName.toString())) {
				nickName = new StringBuilder();
			}
		}

		nickNames.add(nickName.toString());
		return nickName.toString();
	}

	public Set<String> getNickNames() {
		return nickNames;
	}
	
}
