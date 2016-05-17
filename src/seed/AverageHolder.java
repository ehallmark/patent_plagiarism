package seed;

public class AverageHolder implements Comparable<AverageHolder> {
	private Integer count;
	private static Integer total = 0; 
	
	public AverageHolder(int count) {
		this.count = count;
	}
	
	public double getAverage() {
		return ((double)count)/total;
	}
	
	public void add(int count) {
		this.count+=count;
	}
	
	public static void addToTotal(int total) {
		AverageHolder.total+=total;
	}

	@Override
	public int compareTo(AverageHolder o) {
		return count.compareTo(o.count);
	}
}
