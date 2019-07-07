package atomixraft.server;

import atomixraft.handler.Comando;
import atomixraft.handler.Mapa;

import java.util.concurrent.*;

public class MapManager implements Runnable {

	private Mapa mapa;
	private BlockingQueue < Comando > Fila_F2;

	public MapManager(BlockingQueue < Comando > f2) {
		Fila_F2 = f2;
		mapa = new Mapa();
	}

	// Recebe Comandos de  F2 para inserir na memoria
	public void run() {
		Comando comando;
		int opcao;
		int flag = -1;
		System.out.println("MapManager rodando");
		while (true) {
			try {
				comando = (Comando) Fila_F2.take();
				opcao = comando.getOperacao();
				// CREATE
				if (opcao == 1) {
					flag = mapa.create(comando.getChave(), comando.getValor());
				}
				// GET
				else if (opcao == 2) {
					comando.getCliente().println(new String(mapa.read(comando.getChave())));
					flag = 0;
				}
				// UPDATE
				else if (opcao == 3) {
					flag = mapa.update(comando.getChave(), comando.getValor());
				}
				// DELETE
				else if (opcao == 4) {
					flag = mapa.delete(comando.getChave());
				}
				if (flag == 1) comando.getCliente().println("Falha na operacao!");
			} catch(InterruptedException e) {} catch(NullPointerException a) {}
		}
	}

}