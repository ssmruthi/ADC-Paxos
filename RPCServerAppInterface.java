

import java.rmi.Remote;
import java.util.Map;

public interface RPCServerAppInterface extends Remote {
	
	
	public RPCServerAppInterface getLeader() throws java.rmi.RemoteException;
	
	//Incoming server requests
	public String put(String key, String value)throws java.rmi.RemoteException ;
	public String get(String key)throws java.rmi.RemoteException ;
	public String delete(String key)throws java.rmi.RemoteException ;
	
	//Proposer operation
	public boolean prepare()throws java.rmi.RemoteException ;
	public boolean accept(String value)throws java.rmi.RemoteException ;
	
	//Acceptor Operations
	public int 	voteRequestForPrepare(int Nhigh)throws java.rmi.RemoteException ;
	public String voteRequestForAccept(int Nhigh,String value)throws java.rmi.RemoteException ;
	
	//Learner commit operation
    public void commit(Map<String, String> keyValueStore2) throws java.rmi.RemoteException ;
	public void setKeyValueStore(Map<String,String> kvStore)throws java.rmi.RemoteException ;	
}