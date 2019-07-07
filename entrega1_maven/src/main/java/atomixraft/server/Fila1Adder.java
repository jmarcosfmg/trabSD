package atomixraft.server;

import atomixraft.handler.Comando;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.math.BigInteger;
import java.util.Scanner;

public class Fila1Adder implements Runnable {

	private BlockingQueue < Comando > Fila_F1;
	private Socket conexao;

	public Fila1Adder(BlockingQueue < Comando > a, Socket s) {
		Fila_F1 = a;
		conexao = s;
	}

	public void run() {
		BigInteger chave = new BigInteger("0");
		int opcao = 0;
		byte[] valor = null;
		try {
			BufferedReader entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
			Scanner ler = new Scanner(System. in );
			PrintStream saida = null;
			saida = new PrintStream(conexao.getOutputStream());
			if (saida == null) System.out.println("Erro, saida nula");
			// primeiramente, espera-se a opcao do servidor
			while (true) {
				opcao = Integer.parseInt(entrada.readLine());
				// agora, verifica se string recebida é valida, pois
				// sem a conexão foi interrompida, a string é null.
				// Se isso ocorrer, deve-se terminar a execução.
				System.out.println("Cliente: " + conexao.getPort() + ". Opcao escolhida: " + opcao);
				if (opcao == 5) {
					throw new IOException();
				}
				// CREATE
				else if (opcao == 1) {
					chave = new BigInteger(entrada.readLine());
					valor = entrada.readLine().getBytes();
				}
				// GET
				else if (opcao == 2) {
					chave = new BigInteger(entrada.readLine());
					valor = null;
				}
				// UPDATE
				else if (opcao == 3) {
					chave = new BigInteger(entrada.readLine());
					valor = entrada.readLine().getBytes();
				}
				// DELETE
				else if (opcao == 4) {
					chave = new BigInteger(entrada.readLine());
					valor = null;
				}
				// Entrada de comandos em F1 pelo cliente
				Fila_F1.put(new Comando(opcao, chave, valor, new PrintStream(conexao.getOutputStream())));
			}
		} catch(Exception e) {
			try {
				System.out.println(conexao.getPort() + " se desconectou");
				conexao.close();
			} catch(IOException err) {}
		}
	}

}