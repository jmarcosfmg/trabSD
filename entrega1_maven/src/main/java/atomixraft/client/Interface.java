package atomixraft.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.math.BigInteger;

public class Interface implements Runnable {

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