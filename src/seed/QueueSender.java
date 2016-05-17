package seed;

public class QueueSender {
	String arg1;
	String arg2;
	String arg3;
	Integer int1;
	Integer int2;
	
	public QueueSender (String s1, String s2, String s3) {
		arg1 = s1;
		arg2 = s2;
		arg3 = s3;
	}
	
	public QueueSender (String s1, String s2, Integer i3, Integer i4) {
		arg1 = s1;
		arg2 = s2;
		int1 = i3;
		int2 = i4;
	}
	
}
