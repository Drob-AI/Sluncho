package net.asteasolutions.cinusuidi.sluncho.bot.doc2vecClassifierUtils;

import lombok.NonNull;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * This is primitive seeker for nearest labels. It's used instead of basic
 * wordsNearest method because for ParagraphVectors only labels should be taken
 * into account, not individual words
 *
 * @author raver119@gmail.com
 */
public class LabelSeeker {
	private List<String> labelsUsed;
	private InMemoryLookupTable<VocabWord> lookupTable;

	public LabelSeeker(@NonNull List<String> labelsUsed, @NonNull InMemoryLookupTable<VocabWord> lookupTable) {
		if (labelsUsed.isEmpty())
			throw new IllegalStateException("You can't have 0 labels used for ParagraphVectors");
		this.lookupTable = lookupTable;
		this.labelsUsed = labelsUsed;
	}

	/**
	 * This method accepts vector, that represents any document, and returns
	 * distances between this document, and previously trained categories
	 * 
	 * @return
	 */
	public List<Pair<String, Double>> getScores(@NonNull INDArray vector) {
		List<Pair<String, Double>> result = new ArrayList<>();
		for (String label : labelsUsed) {
			INDArray vecLabel = lookupTable.vector(label);
			if (vecLabel == null)
				throw new IllegalStateException("Label '" + label + "' has no known vector!");

			double sim = Transforms.cosineSim(vector, vecLabel);
			result.add(new Pair<String, Double>(label, sim));
		}
		return result;
	}

	public Map<String, Double> getScoresAsMap(@NonNull INDArray vector) {
		Map<String, Double> result = new HashMap<>();
		for (String label : labelsUsed) {
			INDArray vecLabel = lookupTable.vector(label);
			if (vecLabel == null)
				throw new IllegalStateException("Label '" + label + "' has no known vector!");

			double sim = Transforms.cosineSim(vector, vecLabel);
			result.put(label, sim);
		}
		return result;
	}
	
	public Pair<String, Double> getMaxScore(@NonNull INDArray vector) {
		Pair<String, Double> result = null;
		// min cosine sum is -1
		double maxSum = -1;
		for (String label : labelsUsed) {
			INDArray vecLabel = lookupTable.vector(label);
			if (vecLabel == null)
				throw new IllegalStateException("Label '" + label + "' has no known vector!");

			double sum = Transforms.cosineSim(vector, vecLabel);
			if (maxSum < sum) {
				result = new Pair<String, Double>(label, sum);
				maxSum = sum;
			}
		}
		return result;
	}

	public List<Pair<String, Double>> getMaxNScores(@NonNull INDArray vector, Integer n) {
		Comparator<Pair<String, Double>> compare = new Comparator<Pair<String, Double>>() {
			public int compare(Pair<String, Double> first, Pair<String, Double> second) {
				return first.getSecond() > second.getSecond() ? -1 : 1;
			}
		};
		List<Pair<String, Double>> result = new ArrayList<>();
		PriorityQueue<Pair<String, Double>> heap = new PriorityQueue<Pair<String, Double>>(n, compare);
		
		for (String label : labelsUsed) {
			INDArray vecLabel = lookupTable.vector(label);
			if (vecLabel == null)
				throw new IllegalStateException("Label '" + label + "' has no known vector!");

			double sum = Transforms.cosineSim(vector, vecLabel);
			heap.add(new Pair<String, Double>(label, sum));
		}
		
		while(!heap.isEmpty() && n > 0) {
			result.add(heap.remove());
			n--;
		}
		
		return result;
	}

}
