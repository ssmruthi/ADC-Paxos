The server configuration is hardcoded for n01, n02, n03, n04, n05 - 5 servers.
To change configuration, edit in src/serverConfig file. Details towards the end.
Make file is present in "SourceCode" folder

- Copy the contents of "SourceCode" only in these VMS n01, n02, n03, n04, n05, n06 (6= 5 server + 1 client)
- Run 'make' in all 6 VMs
- Execute "java RPCServer" in each of 5 VMs starting n01 to n05
- Enter port number as 8080  for all VMs (Current configuration. Please do not enter different port. To change configuration, edit in src/serverConfig file)
- Execute "java RPCClient" in another n06 VM
- Enter one of the server address and portnumber . Eg : n01 8080
- Verify results


To verify for different servers and ports

- Edit src/serverConfig
- Enter all server details one below another in the format
<serverhostname><space><portnumber> 

- Do not add additional new line character at the end of file.