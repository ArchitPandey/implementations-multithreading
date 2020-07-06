package implementations;

/*
 * Class providing methods to allow multiple reads when no write is happening 
 * at the same time
 * 
 * also allows one write when no read and write are happening at the same time
 * 
 * Reponsibility- 
 * provide methods to acquire/release readLock - should block when write is ongoing
 * provide methods to acquire/release writeLock - should block when any read/write is ongoing
 * 
 * Threads are supposed to acquire lock before changing some shared state, release after they are done
 * 
 * normally looks like this inside runnable - 
 * readWriteLock.acquireReadLock()
 * do some work here
 * readWriteLock.releaseReadLock()
 * 
 * same for writer thread
 * 
 * Note that this implementation may lead to starvation of writer threads as num of reader can be much more
 * and writer can only write when no reader is reading
 */

public class ReadWriteLock {
	
	int activeReadLock;
	boolean writeLock;
	
	//only one thread can try to get read lock at a time allowing multiple threads to 
	//modify activeReadLock will cause multithreading issues
	public synchronized void acquireReadLock() throws InterruptedException {
		while(writeLock) {
			this.wait();
		}
		
		activeReadLock++;
	}
	
	public synchronized void releaseReadLock() {
		activeReadLock--;
		this.notifyAll();
	}
	
	public synchronized void acquireWriteLock() throws InterruptedException {
		while( (writeLock) || (activeReadLock != 0) )
			this.wait();
		
		writeLock = true;
	}
	
	public synchronized void releaseWriteLock() {
		writeLock = false;
		this.notifyAll();
	}
}
