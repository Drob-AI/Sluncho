package net.asteasolutions.cinusuidi.sluncho.utils;

import java.util.Scanner;

public class ThreeGramRows {
	String firstWord;
	String secondWord;
	String thirdWord;
	String firstWordType;
	String secondWordType;
	String thirdWordType;
	Integer frequency;

	public String getFirstWord() {
		return firstWord;
	}

	public String getFirstWordType() {
		return firstWordType;
	}

	public Integer getFrequency() {
		return frequency;
	}

	public ThreeGramRows(Scanner in) {
		this.frequency  = in.nextInt();
		this.firstWord = in.next().toLowerCase();
		this.secondWord = in.next().toLowerCase();
		this.thirdWord = in.next().toLowerCase();
		this.firstWordType = in.next().toLowerCase();
		this.secondWordType = in.next().toLowerCase();
		this.thirdWordType = in.next().toLowerCase();
	}

	public static String generateThreeGramKey(String[] firstWordWithType, String[] secondWordWithType, String[] thirdWordWithType) {
		return firstWordWithType[0] + firstWordWithType[1].charAt(0) + secondWordWithType[0] + secondWordWithType[1].charAt(0) + thirdWordWithType[0] + thirdWordWithType[1].charAt(0);
	}

	public String generateThreeGramTypeKey() {
		return this.firstWordType.substring(0, 1) + this.secondWordType.substring(0, 1) + this.thirdWordType.substring(0, 1);
	}

	public String generateThreeGramKey() {
		String[] firstWordWithType = {this.firstWord , this.firstWordType.substring(0, 1)};
		String[] secondWordWithType = {this.secondWord , this.secondWordType.substring(0, 1)};
		String[] thirdWordWithType = {this.thirdWord , this.thirdWordType.substring(0, 1)};

		return generateThreeGramKey(firstWordWithType, secondWordWithType, thirdWordWithType);
	}

}
