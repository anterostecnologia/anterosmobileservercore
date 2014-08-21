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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.mobile.core.protocol.MobileAction;
import br.com.anteros.mobile.core.protocol.MobileRequest;
import br.com.anteros.mobile.core.protocol.MobileResponse;
import br.com.anteros.mobile.core.synchronism.engine.SynchronismManager;
import br.com.anteros.mobile.core.synchronism.handler.MobileResponseHandler;
import br.com.anteros.mobile.core.util.ConvertTypes;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.metadata.annotation.Lob;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.session.query.SQLQuery;

@SuppressWarnings("serial")
@Entity
@DiscriminatorValue(value = "TABELA")
public class TableSynchronism extends Synchronism {

	private static Logger log = LoggerFactory.getLogger(TableSynchronism.class);

	@Column(name = "TABLE_NAME_MOBILE", length = 100)
	private String tableNameMobile;

	@Column(name = "TABLE_SQL")
	@Lob
	private byte[] tableSql;

	public String[] getFieldsString() {
		ArrayList<String> result = new ArrayList<String>();
		FieldSynchronism fields[] = this.getFields();
		for (FieldSynchronism fieldSynchronism : fields)
			result.add(fieldSynchronism.getName());
		return result.toArray(new String[] {});
	}

	public String[] getFieldsSqlString() {
		ArrayList<String> result = new ArrayList<String>();
		FieldSynchronism fields[] = this.getFields();
		for (FieldSynchronism fieldSynchronism : fields)
			result.add(fieldSynchronism.getSqlFieldName());
		return result.toArray(new String[] {});
	}

	public MobileResponse execute(SynchronismManager synchronismManager, MobileRequest mobileRequest,
			MobileAction mobileAction) {
		MobileResponse mobileResponse = new MobileResponse("", "");
		try {
			log.debug("Executando Ação " + mobileAction.getName() + " Tipo: SQL ##"+mobileRequest.getClientId());
			if (this.getParameters().length > 0) {
				if (mobileAction.getParameters().size() == 0) {
					log.error(
							new StringBuffer("Erro Executando Ação ").append(mobileAction.getName())
									.append(" Tipo: SQL Tabela ").append(this.getTableNameMobile())
									.append(" -> Número de Parâmetros incorretos ").append(" ##" + mobileRequest.getClientId()).toString());
					mobileResponse.setStatus("Erro executando ação " + mobileAction.getName() + " Tabela "
							+ this.getTableNameMobile() + " -> Número de Parâmetros incorretos ");
					return mobileResponse;
				}

				if ((mobileAction.getParameters().get(0).length != this.getParameters().length)) {
					log.error(
							new StringBuffer("Erro Executando Ação ").append(mobileAction.getName())
									.append(" Tipo: SQL Tabela ").append(this.getTableNameMobile())
									.append(" -> Número de Parâmetros incorretos ").append(" ##" + mobileRequest.getClientId()).toString());
					mobileResponse.setStatus("Erro executando ação " + mobileAction.getName() + " Tabela "
							+ this.getTableNameMobile() + " -> Número de Parâmetros incorretos ");
					return mobileResponse;
				}
			}
			
			log.debug("Convertendo parâmetros para parâmetros nomeados."+" ##" + mobileRequest.getClientId());
			NamedParameter[] params = ConvertTypes.convertNamedParametersStrings(this.getParameters(), mobileAction
					.getParameters().get(0));
			String sql =  new String(this.getTableSql());
			log.debug(new StringBuffer("Executando o sql no banco de dados ").append(sql).
					append(" ##" + mobileRequest.getClientId()).toString());
			sql = ConvertTypes.replaceParametersSubst(sql, this.getParameters(), mobileAction.getParameters().get(0));
			log.debug(new StringBuffer("Sql substituido ").append(sql).
					append(" ##" + mobileRequest.getClientId()).toString());
			synchronismManager.getSqlSession().setClientId(mobileRequest.getClientId());
			SQLQuery query = synchronismManager.getSqlSession().createQuery(sql,
					params);
			query.resultSetHandler(new MobileResponseHandler(synchronismManager, this));
			mobileResponse = (MobileResponse) query.getSingleResult();
			mobileResponse.setStatus("OK");
			log.debug(new StringBuffer("Executou Ação ").append(mobileAction.getName()).append(" ##" + mobileRequest.getClientId()).toString());
		} catch (ArrayIndexOutOfBoundsException e) {
			log.error(new StringBuffer("Erro Executando Ação ")
					.append(mobileAction.getName()).append(" Tipo: SQL").append(e.getMessage()).append(" ##" + mobileRequest.getClientId()).toString());
			mobileResponse.setStatus("Erro executando ação " + mobileAction.getName() + " Tabela "
					+ this.getTableNameMobile() + " -> Parâmetros incorretos ");
			return mobileResponse;
		} catch (Exception e) {
			log.error(new StringBuffer("Erro Executando Ação ")
					.append(mobileAction.getName()).append(" Tipo: SQL").append(e.getMessage()).append(" ##" + mobileRequest.getClientId()).toString());
			mobileResponse.setStatus("Erro executando ação " + mobileAction.getName() + " Tabela "
					+ this.getTableNameMobile() + " -> " + e.getMessage());
			return mobileResponse;
		}
		return mobileResponse;

	}

	public int getCountInputParams() {
		int result = 0;
		for (ParameterSynchronism param : this.getParameters()) {
			if (param.getParameterType() == ParameterSynchronism.INPUT)
				result++;
		}
		return result;
	}

	public String getTableNameMobile() {
		return tableNameMobile;
	}

	public void setTableNameMobile(String tableNameMobile) {
		this.tableNameMobile = tableNameMobile;
	}

	public byte[] getTableSql() {
		return tableSql;
	}

	public void setTableSql(byte[] tableSql) {
		this.tableSql = tableSql;
	}

	public ParameterSynchronism[] getParameters() {
		ArrayList<ParameterSynchronism> result = new ArrayList<ParameterSynchronism>();
		if (getItems() != null) {
			for (Synchronism synchronism : getItems()) {
				if (synchronism instanceof ParameterSynchronism)
					result.add((ParameterSynchronism) synchronism);
			}
		}
		return result.toArray(new ParameterSynchronism[] {});
	}

	public FieldSynchronism[] getFields() {
		ArrayList<FieldSynchronism> result = new ArrayList<FieldSynchronism>();
		if (getItems() != null) {
			for (Synchronism synchronism : getItems()) {
				if (synchronism instanceof FieldSynchronism)
					result.add((FieldSynchronism) synchronism);
			}
		}
		return result.toArray(new FieldSynchronism[] {});

	}
	
	@Override
	public String toString() {
		return "TABELA SINCRONISMO "+getId()+" - "+getDescription();
	}

}
