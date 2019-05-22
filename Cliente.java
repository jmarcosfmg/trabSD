import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Vector;
import java.math.BigInteger;

public class Cliente {

	private static Socket server = null;
	public Cliente() {}

	public static void main(String[] args) {
		try {
			server = new Socket("127.0.0.1", 5082);
			if (server == null) System.out.println("Erro, socket = null");
			Thread menu = new Thread(new Interface(server));
			Thread receptor = new Thread(new Receptor(server));
			menu.start();
			receptor.start();

		} catch(UnknownHostException a) {
			System.out.println("Erro na conexao. Socket nao criado.\nErr." + a);
		}
		catch(IOException b) {
			System.out.println("Erro na conexao. Socket nao criado.\nErr." + b);
		}
	}

}


class Interface implements Runnable {

	private Socket socket_cliente = null;
	private String menu = "Menu:\n" + "1 - Create\n" + "2 - Read\n" + "3 - Update\n" + "4 - Delete\n" + "5 - Quit\n";
	private int resposta;

	public Interface(Socket a) {
		socket_cliente = a;
		if (socket_cliente == null) System.out.println("Erro, saida = null");
	}

	public void main() {
		Thread op = new Thread(this);
		op.start();
		try {
			op.join();
		} catch(InterruptedException a) {}
	}

	public void run() {
		Scanner leitor = new Scanner(System. in );
		while (! (Thread.currentThread().isInterrupted())) {
			System.out.println(menu);
			resposta = leitor.nextInt();
			validaResposta(resposta);
		}
	}

	private void validaResposta(int a) {
		BigInteger chave;
		String valor;
		Scanner ler = new Scanner(System. in );
		if (a > 0 && a < 6) {
			try {
				PrintWriter saida = new PrintWriter(socket_cliente.getOutputStream(), true);
				if (saida == null) System.out.println("Erro, saida = null");
				else {
					saida.println(a);
					try {
						if (a == 1) {
							System.out.println("Entre com a chave:");
							chave = ler.nextBigInteger();
							ler.nextLine();
							System.out.println("Entre com o valor:");
							valor = ler.nextLine();
							saida.println(chave);
							saida.println(valor);
						}
						else if (a == 2) {
							System.out.println("Entre com a chave:");
							chave = ler.nextBigInteger();
							saida.println(chave);
						}
						else if (a == 3) {
							System.out.println("Entre com a chave:");
							chave = ler.nextBigInteger();
							ler.nextLine();
							System.out.println("Entre com o valor:");
							valor = ler.nextLine();
							saida.println(chave);
							saida.println(valor);
						}
						else if (a == 4) {
							System.out.println("Entre com a chave:");
							chave = ler.nextBigInteger();
							saida.println(chave);
						}
						else if (a == 5) {
							System.out.println("Tchau");
							System.exit(0);
						}
					} catch(Exception falha) {
						System.out.println("Erro - " + falha + ".\n Verifique os valores inseridos e tente de novo.");
					}
				}
			} catch(IOException e) {}
		} else System.out.println("Erro: Opção inválida");
	}

}


class Receptor implements Runnable {

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