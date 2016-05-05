package seed;

public class HashFunction {
	private int rand;
	public HashFunction(int seed) {
		this.rand = seed;
	};
	
	public int getHashCode(Object obj) {
		return obj.hashCode() ^ rand;
	}

}
