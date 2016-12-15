import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class RPCServerAppImpl extends UnicastRemoteObject implements RPCServerAppInterface
{ 

	static int random_number = 1000;

	public RPCServerAppImpl() throws RemoteException {	
		
	}
		
	public RPCServerAppInterface leader;	
	public RPCServerAppInterface learner;	

	public static Map<String,String> keyValueStore;
	String message="";
	private static int Nhigh=7777; //sequence number of Leader. Assumption : 1 proposer 1 Leader
	private int N=new Random().nextInt(100); //sequence number for all acceptors
	private int quorumSize=3;	
	
    public Map<String, String> getKeyValueStore() {
		return keyValueStore;
	}

    @Override
    public void setKeyValueStore(Map<String, String> keyValueStore) throws RemoteException {
		this.keyValueStore=keyValueStore;
	}
    
	public String delete(String key) throws RemoteException {

		this.leader=RPCServer.leader;
		this.learner=RPCServer.learner;
		
		//Invokes phase 1 to get 'Go' from all servers.

		if (prepare()) {
			
			//Invokes phase 2 after receiving 'Go' from all servers.

			if (accept("Delete("+key+")")) {
			
				//Performs Del operation

				if (keyValueStore == null) {
					keyValueStore = new HashMap<String, String>();
				}

				if (keyValueStore.containsKey(key)) {
					message = new Timestamp(System.currentTimeMillis()) + "\tDELETE Request completed\t\t" + key + " , "
							+ keyValueStore.get(key);
					keyValueStore.remove(key);
				} else
					message = new Timestamp(System.currentTimeMillis())
							+ "\tDELETE Request completed \t\tNo VALUE found for " + key;

				try {
					message = "Client IP : " + RemoteServer.getClientHost() + "  " + message;
				} catch (ServerNotActiveException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				commit(keyValueStore);
				System.out.println("Changes persisted across All Servers. ");
				Nhigh=-1;

			} else {
				message = "Cannot perform delete operation.Accept phase during data persistence.";
			}
		} else {
			message = "Prepare phase Error. Max tries exceeded. Cannot commit data currently. Please try again.";
		}
		return message;
	}
    

	public String put(String key, String value) throws RemoteException {
	
		if (prepare()) {
			
			
			//Invokes accept after recieving 'Go' from all servers.
			
			if (accept("Put("+key+","+value+")")) {
			
				//Performs Put operation
				if (keyValueStore == null) {
					keyValueStore = new HashMap<String, String>();
				}

				keyValueStore.put(key, value);
				if (keyValueStore.containsKey(key)) {
					message = new Timestamp(System.currentTimeMillis()) + "\tPUT Request completed\t\t" + key + " , "
							+ value;
				} else {
					message = "WARNING!\t" + new Timestamp(System.currentTimeMillis())
							+ "\t\t\tEntry already exists for the key.(" + key + "," + value + ") OVERWRITTEN";
				}
				try {
					message = "Client IP : " + RemoteServer.getClientHost() + "  " + message;
				} catch (ServerNotActiveException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println(message);				
				
				commit(keyValueStore);
				System.out.println("Changes persisted across All Servers. ");
				Nhigh=-1;
			} else {
				message = "Cannot perform Put operation.Accept phase Error during data persistence.";
			}
		} else {
			message = "Prepare phase Error. Max tries exceeded. Cannot commit data currently. Please try again.";
		}
		return message;
	}
    
    public void commit(Map<String, String> keyValueStore2) {
		
    	Scanner sc=null;
    	 try {
			  
			  sc= new Scanner(new File("src/serverConfig"));
			  Map<String,Integer> ack= new HashMap<String,Integer>();
		
			  
			  while(sc.hasNextLine()){
				  String server=sc.next();
				  int port =sc.nextInt();

				  // Makes a RPC connection to each of the server in the network
				 System.out.println(server+"*"+port+"  Pinging server ");
				 RPCServerAppInterface obj= (RPCServerAppInterface) Naming.lookup("rmi://"+server+":"+port+"/RPCServer");
				 obj.setKeyValueStore(keyValueStore2);
				 
			  }
			 		  
			  System.out.println("Promise Response from Acceptors: "+ack);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			sc.close();
		}
		
	}

	public String get(String key)throws RemoteException{
    	
    	   	
    	if(keyValueStore==null){
    		keyValueStore= new HashMap<String, String>();
    	}
		    	
    	if(keyValueStore.containsKey(key))
    		message =new Timestamp(System.currentTimeMillis())+"\tGET Request completed\t\t"+key+" , "+ keyValueStore.get(key);   
    	else
    		message=new Timestamp(System.currentTimeMillis())+"\tGET Request completed \t\tNo VALUE found for "+key;
    	
    	try {
    		message="Client IP : "+RemoteServer.getClientHost()+"  "+message;
    		
    	} catch (ServerNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println(message);
    	return message;
    	
    }    
	
    
    // Called from  RPCClient.java Ping().
    // Collects vote or go command from a server.
	
	@Override
	public int voteRequestForPrepare(int Nhigh) throws RemoteException {
		// TODO Auto-generated method stub
		
		System.out.println("\tPromising back with "+Nhigh);
		this.Nhigh=Nhigh;
		return this.Nhigh;
	}
	
	@Override
	public String voteRequestForAccept(int Nhigh,String value) throws RemoteException {

		System.out.print("\tAccepting "+Nhigh +" value "+value);

		if(Nhigh == this.Nhigh){
			this.Nhigh=Nhigh;
			}
		return value;
		
	}
	

	 /* Phase 1 of a put/ delete request
	  * Retrieves list of other servers configured in serverConfig file.
	  * Uses RPC connection to get a Go/Abort command from the == makes RPCClient.ping() call	   
	  * 
	  * Returns true only if all Servers have agreed for a Go.
	 */  
    
	  public boolean prepare()throws RemoteException  {
		  
		  Nhigh=new Random().nextInt(1000);
		  System.out.println("Preparing with Nhigh "+Nhigh);
		  
		  boolean commit=false;
		  Scanner sc=null;

		  try {
			  
			  sc= new Scanner(new File("src/serverConfig"));
			  Map<String,Integer> ack= new HashMap<String,Integer>();
			  
			  int vote=-1;
			  int maxPromise=0;
			
  
			  while(sc.hasNextLine()){
				  String server=sc.next();
				  int port =sc.nextInt();

				  // Makes a RPC connection to each of the server in the network
				 System.out.print(server+":"+port+"  Pinging server ");
				 
				try{
					RPCServerAppInterface obj= (RPCServerAppInterface) Naming.lookup("rmi://"+server+":"+port+"/RPCServer"); 
				    vote=obj.voteRequestForPrepare(Nhigh);

				}catch(NotBoundException e){
					System.out.println("Connection error.");
				}
				
				 System.out.println("\t Promise Response from Acceptor: "+server+":"+port+" Responsed with value:"+vote);

				 if(vote==this.Nhigh)
					 maxPromise++;
				 
				 ack.put(server+":"+port,vote);
				 
			  }
			  
			  //Check consensus
			  if(maxPromise>=this.quorumSize){
				  commit= true;
			  }
			  
			  System.out.println("\nCommit Returned : "+commit+" quorumSize:"+quorumSize+" maxPromise:"+maxPromise);
			  System.out.println("Promise Responses from Acceptors: "+ack);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			sc.close();
		}
		  return commit;
	  }
 
	  /* After all servers have agreed for a 'Go' in phase1, triggers Phase 2 of a put/ delete request
	   * Phase 2 updates KeyValue store in all Servers.
	   * Retrieves list of other servers configured in serverConfig file.
	   * Uses RPC connection to connect to to other servers and send the updated keyvalue store	  
	   * 
	   *  Returns true only if all Servers have updated for a Go.
	   */ 
	  
	  public boolean accept(String value)throws RemoteException  {
				 
		 	  
		  boolean commit=false;
		  Scanner sc=null;
		  try {
			  
			  sc= new Scanner(new File("src/serverConfig"));
			  Map<String,Integer> ack= new HashMap<String,Integer>();
			  
			  int vote=-1;
			  int maxAccept=0;
			  String acceptValue=null;
			  
			  while(sc.hasNextLine()){
				  
				  vote=-1;
				  String server=sc.next();
				  int port =sc.nextInt();

				 System.out.println(server+"*"+port+"Accept Value Request for  "+value);
				 
				 try{
				 
					 RPCServerAppInterface obj= (RPCServerAppInterface) Naming.lookup("rmi://"+server+":"+port+"/RPCServer"); 
					 acceptValue=obj.voteRequestForAccept(Nhigh,value);
					 
					 Random rand = new Random();
				        int random_number = rand.nextInt(10) + 1; // generate a random # from 1-10
				        if (random_number <= 3) { // 30% chance of failure
				            System.out.println("Randomized System Failure at " + (System.currentTimeMillis()) + " milliseconds");
				            try {
				                //thread to sleep for the specified number of milliseconds
				                Thread.sleep(3000);
				            } catch ( java.lang.InterruptedException ie) {
				                System.out.println("Interrupted Exception " + ie);
				            }
				            acceptValue =null;
				        }
				 
				 } catch (NotBoundException e) {
					 System.out.println("Connection error.");
				 }
				 
				 System.out.println("\t Promise Response from Acceptor: "+server+":"+port+" Responsed with value:"+acceptValue);
				 
				 if(acceptValue!=null && acceptValue.equals(value)){
					 maxAccept++;
					 vote=1;
				 }
			
				//	Add acks from Server to map
					ack.put(server+":"+port,vote);								
			  }
				
				//Error connecting server conditions
				if(maxAccept>=this.quorumSize){				
					commit=true;					
				}
			
			  System.out.println("Commit Returned : "+commit+" acceptorCount:"+quorumSize+" maxAccept:"+maxAccept);
			  System.out.println("Accept Response from Acceptors: "+ack);
			  
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}finally{
			sc.close();
		}
		  return commit;
	  }

	@Override
	public RPCServerAppInterface getLeader() throws RemoteException {
		return this.leader;		
	}

}

