package jorge.sockets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Cliente {

	public static void main(String[] args) {
		MarcoCliente miMarco = new MarcoCliente();
		miMarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}

class MarcoCliente extends JFrame {
	
	public MarcoCliente(){
		setBounds(600,300,280,350);
		
		//Se crea una instancia a de la lamina de cliente y se agrega al marco
		LaminaMarcoCliente milamina = new LaminaMarcoCliente();
		add(milamina);
		
		
		setVisible(true);
		addWindowListener(new EnvioOnLine());
	}	
	
}

class EnvioOnLine extends WindowAdapter {
	public void windowOpened(WindowEvent e) {
		try {
			Socket miSocket = new Socket("192.168.1.227", 9999);
			PaqueteEnvio datos = new PaqueteEnvio();
			datos.setMensaje("OnLine");
			ObjectOutputStream paquete_datos = new ObjectOutputStream(miSocket.getOutputStream());
			paquete_datos.writeObject(datos);
			miSocket.close();
		} catch (Exception e2) {
			System.out.println(e2.getMessage());
		}
	}
}

class LaminaMarcoCliente extends JPanel implements Runnable {
	
	private JTextField campo1;
	private JLabel nick;
	private JButton miboton;
	private JTextArea campoChat;
	private JComboBox ip;
	
	
	public LaminaMarcoCliente() {
		//se inicia el campo nick y se agrega a la lamina
		String nick_Usuario = JOptionPane.showInputDialog("Nick: ");
		JLabel n_nick = new JLabel("Nick: ");
		add(n_nick);
		nick = new JLabel(nick_Usuario);
		add(nick);
		
		//Se crea el encabezado o titulo de la app despues de agrega a la lamina
		JLabel texto = new JLabel(" Online: ");
		add(texto);
		
		//se inicia el campo ip y se agrega a la lamina
		ip = new JComboBox();
		/*ip.addItem("Usuatio 1");
		ip.addItem("Usuatio 2");
		ip.addItem("Usuatio 3");*/
		//ip.addItem("192.168.1.227");
		add(ip);
		
		//se inicia el texare donde se mostraran los mensajes
		campoChat = new JTextArea(12, 20);
		add(campoChat);
		
		//Se inicia el texfile y se agrega a la lamina
		campo1 = new JTextField(20);
		add(campo1);	
		
		//Se inicia el boton y se agrega a la lamina
		miboton = new JButton("Enviar");
		EnviaTexto miEvento = new EnviaTexto();
		miboton.addActionListener(miEvento);
		add(miboton);
		Thread miHilo = new Thread(this);
		miHilo.start();
	}
	
	private class EnviaTexto implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			//System.out.println(campo1.getText());
			campoChat.append("\n" + campo1.getText());
			try {
				Socket miSocket = new Socket("192.168.1.227", 9999);
				
				PaqueteEnvio datos = new PaqueteEnvio();
				datos.setNick(nick.getText());
				datos.setIp(ip.getSelectedItem().toString());
				datos.setMensaje(campo1.getText());
				
				ObjectOutputStream paqueteDatos = new ObjectOutputStream(miSocket.getOutputStream());
				paqueteDatos.writeObject(datos);
				miSocket.close();
				
				//DataOutputStream flujoSalida = new DataOutputStream(miSocket.getOutputStream());
				//flujoSalida.writeUTF(campo1.getText());
				//flujoSalida.close();
				
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println(e1.getMessage());
			}
		}
	}

	@Override
	public void run() {
		try {
			ServerSocket servidor_cliente = new ServerSocket(9090);
			Socket cliente;
			PaqueteEnvio paqueteRecibido;
			while (true) {
				cliente = servidor_cliente.accept();
				ObjectInputStream flujoEntrada = new ObjectInputStream(cliente.getInputStream());
				paqueteRecibido = (PaqueteEnvio) flujoEntrada.readObject();
				if(!paqueteRecibido.getMensaje().equals("OnLine")) {
					campoChat.append("\n" + paqueteRecibido.getNick() + ": " + paqueteRecibido.getMensaje()); 
				} else {
					//campoChat.append("\n" + paqueteRecibido.getListaIps());
					ArrayList <String> IpsMenu = new ArrayList<String>();
					IpsMenu = paqueteRecibido.getListaIps();
					ip.removeAllItems();
					for(String z : IpsMenu) {
						ip.addItem(z);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
}

class PaqueteEnvio implements Serializable {
	
	private String nick, ip, mensaje;
	private ArrayList<String> listaIps;

	public ArrayList<String> getListaIps() {
		return listaIps;
	}

	public void setListaIps(ArrayList<String> listaIps) {
		this.listaIps = listaIps;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	
}