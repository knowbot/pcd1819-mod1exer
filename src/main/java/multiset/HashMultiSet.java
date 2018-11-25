package multiset;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * <p>A baseMap models a data structure containing elements along with their frequency count i.e., </p>
 * <p>the number of times an element is present in the set.</p>
 * <p>HashMultiSet is a Map-based concrete implementation of the baseMap concept.</p>
 * 
 * <p>baseMap a = <{1:2}, {2:2}, {3:4}, {10:1}></p>
 * */

	/**
	 * <p>Due to practical reasons (frequency of objects in the set always being a number), I deemed</p>
	 * <p>reasonable to assume that Generic type V extends Number; this way it is always possible to instantiate</p>
	 * <p>a HashMultiSet using Integer, Long, Short, etc. as type for the frequency, but instantiation attempts</p>
	 * <p>with "wrong" types are prevented. Moreover it becomes easier to implement methods such as addElement, as</p>
	 * <P>I can safely increment the frequency counter by adding 1 to it.</P>
	 */

public final class HashMultiSet<T, V extends Number> {
	/**
	 *XXX: data structure backing this baseMap implementation.
	 */
	private final Map<T,V> baseMap;
	/**
	 * Sole constructor of the class.
	 **/
	public HashMultiSet() {
		baseMap = new HashMap<>();
	};
	
	
	/**
	 * If not present, adds the element to the data structure, otherwise 
	 * simply increments its frequency.
	 * 
	 * @param t T: element to include in the multiset
	 *
	 * @return V: frequency count of the element in the multiset
	 * */
	public V addElement(T t) {
		Number frequency = baseMap.get(t);
		if (frequency == null)
			baseMap.put(t, (V) (Number) 1);
		else {
			frequency = new BigInteger(frequency.toString()).add(new BigInteger("1"));
			baseMap.put(t, (V) frequency);
		}
		return baseMap.get(t);
	}

	/**
	 * Check whether the elements is present in the multiset.
	 * 
	 * @param t T: element
	 * 
	 * @return boolean: true if the element is present, false otherwise.
	 * */	
	public boolean isPresent(T t) {
		return baseMap.containsKey(t);
	}
	
	/**
	 * @param t T: element
	 * @return V: frequency count of parameter t ('0' if not present)
	 * */
	public V getElementFrequency(T t) {
		if (isPresent(t))
			return baseMap.get(t);
		else
			return (V) (Number) 0;
	}
	
	
	/**
	 * Builds a multiset from a source data file. The source data file contains
	 * a number comma separated elements. 
	 * Example_1: ab,ab,ba,ba,ac,ac -->  <{ab:2},{ba:2},{ac:2}>
	 * Example 2: 1,2,4,3,1,3,4,7 --> <{1:2},{2:1},{3:2},{4:2},{7:1}>
	 * 
	 * @param source Path: source of the multiset
	 * */
	public void buildFromFile(Path source) throws IOException, IllegalArgumentException {
		if (source == null || Files.notExists(source))
			throw new IllegalArgumentException("File not found. Please check for invalid or empty path.");
		try(Stream<String> fileStream = Files.lines(source)) {
			fileStream.forEach(line -> {
				String[] items = line.split(",");
				for (String item : items)
					addElement((T)item);
			});
		} catch (IOException e) {
			System.out.println("Unable to read file at given path, aborting operation...");
		}
	}

	/**
	 * Same as before with the difference being the source type.
	 * @param source List<T>: source of the multiset
	 * */
	public void buildFromCollection(List<? extends T> source) {
		source.stream()
				.forEach(item -> addElement(item));
	}
	
	/**
	 * Produces a linearized, unordered version of the baseMap data structure.
	 * Example: <{1:2},{2:1}, {3:3}> -> 1 1 2 3 3 3 3
	 * 
	 * @return List<T>: linearized version of the multiset represented by this object.
	 */
	public List<T> linearize() {
		List<T> linearizedList = new ArrayList<>();
		baseMap.forEach((item, freq) -> {
			BigInteger counter = new BigInteger(freq.toString());
			while (counter.compareTo(new BigInteger("0")) == 1) {
				linearizedList.add(item);
				counter.subtract(new BigInteger("1"));
			}
		});
		return linearizedList;
	}


	public Map<T, V> getBaseMap() {
		return baseMap;
	}
}
