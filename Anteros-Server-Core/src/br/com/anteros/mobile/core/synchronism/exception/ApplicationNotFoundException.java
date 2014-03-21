package br.com.anteros.mobile.core.synchronism.exception;

public class ApplicationNotFoundException extends SynchronismException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7831390113711195829L;
	
	public ApplicationNotFoundException(String application) {
		super("Aplicação "+application+" não encontrada !");
	}

	

}
