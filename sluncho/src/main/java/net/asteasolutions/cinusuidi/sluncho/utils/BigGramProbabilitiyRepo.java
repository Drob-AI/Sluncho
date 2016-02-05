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

public final class BigGramProbabilitiyRepo {

  private static void extractBigramsInfo(HashMap<String, Integer> bigramProbabilities, BigramsRow next) {
    String bigramKey = next.generateBigramKey();
    String bigramTypeKey = next.generateBigramTypeKey();

    Integer bigramCount = bigramProbabilities.get(bigramKey);
    if(bigramCount == null) {
      bigramProbabilities.put(bigramKey, next.frequency);
    } else {
      bigramProbabilities.put(bigramKey,  bigramCount + next.frequency);
    }

    if(foundBigramTypes.get(bigramTypeKey) == null) {
      allBigramTypes++;
      foundBigramTypes.put(bigramTypeKey, true);
    }
  }

  public static Integer allBigrams = 1;
  public static Integer allBigramTypes = 0;
  private static HashMap<String, Boolean> foundBigramTypes = new HashMap<>();

  private static String fileForSave = "w2probability";
  private static HashMap<String, Integer> biGramFrequency = new HashMap<String, Integer>();
  private static HashMap<String, BigDecimal> biGramProbability = new HashMap<String, BigDecimal>();

  public static void loadProbability() {

		try {
			@SuppressWarnings("resource")
			Scanner in = new Scanner(new FileReader(fileForSave + ".int"));
			allBigrams = in.nextInt();
			allBigramTypes = in.nextInt();

			FileInputStream fis = new FileInputStream(fileForSave + ".hash");
		    @SuppressWarnings("resource")
			ObjectInputStream ois = new ObjectInputStream(fis);

		    biGramProbability = (HashMap<String, BigDecimal>) ois.readObject();
		} catch (FileNotFoundException e) {
			train();
			//e.printStackTrace();
		} catch (IOException e) {
			train();
			//e.printStackTrace();
		} catch (ClassNotFoundException e) {
			train();
			//e.printStackTrace();
		}
	}

  private static void save() {

	FileOutputStream fos;
	try {
		@SuppressWarnings("resource")
		Writer wr = new FileWriter(fileForSave + ".int");
		wr.write(allBigrams.toString());
		wr.write(" ");
		wr.write(allBigramTypes.toString());
		wr.close();

		fos = new FileOutputStream(fileForSave + ".hash");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(biGramProbability);
		oos.close();

	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
  }

  public static void train() {
    File file = new File("w2c.txt");
    BufferedReader reader = null;
    ArrayList<BigramsRow> bigrams = new ArrayList<>();
    try {
        reader = new BufferedReader(new FileReader(file));
        String text = null;
        //check types here http://ucrel.lancs.ac.uk/claws7tags.html

        while ((text = reader.readLine()) != null) {
          @SuppressWarnings("resource")
          Scanner in = new Scanner(text);
          BigramsRow next = new BigramsRow(in);
          bigrams.add(next);

          allBigrams += next.frequency;
          extractBigramsInfo(biGramFrequency, next);
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
			Iterator<?> it = biGramFrequency.entrySet().iterator();
		    while (it.hasNext()) {
		    	Entry<?, ?> pair = (Entry<?, ?>) it.next();
		    	Integer freq = biGramFrequency.get(pair.getKey());
		    	biGramProbability.put((String) pair.getKey(), new BigDecimal(freq).divide(new BigDecimal(allBigrams + 1), 100, RoundingMode.HALF_UP));
		    }

		    BigGramProbabilitiyRepo.save();

            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
        }
    }
  }

  public static BigDecimal probabilityFor(String[] firstWordWithType, String[] secondWordWithType) {
	  String key = BigramsRow.generateBigramKey(firstWordWithType, secondWordWithType);
	  BigDecimal bigramProb = biGramProbability.get(key);
	  if(bigramProb == null) {
		  bigramProb = new BigDecimal(allBigramTypes).divide(new BigDecimal(allBigrams + allBigramTypes), 100, RoundingMode.HALF_UP);
	  }

    return bigramProb;
  }

}

