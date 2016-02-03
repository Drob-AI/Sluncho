package net.asteasolutions.cinusuidi.sluncho.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

public class ThreeGramProbabilityRepo {

	private static void extractThreeGramInfo(HashMap<String, Integer> threeGramProbabilities, ThreeGramRows next) {
		String threeGramKey = next.generateThreeGramKey();
		Integer threeGramCount = threeGramProbabilities.get(threeGramKey);
		if (threeGramCount == null) {
			threeGramProbabilities.put(threeGramKey, next.frequency);
		} else {
			threeGramProbabilities.put(threeGramKey, threeGramCount + next.frequency);
		}

	}

	private static void extratWordsInfo(HashMap<String, Integer> wordCounter, ThreeGramRows next) {
		String key = next.getFirstWord() + next.getFirstWordType().charAt(0);
		Integer wCount = wordCounter.get(key);
		if (wCount == null) {
			wordCounter.put(key, next.getFrequency());
		} else {
			wordCounter.put(key, wCount + next.getFrequency());
		}
	}

	private static Integer allThreeGrams = 1;

	public static BigDecimal getWordProbability(String[] word) {
		Integer wCount = ThreeGramProbabilityRepo.wordsCount.get(word[0] + word[1]);
		if (wCount == null) {
			wCount = 0;
		}

		wCount += 1;
		return new BigDecimal(wCount / (double) ThreeGramProbabilityRepo.allThreeGrams);
	}

	private static String fileForSave = "w3probability";
	private static HashMap<String, Integer> threeGramFrequency = new HashMap<String, Integer>();

	public static HashMap<String, Integer> wordsCount = new HashMap<>();
	public static HashMap<String, BigDecimal> threeGramProbability = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static void loadProbability() {
		
		BigGramProbabilitiyRepo.loadProbability();
		try {
			@SuppressWarnings("resource")
			Scanner in = new Scanner(new FileReader(fileForSave + ".int"));
			allThreeGrams = in.nextInt();

			FileInputStream fis = new FileInputStream(fileForSave + ".hash");
			@SuppressWarnings("resource")
			ObjectInputStream ois = new ObjectInputStream(fis);

			threeGramProbability = (HashMap<String, BigDecimal>) ois.readObject();

			fis = new FileInputStream(fileForSave + ".wcount");
			@SuppressWarnings("resource")
			ObjectInputStream oisWcount = new ObjectInputStream(fis);

			wordsCount = (HashMap<String, Integer>) oisWcount.readObject();

		} catch (FileNotFoundException e) {
			train();
			// e.printStackTrace();
		} catch (IOException e) {
			train();
			// e.printStackTrace();
		} catch (ClassNotFoundException e) {
			train();
			// e.printStackTrace();
		}
	}

	private static void save() {

		FileOutputStream fos;
		try {
			@SuppressWarnings("resource")
			Writer wr = new FileWriter(fileForSave + ".int");
			wr.write(new Integer(allThreeGrams).toString());
			wr.close();

			fos = new FileOutputStream(fileForSave + ".hash");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(threeGramProbability);
			oos.close();

			fos = new FileOutputStream(fileForSave + ".wcount");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(wordsCount);
			oos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void train() {
		File file = new File("w3c.txt");
		BufferedReader reader = null;
		ArrayList<ThreeGramRows> threeGrams = new ArrayList<>();
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			// check types here http://ucrel.lancs.ac.uk/claws7tags.html
			while ((text = reader.readLine()) != null) {
				@SuppressWarnings("resource")
				Scanner in = new Scanner(text);
				ThreeGramRows next = new ThreeGramRows(in);
				threeGrams.add(next);
				extratWordsInfo(wordsCount, next);
				allThreeGrams += next.frequency;
				extractThreeGramInfo(threeGramFrequency, next);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Iterator<Entry<String, Integer>> it = threeGramFrequency.entrySet().iterator();
			while (it.hasNext()) {
				Entry<?, ?> pair = (Entry<?, ?>) it.next();
				Integer freq = threeGramFrequency.get(pair.getKey());
				threeGramProbability.put((String) pair.getKey(),
						new BigDecimal(freq).divide(new BigDecimal(allThreeGrams + 1), 100, RoundingMode.HALF_UP));
			}

			ThreeGramProbabilityRepo.save();
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public static BigDecimal possibilityFor(String[] firstWordWithType, String[] secondWordWithType,
			String[] thridWordWithType) {
		BigDecimal threeGramProb = threeGramProbability
				.get(ThreeGramRows.generateThreeGramKey(firstWordWithType, secondWordWithType, thridWordWithType));

		if (threeGramProb == null) {
			threeGramProb = new BigDecimal(1).divide(new BigDecimal(allThreeGrams + 1), 100, RoundingMode.HALF_UP);
		}

		return threeGramProb;
	}

	public static BigDecimal reletiveProbability(String[][] ifBothOtherWords, String[] wordToHappen) {
		BigDecimal probForThisThreeGram = possibilityFor(ifBothOtherWords[0], ifBothOtherWords[1], wordToHappen);

		BigDecimal probForThisBigram = BigGramProbabilitiyRepo.probabilityFor(ifBothOtherWords[0], ifBothOtherWords[1]);

		if (threeGramProbability.get(
				ThreeGramRows.generateThreeGramKey(ifBothOtherWords[0], ifBothOtherWords[1], wordToHappen)) == null) {
			return probForThisBigram;
		} else {
			return probForThisThreeGram.divide(probForThisBigram, 100, RoundingMode.HALF_UP);
		}
	}
}