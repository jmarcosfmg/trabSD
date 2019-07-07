package atomixraft.client;

import java.io.*;
import java.net.*;

public class Receptor implements Runnable {

	private Socket socket_cliente;

	public Receptor(Socket a) {
		socket_cliente = a;
	}

	public void main() {
		Thread op = new Thread(this);
		op.start();
		try {
			op.join();
		} catch(InterruptedException a) {}
	}

	public void run() {
		try {
			BufferedReader entrada = new BufferedReader(new InputStreamReader(socket_cliente.getInputStream()));
			String linha;
			while (! (Thread.currentThread().isInterrupted())) {
				// pega o que o servidor enviou
				linha = entrada.readLine();
				// verifica se é uma linha válida. Pode ser que a conexão
				// foi interrompida. Neste caso, a linha é null. Se isso
				// ocorrer, termina-se a execução saindo com break
				if (linha == null) {
					try {
						Thread.currentThread().sleep(5);
						System.out.println("Conexão encerrada!");
						System.exit(0);
					}
					catch(InterruptedException a) {
						System.out.println("Recepcao interrompida");
						break;
					}
				} else System.out.println(linha);
			}
		} catch(IOException a) {}
	}

}