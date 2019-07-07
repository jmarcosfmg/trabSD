package atomixraft.server;

import atomixraft.handler.Comando;

import java.util.concurrent.*;

public class Fila1Manager implements Runnable {

	private BlockingQueue <Comando> Fila_F1;
	private BlockingQueue < Comando > Fila_F2;
	private BlockingQueue < Comando > Fila_F3;

	public Fila1Manager(BlockingQueue < Comando > f1, BlockingQueue < Comando > f2, BlockingQueue < Comando > f3) {
		Fila_F1 = f1;
		Fila_F2 = f2;
		Fila_F3 = f3;
	}

	// Distribuicao de comandos de F1 para F2 e F3
	public void run() {
		Comando comando;
		System.out.println("Fila1Manager rodando");
		while (true) {
			try {
				comando = (Comando) Fila_F1.take();
				System.out.println(comando);
				Fila_F2.put(comando);
				Fila_F3.put(comando);
			} catch(InterruptedException e) {

			}
		}
	}

}