package implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * A barbershop has waiting room with n chairs. 
 * There is a barber chair for giving haricuts. 
 * A customer enters the shop. If all chairs 
 * are full, he leaves. If one of the chairs are empty
 * he occupies. Barber goes to sleep if there are no 
 * customers. If a customer enters and finds the chair and 
 * if barber is sleeping, he wakes him up
 * 
 * Barber greets one the waiting customers and take him to 
 * seat him barber area and seat him in the barber chair 
 * for haircut
 * 
 * Barber shop should expose two methods - 
 * customerEnter()
 * barberWork()
 * 
 * customerEnter is supposed to be called by customer threads
 * trying to get a haircut
 * 
 * barberWork is supposed to be called by barber thread
 */
public class BarberShop {
	
	int numCustomersWaiting;
	final int MAX_CUSTOMERS;
	Lock lock;
	Semaphore customerWaiting;
	Semaphore barberCalls;
	Semaphore occupyBarberSeat;
	Semaphore waitHairCutEnd;
	
	BarberShop(int capacity) {
		this.MAX_CUSTOMERS = capacity;
		this.numCustomersWaiting = 0;
		this.lock = new ReentrantLock();
		this.customerWaiting = new Semaphore(0);	//initially zero customers in shop
		this.barberCalls = new Semaphore(0);	//barber signals customer to come
		this.occupyBarberSeat = new Semaphore(0); //customer will signal barber after it's seated in barber seat
		this.waitHairCutEnd = new Semaphore(0);	//wait for hair cut to end. barber will signal it after it has completed haircut
	}
	
	public void customerEnter() throws InterruptedException {
		
		lock.lock();
		
		if(numCustomersWaiting == MAX_CUSTOMERS) {
			System.out.println("No space left, leaving "+Thread.currentThread().getName());
			lock.unlock();
			return;
		}
		
		numCustomersWaiting++;
		System.out.println("Wating "+Thread.currentThread().getName());
		lock.unlock();
		
		customerWaiting.release();	//signal in case barber is sleeping due to no customers
		
		barberCalls.acquire(); //wait for barber to greet or call
		
		lock.lock();
		numCustomersWaiting--;		//after barber calls release your waiting seat
		
		System.out.println("called by barber "+Thread.currentThread().getName());
		
		lock.unlock();
		
		occupyBarberSeat.release();
		
		waitHairCutEnd.acquire();
		
	}
	
	public void barberWork() throws InterruptedException {
		while(true) {
			customerWaiting.acquire();
			System.out.println("Barber awake!");
			
			barberCalls.release();	//signal any customer waiting for call
			
			occupyBarberSeat.acquire(); //wait for customer to be seated
			
			Thread.sleep(2000);  //simulate haircut
			System.out.println("Haircut done!");
			
			waitHairCutEnd.release();  //signal waiting customer that hair cut is done
		}
	}
	
	
	public static void main(String[] args) {
		
		BarberShop o = new BarberShop(5);
		
		Thread barber = new Thread(() -> {
			try {
				o.barberWork();
			} catch (InterruptedException e) {
				System.out.println("Interrupted "+Thread.currentThread().getName());
				e.printStackTrace();
			}
		});
		
		List<Thread> customers = new ArrayList<>();
		
		Random random = new Random();
		
		for(int i = 0; i<10; i++) {
			 Thread customer = new Thread( ()->{
				
				try {
					Thread.sleep((Math.abs(random.nextInt())%5)*1000);
					o.customerEnter();
				} catch (InterruptedException e) {
					System.out.println("Interrupted "+Thread.currentThread().getName());
					e.printStackTrace();
				}
			}  
					);
			customer.setName("Customer Thread "+i);
			customers.add(customer);
		}
		
		barber.start();
		for(Thread customer: customers) {
			customer.start();
		}
		
		for(Thread customer: customers) {
			try {
				customer.join();
			} catch (InterruptedException e) {
				System.out.println("Interrupted while customer main thread waiting in join of "+customer.getName());
				e.printStackTrace();
			}
		}
		
		barber.interrupt();
		System.out.print("Interrupted barber thread! exiting");
	}
}
