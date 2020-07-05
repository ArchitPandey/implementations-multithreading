package implementations;

/*
 * a semaphore class with max permits
 * different from java-Semaphore in the sense that java semaphore only has
 * initial permits, actual permits can go beyond initial permits
 * 
 * Responsibility - 
 * allows threads to acquire and release permits (permits can be analogous to an
 * expensive resource). using permits, access to a pool of resources can be controlled
 * in multi-threaded environments
 * 
 * Behaviour - 
 * acquire() - obtains a permit, blocks if all the permits are given away
 * release() - releases a permit, blocks if no permit is acquired
 * 
 * allows user to specify max_permits and initial available permits
 */

public class CountingSemaphore {
	
	private int availablePermits;
	private final int MAX_PERMITS;
	
	public CountingSemaphore(int maxpermits, int availablepermits) {
		this.MAX_PERMITS = maxpermits;
		this.availablePermits = availablepermits;
	}
	
	public synchronized void acquire() throws InterruptedException {
		while(availablePermits == 0)
			this.wait();
		
		availablePermits--;
		this.notifyAll();
	}
	
	public synchronized void release() throws InterruptedException {
		while(availablePermits == MAX_PERMITS) {
			this.wait();
		}
		
		availablePermits++;
		this.notifyAll();
	}
	
	public int getMaxPermits() {
		return this.MAX_PERMITS;
	}
}
