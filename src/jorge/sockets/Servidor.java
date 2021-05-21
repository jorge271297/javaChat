package jorge.sockets;

import java.awt.BorderLayout;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class Servidor {
	
	public static void main(String[] args) {
		MarcoServidor miMarco = new MarcoServidor();
		miMarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}

class MarcoServidor extends JFrame implements Runnable {
	
	private	JTextArea areatexto;
	
	public MarcoServidor(){
		setBounds(800,300,280,350);				
		
		//Se crea una lamina y se agrega al marco estableceindo el borderLayout
		JPanel milamina = new JPanel();
		milamina.setLayout(new BorderLayout());
		
		//Se inicia el textarea y se agrega alcentro de la lamina creada anteriormente
		areatexto = new JTextArea();
		milamina.add(areatexto,BorderLayout.CENTER);
		
		//Seagrega la lamina al marco
		add(milamina);
		
		setVisible(true);
		
		Thread miHilo = new Thread(this);
		miHilo.start();
	}

	@Override
	public void run() {
		//System.out.println("Estoy a la escucha");
		try {
			ServerSocket miServerSocket = new ServerSocket(9999);
			
			String nick, ip, mensaje;
			
			PaqueteEnvio paqueteRecibido;
			
			ArrayList<String> listaIp = new ArrayList<String>();
			
			while (true) {
				Socket miSocket = miServerSocket.accept();
								
				ObjectInputStream paquete_datos = new ObjectInputStream(miSocket.getInputStream());
				paqueteRecibido = (PaqueteEnvio) paquete_datos.readObject();
				
				nick = paqueteRecibido.getNick();
				ip = paqueteRecibido.getIp();
				mensaje = paqueteRecibido.getMensaje();
				
				if(!mensaje.equals("OnLine")) {
					areatexto.append("\n" + nick + ": " + mensaje + " para " + ip);
					
					Socket enviaDestinatario = new Socket(ip, 9090);
					ObjectOutputStream paqueteRenvio = new ObjectOutputStream(enviaDestinatario.getOutputStream());
					paqueteRenvio.writeObject(paqueteRecibido);
					
					paqueteRenvio.close();
					enviaDestinatario.close();
					miSocket.close();
				} else {
					/*----------------------------Detecta online---------------------------------------*/
					InetAddress localizacion = miSocket.getInetAddress();
					String IpRemota = localizacion.getHostAddress();
					//System.out.println("Online " + IpRemota);
					listaIp.add(IpRemota);
					paqueteRecibido.setListaIps(listaIp);
					for (String z:listaIp) {
						//System.out.println("Array: " + z);
						Socket enviaDestinatario = new Socket(z, 9090);
						ObjectOutputStream paqueteRenvio = new ObjectOutputStream(enviaDestinatario.getOutputStream());
						paqueteRenvio.writeObject(paqueteRecibido);
						paqueteRenvio.close();
						enviaDestinatario.close();
						miSocket.close();
					}
					/*----------------------------Detecta online---------------------------------------*/
				}
				/*DataInputStream flujoEntrada = new DataInputStream(miSocket.getInputStream());
				String mensajeTexto = flujoEntrada.readUTF();
				areatexto.append("\n" + mensajeTexto);*/
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}