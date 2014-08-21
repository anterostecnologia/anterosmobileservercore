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
import java.util.Collections;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.mobile.core.protocol.MobileAction;
import br.com.anteros.mobile.core.protocol.MobileRequest;
import br.com.anteros.mobile.core.protocol.MobileResponse;
import br.com.anteros.mobile.core.synchronism.engine.SynchronismManager;
import br.com.anteros.mobile.core.util.ConvertTypes;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.metadata.annotation.type.CallableType;
import br.com.anteros.persistence.session.ProcedureResult;
import br.com.anteros.persistence.session.query.StoredProcedureSQLQuery;

@SuppressWarnings("serial")
@Entity
@DiscriminatorValue(value = "PROCEDIMENTO")
public class ProcedureSynchronism extends Synchronism implements Comparator<ParameterSynchronism> {

	private static Logger log = LoggerFactory.getLogger(ProcedureSynchronism.class);

	@Column(name = "PROCEDURE_PARAM_OUT")
	private String procedureParamOut;

	public MobileResponse execute(SynchronismManager synchronismManager, MobileRequest mobileRequest,
			MobileAction mobileAction) {

		log.debug("Iniciou execução PROCEDIMENTO " + getName() + " ##" + mobileRequest.getClientId());
		MobileResponse mobileResponse = new MobileResponse("", "");

		try {

			ProcedureResult result;
			for (String[] values : mobileAction.getParameters()) {

				log.debug(new StringBuffer("Executando chamada do PROCEDIMENTO [").append(getName())
						.append("] no banco de dados").append(" ##" + mobileRequest.getClientId()).toString());

				if (values.length != this.getInputParameters().length) {
					log.error(new StringBuffer("Erro Executando Ação ").append(mobileAction.getName())
							.append(" Tipo: PROCEDURE -> ").append(this.getName()).append(" ")
							.append("Número de parâmetros incorretos !").append(" ##" + mobileRequest.getClientId())
							.toString());

					mobileResponse.setStatus(new StringBuffer("Erro Executando Ação ").append(mobileAction.getName())
							.append(" Tipo: PROCEDURE -> ").append(this.getName()).append(" ")
							.append("Número de parâmetros incorretos !").toString());
					break;
				}

				synchronismManager.getSqlSession().setClientId(mobileRequest.getClientId());
				result = null;
				try {
					StoredProcedureSQLQuery storedProcedureSQLQuery = synchronismManager.getSqlSession().createStoredProcedureQuery(this.getName(), ConvertTypes.convertParametersStrings(this.getInputParameters(), values)).outputParametersName(new String[] { this.getProcedureParamOut() });
					storedProcedureSQLQuery.timeOut(15);
					result = storedProcedureSQLQuery.execute();
				
					if (result == null) {
						log.error(new StringBuffer("Erro Executando Ação ").append(mobileAction.getName())
								.append(" Tipo: PROCEDURE -> ").append(this.getName()).append(" ")
								.append("O parâmetro ").append(this.getProcedureParamOut()).append(" da PROCEDURE ")
								.append(this.getName()).append(" retornou NULO !")
								.append(" ##" + mobileRequest.getClientId()).toString());

						mobileResponse.setStatus(new StringBuffer("O parâmetro ").append(this.getProcedureParamOut())
								.append(" da PROCEDURE ").append(this.getName()).append(" retornou NULO !").toString());
					} else {
						mobileResponse.setStatus(result.getOutPutParameter(this.getProcedureParamOut()) + "");
					}
				} finally {
					if (result != null)
						result.close();
				}

			}
		} catch (Exception e) {
			log.error(
					new StringBuffer("Erro executando ação ").append(mobileAction.getName()).append(" SP ")
							.append(this.getName()).append(" -> ").append(" ").append(e.getMessage())
							.append(" ##" + mobileRequest.getClientId()).toString(), e);
			mobileResponse.setStatus(new StringBuffer("Erro executando ação ").append(mobileAction.getName())
					.append(" SP ").append(this.getName()).append(" -> ").append(e).toString());
		}
		log.debug("Finalizou execução PROCEDIMENTO " + getName() + " ##" + mobileRequest.getClientId());
		return mobileResponse;

	}

