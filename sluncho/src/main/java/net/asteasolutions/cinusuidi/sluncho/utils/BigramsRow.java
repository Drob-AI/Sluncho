package net.asteasolutions.cinusuidi.sluncho.utils;

import java.util.Scanner;

public class BigramsRow {
	String firstWord;
	String secondWord;
	String firstWordType;
	String secondWordType;
	Integer frequency;

	public BigramsRow(Integer frequency, String firstWord, String secondWord, String firstWordType,
			String secondWordType) {
		this.firstWord = firstWord;
		this.secondWord = secondWord;
		this.firstWordType = firstWordType;
		this.secondWordType = secondWordType;
		this.frequency = frequency;
	}

	public BigramsRow(Scanner in) {
		this.frequency  = in.nextInt();
		this.firstWord = in.next().toLowerCase();
		this.secondWord = in.next().toLowerCase();
		this.firstWordType = in.next().toLowerCase();
		this.secondWordType = in.next().toLowerCase();
	}

	public void print() {
		System.out.println(this.firstWord + " " + this.secondWord +  " "
				+ this.firstWordType + " " + this.secondWordType + " " + this.frequency);
	}

//	private String generateWordTypeProbabilityKey(String word, String type) {
//		return word + type.charAt(0);
//	}

//	public String generateFirstWordTypeProbabilityKey() {
//		return 	generateWordTypeProbabilityKey(this.firstWord, this.firstWordType);
//	}
//
//	public String generateSecondWordTypeProbabilityKey() {
//		return  generateWordTypeProbabilityKey(this.secondWord, this.secondWordType);
//	}

	public String generateBigramKey() {
		String[] firstWordWithType = {this.firstWord , this.firstWordType.substring(0, 1)};
		String[] secondWordWithType = {this.secondWord , this.secondWordType.substring(0, 1)};
		return generateBigramKey(firstWordWithType, secondWordWithType);
	}

	public String generateBigramTypeKey() {
		return this.firstWordType + this.secondWordType;
	}

	public static String generateBigramKey(String[] firstWordWithType, String[] secondWordWithType) {
		return firstWordWithType[0] + firstWordWithType[1].charAt(0) + secondWordWithType[0] + secondWordWithType[1].charAt(0);
	}


}
