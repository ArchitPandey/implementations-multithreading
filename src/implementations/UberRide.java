package implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Uber picks up people from a convention having either Democrats or 
 * Republicans. 
 * 
 * To avoid fights only following combinations are allowed - 
 * 4 democrats
 * 4 republicans
 * 2 democrats and 2 republicans
 * 
 * Each thread should call seated after it's accepted for ride
 * One of the thread should call drive after all threads are seated 
 * 
 * Think of seated as thread sitting in the car..waiting for gates to close
 * Think of drive as closing all the gates and driving away
 */
public class UberRide {
	
	int cntDemocrats;
	int cntRepublicans;
	
	final int MAX_RIDERS;
	
	//advantage of using lock over synchronized. lock can be released in if-else blocks
	//conditionally
	Lock lock;
	
	CyclicBarrier barrier;
	Semaphore demsWaiting;
	Semaphore repubsWaiting;
	
	public static final String DEMOCRAT = "democrat";
	public static final String REPUBLICAN = "republican";
	
	UberRide(int maxallowed) {
		this.MAX_RIDERS = maxallowed;
		this.cntDemocrats = 0;
		this.cntRepublicans = 0;
		this.lock = new ReentrantLock();
		this.barrier = new CyclicBarrier(4);
		this.demsWaiting = new Semaphore(0);
		this.repubsWaiting = new Semaphore(0);
	}
	
	public void bookDemocrat() throws InterruptedException, BrokenBarrierException {
		
		lock.lock();
		
		//last rider can't be instance variable, we want only last rider to call drive
		//if lastrider was instance variable...any other rider thread selected for ride
		//would also be able to call drive...this can happen when last thread reacher 
		//barrier before one of the other riders. In this case it will be blocked since 
		//barrier tripping point hasn't reached...now other rider reaches barrier and it
		//trips, other rider checks if(this.lastRider) which will be set to true and it 
		//will call drive instead of last thread calling drive
		//making lastRider local to function ensures that has thread has it's own copy of
		//of it and only the last rider's copy has it set to true
		boolean lastRider = false;
		this.cntDemocrats++;
		
		if((this.cntDemocrats == (this.MAX_RIDERS/2) ) && (this.cntRepublicans == (this.MAX_RIDERS/2) ) ) {
			this.demsWaiting.release( (this.MAX_RIDERS/2) - 1);
			this.repubsWaiting.release(this.MAX_RIDERS/2);
			this.cntDemocrats-=(this.MAX_RIDERS/2);
			this.cntRepublicans-=(this.MAX_RIDERS/2);
			lastRider = true;
		}
		else if( this.cntDemocrats == this.MAX_RIDERS ) {
			this.demsWaiting.release(this.MAX_RIDERS-1);
			this.cntDemocrats-=this.MAX_RIDERS;
			lastRider = true;
		} else {
			lock.unlock();
			this.demsWaiting.acquire();
		}
		
		seated();
		barrier.await();
		
		if(lastRider) {
			drive();
			lock.unlock();
		}
	}
	
	public void bookRepublican() throws InterruptedException, BrokenBarrierException {
		
		lock.lock();
		boolean lastRider = false;
		this.cntRepublicans++;
		
		if( (this.cntDemocrats == (this.MAX_RIDERS/2)) && (this.cntRepublicans == (this.MAX_RIDERS/2)) ) {
			this.demsWaiting.release(this.MAX_RIDERS/2);
			this.repubsWaiting.release( (this.MAX_RIDERS/2)-1 );
			this.cntDemocrats-=(this.MAX_RIDERS/2);
			this.cntRepublicans-=(this.MAX_RIDERS/2);
			lastRider = true;
		} else if(this.cntRepublicans == this.MAX_RIDERS) {
			this.repubsWaiting.release(this.MAX_RIDERS-1);
			this.cntRepublicans-=this.MAX_RIDERS;
			lastRider = true;
		} else {
			lock.unlock();
			this.repubsWaiting.acquire();
		}
		
		seated();
		barrier.await();
		
		if(lastRider) {
			this.drive();
			this.lock.unlock();
		}
	}

	private void drive() {
		System.out.println("Driving away...called by "+Thread.currentThread().getName());
	}

	private void seated() {
		System.out.println("Seated..I am "+Thread.currentThread().getName());
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		UberRide o = new UberRide(4);
		
		Random random = new Random();
		List<Thread> threadList = new ArrayList<>();
		
		int i = 0;
		
		while(i < 15) {
			if(random.nextInt()%19 < 9) {
				threadList.add(createThread(o, UberRide.DEMOCRAT, i));
			} else {
				threadList.add(createThread(o, UberRide.REPUBLICAN, i));
			}
			i++;
		}
		
		//for debugging; print out all the riders
		System.out.println("Printing out all the riders -");
		for(Thread thread: threadList)
			System.out.println(thread.getName());
		System.out.println("Done printing");
		for(Thread thread: threadList) {
			thread.start();
		}
		
		for(Thread thread: threadList) {
			thread.join();
		}
		
	}

	private static Thread createThread(UberRide o, String party, int i) {
		Thread thread = new Thread( () -> {
			
				try {
					if(party.equals(UberRide.DEMOCRAT))
						o.bookDemocrat();
					else o.bookRepublican();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}
			}
		);
		
		thread.setName( (party.equals(UberRide.DEMOCRAT)?UberRide.DEMOCRAT:UberRide.REPUBLICAN)+"-"+i);
	
		return thread;
	}
}
