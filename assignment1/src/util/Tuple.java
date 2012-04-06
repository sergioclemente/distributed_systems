package util;

public class Tuple<T1, T2> {
	private T1 m_t1;
	private T2 m_t2;
	
	public Tuple(T1 t1, T2 t2) {
		this.m_t1 = t1;
		this.m_t2 = t2;
	}
	
	public T1 getT1() {
		return this.m_t1;
	}
	
	public T2 getT2() {
		return this.m_t2;
	}
}
