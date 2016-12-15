import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Naming;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
public class RPCClient 
{ 
	private static RPCServerAppInterface obj=null;
	
	static int port=0;
	static String server=null;
	
  
	//Entry point for a Client 
    public static void main(String arg[]) 
    { 
    	Scanner sc=null;
    	
        try 
        { 
        	if(port==0 || server==null){
	        	sc= new Scanner(System.in);
	
	        	System.out.print("\nPlease enter a server address to connect:");
	        	server=sc.next();
	        	        	
	        	System.out.print("\nPlease enter a port number to connect:");
	        	port= sc.nextInt();
        	}
        	        	
        	System.out.println("\nInitiating request at "+new Timestamp(System.currentTimeMillis())+"\n");
        	
        	obj= (RPCServerAppInterface) Naming.lookup("rmi://"+server+":"+port+"/RPCServer"); 
        	
        	
        	operateKeyValueStore("src/dataScript");
        	
        } catch (Exception e) 
        { 
        	System.out.println("CONNECTION ERROR to SERVER \""+server+":"+port+ "\"");
           
        } 
        finally{
        	sc.close();
        }
    }

    //Operate a list of put/get/instructions
	private static void operateKeyValueStore(String filename) {
		
		
		StringBuilder str= new StringBuilder();
				
		List<Long> putTime=new ArrayList<Long>();
		List<Long> getTime=new ArrayList<Long>();
		List<Long> delTime=new ArrayList<Long>();
		
		
		Scanner sc=null;
     	        	     	
		try {
			sc = new Scanner(new File(filename));    
			
			String sCurrentWord="";
			String key="";
			String value="";

			long timestamp1=0;
			
			while (sc.hasNext() && (sCurrentWord = sc.next()) != null) {    				
				if(sCurrentWord.startsWith("PUT")){
					key=sc.next().trim();
					value=sc.nextLine().trim();
					
					timestamp1=System.currentTimeMillis();
						str.append(obj.put(key, value));
						
					putTime.add(System.currentTimeMillis()-timestamp1);
					
				}else if(sCurrentWord.startsWith("GET")){
					key=sc.nextLine().trim();
					timestamp1=System.currentTimeMillis();
					str.append(obj.get(key));   
					
					getTime.add(System.currentTimeMillis()-timestamp1);
				}else if(sCurrentWord.startsWith("DELETE")){
					key=sc.nextLine().trim();
					timestamp1=System.currentTimeMillis();

					str.append(obj.delete(key));
					delTime.add(System.currentTimeMillis()-timestamp1);
				}else{
					str.append(new Timestamp(System.currentTimeMillis())+"\t\tInvalid entry detected. Ignoring and proceeding with next entry.\n");
				}
				//sc.nextLine();
				str.append("\n");
			}
			
			System.out.println(str+"\n");

			System.out.println("\n\t\tPUT TRANSACTIONS\n");
			System.out.println("Number of Transactions:"+putTime.size());
			System.out.println("Average time taken(ms) :"+CalculationsUtility.average(putTime.toArray(new Long[0])));
			System.out.println("Standard Deviation :"+CalculationsUtility.standardDeviation(putTime.toArray(new Long[0])));;


			System.out.println("\n\t\tGET TRANSACTIONS\n");
			System.out.println("Number of Transactions:"+getTime.size());
			System.out.println("Average time taken(ms) :"+CalculationsUtility.average(getTime.toArray(new Long[0])));
			System.out.println("Standard Deviation :"+CalculationsUtility.standardDeviation(getTime.toArray(new Long[0])));;

			System.out.println("\n\t\tDELETE TRANSACTIONS\n");
			System.out.println("Number of Transactions:"+delTime.size());
			System.out.println("Average time taken(ms) :"+CalculationsUtility.average(delTime.toArray(new Long[0])));
			System.out.println("Standard Deviation :"+CalculationsUtility.standardDeviation(delTime.toArray(new Long[0])));;

	
        } catch (FileNotFoundException e1) {
        	System.out.println("File not found error. Please try again.");
        } catch (IOException e1) {
        	System.out.println("Error occured. Please try again.");
        }catch (Exception e) {
			str= new StringBuilder();
			str.append(new Timestamp(System.currentTimeMillis())+"\t\tError occured. Process terminated"+e.getMessage());
			e.printStackTrace();
		} finally {
			sc.close();
			
		}
	}
	
} 
 