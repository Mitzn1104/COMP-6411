import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Main class to initate the message passing between threads
 * @author Mitalee
 *
 */
public class exchange {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
			BufferedReader reader;
			HashMap<String,List<String>> mapOfData =  new HashMap<String,List<String>>();
			try {
				System.out.println("** Calls to be made **");
				reader = new BufferedReader(new FileReader("calls.txt"));
				String line = reader.readLine();
				while (line != null) {
					String[] calls = line.split(",",2);
					List<String> tempList = Arrays.asList(calls[1].trim().replace("[", "").replace("]}.", "").trim().split(","));			
					mapOfData.put(calls[0].replace("{", ""), tempList);
					System.out.println(calls[0].replace("{", "") + ": "+tempList);
					line = reader.readLine();
				}
				reader.close();
			}catch(IOException ex) {
				ex.printStackTrace();
			}
			System.out.println();
			//Invoke master thread
			Thread masterThread = new Thread(new master(mapOfData),"Master");
			masterThread.start();			
	}
}
/**
 * Master thread class
 * @author Mitalee
 *
 */
class master implements Runnable{
	HashMap<String,List<String>> mapOfData;
	LinkedBlockingQueue<MessageDetails> masterQueue;
	HashMap<String,calling> mapOfChildThreads;
	
	public master(HashMap<String, List<String>> mapOfData) {
		super();
		this.mapOfData = mapOfData;
		this.masterQueue =  new LinkedBlockingQueue<MessageDetails>();
		this.mapOfChildThreads = new HashMap<String,calling>();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//Create child threads for each contact
		for(String caller : mapOfData.keySet()) {
			
			calling receiver = new calling(caller, new LinkedBlockingQueue<MessageDetails>(),masterQueue);
			mapOfChildThreads.put(caller, receiver);	
			Thread childThread = new Thread(receiver,caller);
			childThread.start();
		}
		
		//Send receiver details to each contact
		for(String caller :mapOfChildThreads.keySet()) {
			mapOfChildThreads.get(caller).sendMessage(mapOfChildThreads,mapOfData.get(caller));
		}
		
		boolean isAlive = true;
        long timer = System.currentTimeMillis();
        //While master is alive, it will check it's masterQueue for any pending msgs
        while (isAlive) {
        	//Timeout after 10 seconds (master)
            if (System.currentTimeMillis() - timer > 10000) {
                System.out.println("\nMaster has received no replies for 10 seconds, ending...");
                isAlive = false;
            }
            while (!masterQueue.isEmpty()) {
                MessageDetails msg = masterQueue.poll();
                System.out.println(msg.getReceiverName() + " received " + msg.getMessageType() + " message from " + msg.getSenderCallingObj().childName + " [" + msg.getTimeStamp() + "]");
                timer = System.currentTimeMillis();
            }
        }
		
	}
	
}
/**
 * Child thread class
 * @author Mitalee
 *
 */
class calling implements Runnable{
	String childName;
	LinkedBlockingQueue<MessageDetails> childQueue;
	LinkedBlockingQueue<MessageDetails> masterQueue;
	
	public calling(String childName, LinkedBlockingQueue<MessageDetails> childQueue,
			LinkedBlockingQueue<MessageDetails> masterQueue) {
		super();
		this.childName = childName;
		this.childQueue = childQueue;
		this.masterQueue = masterQueue;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub	
		boolean isAlive = true;
        long timer = System.currentTimeMillis();
        //While child is alive, it will check it's childQueue for any pending msgs
        while (isAlive) {
        	//Timeout after 5 seconds (child)
            if (System.currentTimeMillis() - timer > 5000) {
                System.out.println("\nProcess " + this.childName + " has recieved no calls for 5 second, ending...");
                isAlive = false;
            }
            
            while (!this.childQueue.isEmpty()) {
                MessageDetails msg = this.childQueue.poll();
                //Adding to master's queue to print the message
                masterQueue.add(msg);
                if (msg.getMessageType().equals("intro")) {
                	//Receiver becomes the sender
                	calling receiver = msg.getSenderCallingObj();
                	try {
        				int randomNum = new Random().nextInt(100) + 1;
        				Thread.sleep(randomNum);
        			} catch (InterruptedException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
                	//Adding reply message to child Queue
                    receiver.childQueue.add(new MessageDetails(this, msg.getSenderCallingObj().childName, "reply", msg.getTimeStamp()));
                }              
                timer = System.currentTimeMillis();
            }
        }
		
	}
	/**
	 * Each child thread receives receiverList from the master
	 * Each child thread will send intro message to the receivers.
	 * @param mapOfChildThreads
	 * @param receiverList
	 */
	public void sendMessage(HashMap<String, calling> mapOfChildThreads, List<String> receiverList) {
		for(String receiver :receiverList) {
			calling receiverThread = mapOfChildThreads.get(receiver);
			try {
				int randomNum = new Random().nextInt(100) + 1;
				Thread.sleep(randomNum);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Adding intro message to child Queue
			receiverThread.childQueue.add(new MessageDetails(this, receiver, "intro", System.currentTimeMillis()));
		}
		
	}
	
	
}

/**
 * Message Component class
 * @author Mitalee
 *
 */
class MessageDetails{
	private String receiverName;
	private calling senderCallingObj;
	private String messageType;
	private long timeStamp;
	
	public MessageDetails(calling senderCallingObj,  String receiverName, String messageType, long timeStamp) {
		super();
		this.senderCallingObj = senderCallingObj;
		this.receiverName = receiverName;
		this.messageType = messageType;
		this.timeStamp = timeStamp;
	}
	public String getReceiverName() {
		return receiverName;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public String getMessageType() {
		return messageType;
	}
	public calling getSenderCallingObj() {
		return senderCallingObj;
	}
}