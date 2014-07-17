package br.com.anteros.mobile.core.synchronism.exception;

public class ActionNotFoundException extends SynchronismException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5703729099510652756L;
	
	public ActionNotFoundException(String action) {
		super("Açao "+action+" não encontrada !");
	}

	

}
