package test;

public class Lottery {

	public int calculateResult(int whiteBalls[], int powerBall) {
		return 0;
	}

	public static void main(String[] args) {
		
		int whiteBalls[] = new int[5];
		int powerBall = 0;
		
		if (args.length == 6) {
			for(int i=0;i<5;i++) {
				whiteBalls[i] = Integer.parseInt(args[i]);
			}
			
			powerBall = Integer.parseInt(args[5]);
			
			Lottery baloto = new Lottery();
			
			int probability = baloto.calculateResult(whiteBalls, powerBall);
			
			System.out.println("The probability is " + probability);			
		}

	}

}