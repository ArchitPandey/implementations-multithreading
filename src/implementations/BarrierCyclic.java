package implementations;

/*
 * Class makes multiples threads to wait at a barrier 
 * until 'x' threads have reached the barrier. after x
 * threads reach the barrier, it trips and threads are 
 * released
 * 
 * barrier then blocks till next x threads reach the barrier
 * 
 * Reponsibility - 
 * class to provide functionality to block until x threads 
 * reach the barrier. after barrier trips, it is reset 
 * automatically and block until next x threads reach the 
 * barrier
 * 
 * BarrierCyclic(x)
 * await() - blocks until x threads reach barrier 
 */

public class BarrierCyclic {
	
	int countThreadsAtBarrier;
	int countThreadsToRelease;
	final int MAX_THREADS;
	
	BarrierCyclic(int x) {
		this.MAX_THREADS = x;
		this.countThreadsAtBarrier = 0;
		this.countThreadsToRelease = 0;
	}
	
	public synchronized void await() throws InterruptedException {
		
		//this is prevent any threads moving forward 
		//if previous x threads are not released
		while(countThreadsAtBarrier == MAX_THREADS)
			wait();
		
		countThreadsAtBarrier++;
		
		if(countThreadsAtBarrier < MAX_THREADS) {
			//wait till all the threads reach barrier
			while(countThreadsAtBarrier < MAX_THREADS)
				wait();
		} else {
			countThreadsToRelease = MAX_THREADS;
			//this is to notify all the threads stuck at ln 45
			//tell them that all x threads have gathered at barrier
			//so wake up
			notifyAll();
		}
		
		countThreadsToRelease--;
		
		//setting countThreadsAtBarrier gives illusion 
		//that all threads are released at the same time
		//more importantly, it prevents other threads from 
		//proceeding past ln 37, when already x threads have
		//reached the barrier and while some of them are yet
		//to be released
		if(countThreadsToRelease == 0) {
			countThreadsAtBarrier = 0;
			//this is to notify all the next set threads that were stuck 
			//at ln 37 while current set of x threads are getting released
			notifyAll();				
		}
	}
}
