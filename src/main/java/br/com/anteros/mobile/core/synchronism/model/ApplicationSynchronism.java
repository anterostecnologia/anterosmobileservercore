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
package br.com.anteros.mobile.core.synchronism.model;

import java.nio.charset.Charset;
import java.util.ArrayList;

import br.com.anteros.core.utils.AnterosStandardsCharsets;
import br.com.anteros.mobile.core.protocol.MobileAction;
import br.com.anteros.mobile.core.protocol.MobileRequest;
import br.com.anteros.mobile.core.protocol.MobileResponse;
import br.com.anteros.mobile.core.synchronism.engine.SynchronismManager;
import br.com.anteros.mobile.core.synchronism.exception.ActionNotFoundException;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Entity;


@SuppressWarnings("all")
@Entity
@DiscriminatorValue(value = "APLICACAO")
public class ApplicationSynchronism extends Synchronism {

	@Column(name = "DRIVER_CLASS", length = 250)
	private String driverClass;

	@Column(name = "JDBC_URL", length = 250)
	private String jdbcUrl;

	@Column(name = "USERNAME", length = 50)
	private String user;

	@Column(name = "PASSWORD", length = 50)
	private String password;

	@Column(name = "INCREMENT_POOL")
	private Long acquireIncrement = 2L;

	@Column(name = "INITIAL_POOL_SIZE")
	private Long initialPoolSize = 5L;

	@Column(name = "MAX_POOL_SIZE")
	private Long maxPoolSize = 50L;

	@Column(name = "MIN_POOL_SIZE")
	private Long minPoolSize = 5L;
	
	@Column(name = "DEFAULT_SCHEMA", length = 100)
	private String defaultSchema;

	@Column(name = "DEFAULT_CATALOG", length = 100)
	private String defaultCatalog;

	@Column(name = "TP_POOL", length = 30)
	private String connectionPoolType;
	
	@Column(name = "DIALECT", length = 30)
	private String dialect;
	
	@Column(name = "JNDI_NAME", length = 250)
	private String jndiName;
	
	@Column(name = "CHARSET_NAME", length = 15)
	private String charsetName;

	public MobileResponse processRequest(SynchronismManager synchronismManager, MobileRequest mobileRequest) throws Exception {

		/*
		 * Status inicia em OK
		 */
		MobileResponse mobileResponse = new MobileResponse("OK", "");

		/*
		 * Verifica se todas as Actions do MobileRequest são válidas
		 */
		ActionSynchronism actionSynchronism = null;
		for (MobileAction mobileAction : mobileRequest.getActions()) {
			actionSynchronism = this.getActionByName(mobileAction.getName());
			if (actionSynchronism == null)
				throw new ActionNotFoundException(mobileAction.getName());
		}

		/*
		 * Se o Request é para ser adicionado na fila ou Request é para executar
		 * a fila adiciona o Request na Fila do Sincronismo para execução na
		 * sequência
		 */
		if (mobileRequest.getRequestMode().equals(MobileRequest.ACTION_QUEUE) || (mobileRequest.getRequestMode().equals(MobileRequest.ACTION_EXECUTE_QUEUE)))
			synchronismManager.getRequestQueue().addRequest(mobileRequest);

		/*
		 * Executa a fila de MobileRequest da Aplicação/Usuário
		 */
		if (mobileRequest.getRequestMode().equals(MobileRequest.ACTION_EXECUTE_QUEUE))
			mobileResponse = executeQueueUserAgent(synchronismManager, mobileRequest.getApplication(), mobileRequest.getUserAgent());

		/*
		 * Caso seja apenas para executar o Request, limpa a fila da
		 * Aplicação/Usuário e executa as Actions do Request
		 */
		if (mobileRequest.getRequestMode().equals(MobileRequest.ACTION_EXECUTE_IMMEDIATE)) {
			synchronismManager.getRequestQueue().clearQueueUserAgent(mobileRequest.getApplication(), mobileRequest.getUserAgent());
			mobileResponse = executeActions(synchronismManager, mobileRequest);
		}

		if (!mobileResponse.getStatus().startsWith(MobileResponse.OK))
			return mobileResponse;

		return mobileResponse;
	}

	private MobileResponse executeQueueUserAgent(SynchronismManager synchronismManager, String application, String userAgent) throws Exception {
		MobileRequest mobileRequest = null;
		MobileResponse mobileResponse = new MobileResponse("", "");
		while (true) {
			mobileRequest = synchronismManager.getRequestQueue().getRequest(application, userAgent);
			if (mobileRequest == null)
				return mobileResponse;
			mobileResponse = executeActions(synchronismManager, mobileRequest);
			if (!mobileResponse.getStatus().equals(MobileResponse.OK))
				return mobileResponse;
		}
	}

	private MobileResponse executeActions(SynchronismManager synchronismManager, MobileRequest mobileRequest) throws Exception {
		MobileResponse mobileResponse = new MobileResponse("", "");
		ActionSynchronism actionSynchronism = null;
		for (MobileAction mobileAction : mobileRequest.getActions()) {
			actionSynchronism = this.getActionByName(mobileAction.getName());
			if (actionSynchronism != null) {
				mobileResponse = actionSynchronism.execute(synchronismManager, mobileRequest, mobileAction, AnterosStandardsCharsets.getCharsetByName(charsetName));
				if (!mobileResponse.getStatus().startsWith(MobileResponse.OK))
					return mobileResponse;
			}
		}
		return mobileResponse;
	}

	public ActionSynchronism getActionByName(String action) {
		for (Synchronism synchronism : this.getItems()) {
			if (synchronism.getName().equals(action))
				return (ActionSynchronism) synchronism;
		}
		return null;
	}

	public MobileResponse execute(SynchronismManager synchronismManager, MobileRequest mobileRequest, MobileAction mobileAction, Charset charset) throws Exception {
		return null;
	}

	public ActionSynchronism[] getActions() {
		ArrayList<ActionSynchronism> result = new ArrayList<ActionSynchronism>();
		for (Synchronism synchronism : this.getItems())
			result.add((ActionSynchronism) synchronism);
		return result.toArray(new ActionSynchronism[] {});
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getAcquireIncrement() {
		return acquireIncrement;
	}

	public void setAcquireIncrement(Long acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
	}

	public Long getInitialPoolSize() {
		return initialPoolSize;
	}

	public void setInitialPoolSize(Long initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
	}

	public Long getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(Long maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public Long getMinPoolSize() {
		return minPoolSize;
	}

	public void setMinPoolSize(Long minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	public String getDefaultSchema() {
		return defaultSchema;
	}

	public void setDefaultSchema(String defaultSchema) {
		this.defaultSchema = defaultSchema;
	}

	public String getDefaultCatalog() {
		return defaultCatalog;
	}

	public void setDefaultCatalog(String defaultCatalog) {
		this.defaultCatalog = defaultCatalog;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public String getConnectionPoolType() {
		return connectionPoolType;
	}

	public void setConnectionPoolType(String connectionPoolType) {
		this.connectionPoolType = connectionPoolType;
	}

	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	@Override
	public String toString() {
		return "APLICAÇÃO "+getId()+" - "+getDescription()+" -> "+getName();
	}

	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}
}
