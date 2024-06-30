package pill;

import java.io.File;

public class Pill {
	static File root ;
	
	public static void setRoot (String s) { 
		root= new File(s);
		System.err.println("Got the root: " + root.getAbsolutePath());
	}
	public static File getRoot () {
		if (root == null) {
			String pfr = System.getenv("PFR");
			if (pfr != null) {
				root = new File(pfr);
			}
			else {
				throw new RuntimeException("Please set-up PFR for debugging");
				
			}
		}
		return root;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
