package seed;

public class TechnologyResult {
	private String name;
	private double similarity;

	public TechnologyResult(String name, double similarity) {
		this.name = name;
		this.similarity = similarity;
	}

	public String getSimilarity() {
		return "%" + (int) (similarity);
	}

	public String getName() {
		return name;
	}

}
