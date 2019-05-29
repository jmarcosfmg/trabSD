package util.memory;

import java.util.Map;
import java.util.HashMap;
import java.math.BigInteger; 

public class Mapa {

	private Map < BigInteger, byte[] > mapa;

	public Mapa() {
		this.mapa = new HashMap < >();
	}

	public boolean existe(BigInteger o1) {
		if (mapa.get(o1) == null) return false;
		else return true;
	}

	public int create(BigInteger o1, byte[] o2) {
		if (!existe(o1)) {
			mapa.put(o1, o2);
			return 0;
		} else return - 1;
	}

	public int update(BigInteger o1, byte[] o2) {
		if (existe(o1)) {
			mapa.remove(o1);
			mapa.put(o1, o2);
			return 0;
		} else return 1;
	}

	public int delete(BigInteger o1) {
		if (existe(o1)) {
			mapa.remove(o1);
			return 0;
		} else return 1;
	}

	public byte[] read(BigInteger o1) {
		return mapa.get(o1);
	}

	public Map < BigInteger, byte[] > getMapa() {
		return mapa;
	}

}