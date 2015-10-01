package my;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Main {
	
	public static void main(String[] args) {
		try{
			Main me = new Main();
			
			System.out.println("server started");
			System.out.println("IP: " + InetAddress.getLocalHost().getHostAddress());
			System.out.println("port: " + port);
			
			me.process();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	
	public Main() throws Exception{
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		surfer = new Surfer();
		socket = new DatagramSocket(port);
	}
	
	
	public void process() throws Exception{
		while(true){
			byte buffer[] = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buffer,  buffer.length);
			socket.receive(packet);
			
			show_info_got_message(packet);
			
			String orge_str = new String(packet.getData());
			System.out.println("    the message:");
			System.out.println(orge_str);
			
			Request request;
			try{
				request = mapper.readValue(orge_str, Request.class);
			}
			catch(Exception e){
				continue;
			}
			process_request(request, packet);
		}
	}


	private void show_info_got_message(DatagramPacket packet){
		InetSocketAddress sockaddr = (InetSocketAddress)packet.getSocketAddress();
		InetAddress ip = sockaddr.getAddress();
		System.out.println("you got a message:");
		System.out.println("    sender's socket id: " + sockaddr.toString());
		System.out.println("    sender's global ip: " + ip.getHostAddress());
		System.out.println("    sender's global port: " + sockaddr.getPort());
	}
	
	private void process_request(Request request, DatagramPacket packet) throws Exception {
		if(request.signature.equals("original message")==false){
			return;
		}
		
		if(request.command.equals("I wanna match another")){
			match_request(request, packet);
		}
	}
	
	private void match_request(Request request, DatagramPacket packet) throws Exception {
		Surfer requester = get_requester(request, packet);
		if(does_surfer_has_another(requester)){
			tell_another(requester);
		}
		else {
			surfer = requester;
			tell_reject(packet);
			System.out.println("rejected");
			System.out.println("surfer was updated:");
			System.out.println("    global_ip: " + surfer.get_global().ip);
			System.out.println("    global_port: " + surfer.get_global().port);
		}
	}
	
	private boolean does_surfer_has_another(Surfer requester){
		System.out.println("culculating surfer's differences...");
		if(surfer.is_available() == false){
			System.out.println("there are no waiting surfer.");
			return false;
		}
		
		System.out.println("waiting surfer's IP: " + surfer.get_global().ip);
		System.out.println("request surfer's IP: " + requester.get_global().ip);
		System.out.println("waiting surfer's port: " + surfer.get_global().port);
		System.out.println("request surfer's port: " + requester.get_global().port);
				
		if(surfer.equals(requester) == true){
			System.out.println("different IP.");
			return false;
		}
		
		return true;
	}
	
	private Surfer get_requester(Request request, DatagramPacket packet){
		InetSocketAddress sockaddr = (InetSocketAddress)packet.getSocketAddress();
		InetAddress ip = sockaddr.getAddress();

		IpPort local = new IpPort();
		local.ip = request.local_ip;
		local.port = request.local_port;
		
		IpPort global = new IpPort();
		global.ip = ip.getHostAddress();
		global.port = sockaddr.getPort();
		
		Surfer requester = new Surfer();
		requester.set(local, global);
		return requester;
	}
	
	private void tell_another(Surfer requester) throws Exception {
		tell_each(surfer, requester);
		tell_each(requester, surfer);
	}
	
	private void tell_each(Surfer from, Surfer to) throws Exception {
		Tell tell = new Tell();
		tell.local_ip = from.get_local().ip;
		tell.local_port = from.get_local().port;
		tell.global_ip = from.get_global().ip;
		tell.global_port = from.get_global().port;
		
		String response = mapper.writeValueAsString(tell);
		byte reply_data[] = response.getBytes();
        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, InetAddress.getByName(to.get_global().ip), to.get_global().port);
        DatagramSocket ds = new DatagramSocket();
        ds.send(dp);
        ds.close();
        
        System.out.println("I send response:");
        System.out.println("    content:");
        System.out.println(response);
        System.out.println("    to...");
        System.out.println("        IP: " + to.get_global().ip);
        System.out.println("        InetAddress: " + InetAddress.getByName(to.get_global().ip).toString());
        System.out.println("        port: " + to.get_global().port);
	}
	
	private void tell_reject(DatagramPacket packet) throws Exception {
		String response = mapper.writeValueAsString(new Reject());
		byte reply_data[] = response.getBytes();
		InetSocketAddress sockaddr = (InetSocketAddress)packet.getSocketAddress();
        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, sockaddr.getAddress(), sockaddr.getPort());
        DatagramSocket ds = new DatagramSocket();
        ds.send(dp);
        ds.close();
	}
	
	static final int port = 9696;
	
	private ObjectMapper mapper;
	private Surfer surfer;
	private DatagramSocket socket;
	
	


}


