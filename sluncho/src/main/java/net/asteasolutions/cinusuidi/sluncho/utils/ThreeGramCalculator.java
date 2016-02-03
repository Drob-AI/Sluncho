package net.asteasolutions.cinusuidi.sluncho.utils;

import java.math.BigDecimal;
import java.math.MathContext;

public class ThreeGramCalculator {
	
	public static BigDecimal calculate(String[][] words){
		BigDecimal sum = ThreeGramProbabilityRepo.getWordProbability(words[0]);
		sum = sum.multiply(BigGramProbabilitiyRepo.probabilityFor(words[0], words[1]));

		for(int i = 2; i < words.length; i++) {
			String[][] prevWords = {words[i - 2], words[i -1]};
			sum = sum.multiply(ThreeGramProbabilityRepo.reletiveProbability(prevWords, words[i]));
		}
		return sum;
	}

}