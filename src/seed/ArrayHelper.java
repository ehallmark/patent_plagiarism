package seed;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArrayHelper {
	public static Set<Integer> intersection(List<Integer> list1, List<Integer> list2) {
		HashSet<Integer> toReturn = new HashSet<Integer>();
		for(int i = 0; i < Math.min(list1.size(), list2.size()); i++) {
			Integer num = list1.get(i);
			if(num.equals(list2.get(i))) toReturn.add(num);
		}
		return toReturn;
	}
	
	public static Set<Integer> union(List<Integer> list1, List<Integer> list2) {
		Set<Integer> toReturn = new HashSet<Integer>();
		toReturn.addAll(list1);
		toReturn.addAll(list2);
		return toReturn;
	}
	
	
	public static double similarity(List<Integer> list1, List<Integer> list2) {
		return (double)ArrayHelper.intersection(list1, list2).size()/ArrayHelper.union(list1, list2).size();
	}
	
}
