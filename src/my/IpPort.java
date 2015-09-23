package my;

public class IpPort{
	public String ip;
	public int port;
	
	public boolean equals(IpPort another){
		if(ip.equals(another.ip) == false){ return false; }
		if(port != another.port){ return false; }
		return true;
	}
}
