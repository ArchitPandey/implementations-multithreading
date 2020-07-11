package implementations;

import java.util.ArrayList;
import java.util.List;

/*
 * class implements Unisex Bathromm
 * 	male and female can't be bathroom at any time
 * 	bathroom can't have more than 3 members
 * 
 * class provides two methods - maleUseBathroom, femaleUseBathroom
 * Each person takes 1000ms to use the bathroom
 * 
 * The implementation consists of using variables to maintain different 
 * information and making caller threads wait based on that information
 * to satisfy the conditions on using bathroom
 * This approach is kinda similar to ReadWriteLock problem
 */

public class UnisexBathroom {
	
	private static final String MALE = "male";
	private static final String FEMALE = "female";
	private static final String NONE = "none";
	private static final int MAX_USERS = 3; 
	private static final int USE_TIME = 1000; //in ms
	
	//inUseBy - male, female, none
	private String inUseBy;
	
	//instead of numUsers, a semaphore with max_permits can also be used
	//permits denote the number of available spaces in bathroom
	private int numUsers;
	
	UnisexBathroom() {
		inUseBy = NONE;
		numUsers = 0;
	}
	
	private void useBathroom(String gender) {
		System.out.println("bathroom used by "+gender);
		try {
			Thread.sleep(USE_TIME);
		} catch (InterruptedException e) {
			System.out.println("Interrupted while using bathroom !");
		}
	}
	
	public void maleUseBathroom() throws InterruptedException {
		synchronized(this) {
			while(inUseBy.equals(FEMALE) || numUsers >= MAX_USERS)
				this.wait();
			inUseBy = MALE;
			numUsers++;
		}
		useBathroom(MALE);
		synchronized(this) {
			numUsers--;
			if(numUsers == 0)
				inUseBy = NONE;
			this.notifyAll();   	//this can possibly be moved to numUser==0 if block
		}
	}
	
	public void femaleUseBathroom() throws InterruptedException {
		synchronized(this) {
			while(inUseBy.equals(MALE) || numUsers >= MAX_USERS)
				this.wait();
			inUseBy = FEMALE;
			numUsers++;
		}
		useBathroom(FEMALE);
		synchronized(this) {
			numUsers--;
			if(numUsers == 0)
				inUseBy = NONE;
			this.notifyAll();
		}
	}
	
	public static void main(String[] args) {
		UnisexBathroom o = new UnisexBathroom();
		
		List<Thread> maleusers = new ArrayList<>();
		List<Thread> femaleusers = new ArrayList<>();
		
		int male = 5;
		int female = 3;
		
		for(int i=0; i<male; i++) {
			maleusers.add( new Thread(  () ->   {try {
				o.maleUseBathroom();
			} catch (InterruptedException e) {
				System.out.println("Interrupted male thread");
			}} )  );
		}
		
		
		for(int i=0; i<female; i++) {
			femaleusers.add(new Thread( () ->  {try {
				o.femaleUseBathroom();
			} catch (InterruptedException e) {
				System.out.println("Interrupted female thread");
			}} ) );
		}
		
		int midx = 0;
		int fidx = 0;
		
		boolean maledone = false;
		boolean femaledone = false;
		int runid = 0;
		
		while( !(maledone && femaledone) ) {
			if( (runid%2 == 0) ) {
				if(midx < maleusers.size()) {
					maleusers.get(midx).start();
					midx++;
				} else {
					maledone = true;
				}
			} else if( (runid%2 == 1) ){
				if(fidx < femaleusers.size()) {
					femaleusers.get(fidx).start();
					fidx++;
				} else {
					femaledone = true;
				}
			}
			runid++;
		}
		
		maleusers.forEach(e -> {
			try {
				e.join();
			} catch (InterruptedException e1) {
				System.out.println("Interrupted while waiting for male to complete!");
			}
		});
		femaleusers.forEach(e -> {
			try {
				e.join();
			} catch (InterruptedException e1) {
				System.out.println("Interrupted while waiting for female to complete!");
			}
		});
	}
}
