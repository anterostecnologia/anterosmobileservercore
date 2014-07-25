package br.com.anteros.mobile.core.synchronism.engine;

import java.sql.SQLException;

import br.com.anteros.mobile.core.protocol.MobileRequest;
import br.com.anteros.mobile.core.protocol.MobileResponse;
import br.com.anteros.mobile.core.synchronism.exception.ActionNotFoundException;
import br.com.anteros.mobile.core.synchronism.exception.ApplicationNotFoundException;
import br.com.anteros.mobile.core.synchronism.model.ApplicationSynchronism;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;

public class SynchronismManager {

	private SQLSession sqlSession;

	private SynchronismRequestQueue requestQueue;

	private DictionaryManager dictionaryManager;
	
	private SQLSessionFactory sqlSessionFactory;

	public SynchronismManager(SQLSession sqlSession, DictionaryManager dictionaryManager, SQLSessionFactory sqlSessionFactory) {
		this.sqlSession = sqlSession;
		this.requestQueue = new SynchronismRequestQueue();
		this.dictionaryManager = dictionaryManager;
		this.sqlSessionFactory = sqlSessionFactory;
	}

	public SynchronismRequestQueue getRequestQueue() {
		return requestQueue;
	}

	public MobileResponse executeRequest(MobileRequest mobileRequest) throws ApplicationNotFoundException,
			ActionNotFoundException, Exception {
		ApplicationSynchronism app = (this.getApplicationByName(mobileRequest.getApplication(), mobileRequest.getClientId()));
		if (app == null)
			throw new ApplicationNotFoundException(mobileRequest.getApplication());
		
		return app.processRequest(this, mobileRequest);
	}

	public ApplicationSynchronism getApplicationByName(String name, String clientId) {
		return dictionaryManager.getApplicationByName(name, clientId);
	}

	public SQLSession getSqlSession() throws SQLException, Exception {
		if (sqlSession == null) {
			sqlSession = sqlSessionFactory.getCurrentSession();
		}
		return sqlSession;
	}

	public void setSqlSession(SQLSession sqlSession) {
		this.sqlSession = sqlSession;
	}

	public void clearDictionary() {
		dictionaryManager.clear();
	}

	public DictionaryManager getDictionaryManager() {
		return dictionaryManager;
	}

	public void closeSession() {
		try {
			sqlSession.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
