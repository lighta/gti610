package app.partie12.server;

public class test {

	public static void main(String[] args) {
		char opcode = 0;
		for (int i = 0; i < 4; i++) {
			
			opcode <<= 1;
			opcode |= true ? 1:0;
			System.out.println(Integer.toBinaryString(opcode));
		}

	}

}
