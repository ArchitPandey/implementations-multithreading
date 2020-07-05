package implementations;


/*
 * Implement producer consumer problem using counting semaphore class
 */
public class ProducerConsumer_CountingSemaphore {
	
	int[] buffer;
	int head;		//remove from front
	int tail;		//add to back
	CountingSemaphore semProducer;
	CountingSemaphore semConsumer;
	CountingSemaphore semLock;		//acts as a mutex
	
	ProducerConsumer_CountingSemaphore(int buffersize) {
		this.buffer = new int[buffersize];
		this.head = 0;
		this.tail = 0;
		semProducer = new CountingSemaphore(buffersize, buffersize);
		semConsumer = new CountingSemaphore(buffersize, 0);
		semLock = new CountingSemaphore(1,1);
	}
	
	//note that the lock order 
	//semProducer.acquire then semLock.acquire in producer and 
	//semConsumer.acquire then semLock.acquire is consumer
	//is important here 
	//if order is reversed, after buffer is full, and flow goes into producer()
	//it acquires the permit from semLock and then tries to acquire the 
	//semProducer permit, this blocks
	//one way to release semProducer permit was is consumer() ran, but consumer 
	//can't run as it needs to get lock of semLock first (if lock orders were reversed)
	//and that is held by producer, this results in deadlock
	
	void produce(int x) throws InterruptedException {
		
		semProducer.acquire();
		semLock.acquire();
		
		tail = (tail%buffer.length);
		
		System.out.println("Producer adding = "+x);
		buffer[tail++] = x;
		
		semLock.release();
		semConsumer.release();
	}
	
	void consumer() throws InterruptedException {
		
		semConsumer.acquire();
		semLock.acquire();
		
		head = (head%buffer.length);
		
		System.out.println("Consumer consuming ="+buffer[head]);
		
		head++;
		
		semLock.release();
		semProducer.release();
	}
	
	public static void main(String[] args) {
		
		ProducerConsumer_CountingSemaphore o = new ProducerConsumer_CountingSemaphore(10);
		
		Thread producer = new Thread(() ->   {
			try {	
				for(int i=1; i<20; i++) {
					o.produce(i);
				}
			} catch(InterruptedException e) {
				System.out.println("Producer Interrupted !");
			}
		} );
		
		Thread consumer  = new Thread( () ->  {
			try {
				for(int i=1; i<20; i++) {
					o.consumer();
				}
			} catch(InterruptedException e) {
				System.out.println("Consumer Interrupted !");
			}
		});
		
		producer.start();
		consumer.start();
		
		try {
			producer.join();
			consumer.join();
		} catch (InterruptedException e) {
			System.out.println("Exception while waiting for threads to finish "+e.getMessage());
		}
		
	}
}