	public String getProcedureParamOut() {
		return procedureParamOut;
	}

	public void setProcedureParamOut(String procedureParamOut) {
		this.procedureParamOut = procedureParamOut;
	}

	public ParameterSynchronism[] getParameters() {
		ArrayList<ParameterSynchronism> result = new ArrayList<ParameterSynchronism>();
		if (this.getItems() != null) {
			for (Synchronism synchronism : this.getItems())
				result.add((ParameterSynchronism) synchronism);
		}
		return result.toArray(new ParameterSynchronism[] {});
	}

	public ParameterSynchronism[] getInputParameters() {
		ArrayList<ParameterSynchronism> result = new ArrayList<ParameterSynchronism>();
		if (this.getItems() != null) {
			for (Synchronism synchronism : this.getItems()) {
				if (((ParameterSynchronism) synchronism).getParameterType() == ParameterSynchronism.INPUT)
					result.add((ParameterSynchronism) synchronism);
			}
		}

		Collections.sort(result, new Comparator<ParameterSynchronism>() {

			public int compare(ParameterSynchronism o1, ParameterSynchronism o2) {
				return o1.getSequence().compareTo(o2.getSequence());
			}
		});

		return result.toArray(new ParameterSynchronism[] {});
	}

	public ParameterSynchronism[] getParametersSortBySequence() {
		ArrayList<ParameterSynchronism> result = new ArrayList<ParameterSynchronism>();
		if (this.getItems() != null) {
			for (Synchronism synchronism : this.getItems())
				result.add((ParameterSynchronism) synchronism);
		}
		Collections.sort(result, this);
		return result.toArray(new ParameterSynchronism[] {});
	}

	public Long getLastSequence() {
		Long result = 0L;
		if (this.getItems() != null) {
			for (Synchronism synchronism : this.getItems())
				if (synchronism instanceof ParameterSynchronism) {
					if (result < ((ParameterSynchronism) synchronism).getSequence())
						result = ((ParameterSynchronism) synchronism).getSequence();
				}
		}
		return result;
	}

	public void renumberSequence() {
		long sequence = 1;
		if (this.getItems() != null) {
			for (Synchronism synchronism : this.getItems())
				if (synchronism instanceof ParameterSynchronism) {
					((ParameterSynchronism) synchronism).setSequence(sequence);
					sequence++;
				}
		}
	}

	public void moveUp(ParameterSynchronism parameterSynchronism) {
		if (parameterSynchronism.getSequence() > 1) {
			ParameterSynchronism previous = getPreviousParameter(parameterSynchronism.getSequence());
			previous.setSequence(previous.getSequence() + 1);
			parameterSynchronism.setSequence(parameterSynchronism.getSequence() - 1);
		}
	}

	public void moveDown(ParameterSynchronism parameterSynchronism) {
		if (parameterSynchronism.getSequence() < getItems().size()) {
			ParameterSynchronism next = getNextParameter(parameterSynchronism.getSequence());
			next.setSequence(next.getSequence() - 1);
			parameterSynchronism.setSequence(parameterSynchronism.getSequence() + 1);
		}
	}

	private ParameterSynchronism getPreviousParameter(long sequence) {
		ParameterSynchronism result = null;
		for (ParameterSynchronism parameter : this.getParameters()) {
			if (parameter.getSequence() < sequence) {
				if ((result == null) || (parameter.getSequence() > result.getSequence()))
					result = parameter;
			}
		}
		return result;
	}

	private ParameterSynchronism getNextParameter(long sequence) {
		ParameterSynchronism result = null;
		for (ParameterSynchronism parameter : this.getParameters()) {
			if (parameter.getSequence() > sequence) {
				if ((result == null) || (parameter.getSequence() < result.getSequence()))
					result = parameter;
			}
		}
		return result;
	}

	public int compare(ParameterSynchronism parameter1, ParameterSynchronism parameter2) {
		if (parameter1.getSequence() > parameter2.getSequence())
			return -1;
		else if (parameter1.getSequence() < parameter2.getSequence())
			return 1;

		return 0;
	}

	@Override
	public String toString() {
		return "PROCEDIMENTO "+getId()+" - "+getDescription();
	}

}
