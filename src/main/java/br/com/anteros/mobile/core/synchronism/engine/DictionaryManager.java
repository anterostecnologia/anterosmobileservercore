/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.mobile.core.synchronism.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.mobile.core.synchronism.model.ApplicationSynchronism;
import br.com.anteros.mobile.core.synchronism.model.Synchronism;
import br.com.anteros.persistence.session.SQLSession;

@SuppressWarnings("unchecked")
public class DictionaryManager {

	private static Logger log = LoggerFactory.getLogger(DictionaryManager.class);
	private final Map<Long, Synchronism> cache = new HashMap<Long, Synchronism>();
	private SQLSession sqlSession;

	public void setSqlSession(SQLSession sqlSession) {
		this.sqlSession = sqlSession;
	}

	public synchronized ApplicationSynchronism getApplicationByName(String name, String clientId) {
		log.debug("Verificando se a aplicação [" + name + "] existe no cache do dicionário. ##" + clientId);
		for (Long id : cache.keySet()) {
			Synchronism synchronism = cache.get(id);
			if (synchronism != null) {
				if (synchronism instanceof ApplicationSynchronism) {
					if (((ApplicationSynchronism) synchronism).getName().equals(name)) {
						log.debug("Encontrou a aplicação " + name + " no cache. ##" + clientId);
						return (ApplicationSynchronism) synchronism;
					}
				}
			}
		}
		try {
			log.debug("Buscando a aplicação [" + name + "] no banco de dados. ##" + clientId);
			sqlSession.setClientId(clientId);
			ApplicationSynchronism app = (ApplicationSynchronism) sqlSession.createQuery(
					"SELECT * FROM MOBILE_OBJETO WHERE NOME_OBJETO = '" + name + "' ", ApplicationSynchronism.class).getSingleResult();
			if (app != null) {
				log.debug("Adicionando a aplicação [" + name + "] no cache. ##" + clientId);
				cache.put(app.getId(), app);
			}
			return app;
		} catch (Exception e) {
			log.error("Ocorreu um erro buscando a aplicação [" + name + "] no dicionário. ##" + clientId, e);
		}
		return null;
	}

	public synchronized List<ApplicationSynchronism> getAllApplications(String clientId) {
		log.debug("Buscando todas as aplicações. ##" + clientId);
		try {
			sqlSession.setClientId(clientId);
			sqlSession.getTransaction().begin();
			List<ApplicationSynchronism> listApp = (List<ApplicationSynchronism>) sqlSession.createQuery(
					"SELECT * FROM MOBILE_OBJETO WHERE TP_OBJETO = 'APLICACAO' ", ApplicationSynchronism.class).getResultList();
			sqlSession.getTransaction().rollback();
			if (listApp != null) {
				for (ApplicationSynchronism app : listApp) {
					log.debug("Adicionando a aplicação [" + app.getName() + "] no cache. ##" + clientId);
					cache.put(app.getId(), app);
				}
			}
			return listApp;
		} catch (Exception e) {
			log.error("Ocorreu um erro buscando as aplicações no dicionário. ##" + clientId, e);
		}
		return null;
	}

	public void clear() {
		cache.clear();
	}

	public SQLSession getSqlSession() {
		return sqlSession;
	}

}
