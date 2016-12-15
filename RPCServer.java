

import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;

public class RPCServer  {

	static RPCServerAppInterface obj;
	static RPCServerAppInterface leader; 
	static RPCServerAppInterface learner; 

	static int port=0;
	static String server=null;
	
	public static void main(String args[]) throws RemoteException, UnknownHostException
    {
        try 
        { 
        	if(port==0){ 
        		Scanner sc= new Scanner(System.in);      
        		System.out.println("Enter port number:"+port);        		  	
        		port= sc.nextInt();        	
        	}  
        	
        	if(obj==null){

        		obj= new RPCServerAppImpl();
        		
        		//Write Leader election algorithm if preferred.
            	            	
        	}
        	

            java.rmi.registry.LocateRegistry.createRegistry(port);
            Naming.rebind("rmi://:"+port+"/RPCServer",obj); 
            
            System.out.println("Server listening at port : "+port);
        } 
        catch (Exception e) 
        { 
            System.out.println("ERROR OCCURRED DURING PROCESSING. PLEASE RETRY PROCESS." + e.getMessage());
            e.printStackTrace();
        } 
    } 
}
