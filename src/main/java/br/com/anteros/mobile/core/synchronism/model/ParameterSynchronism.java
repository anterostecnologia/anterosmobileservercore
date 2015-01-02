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

import br.com.anteros.mobile.core.protocol.MobileAction;
import br.com.anteros.mobile.core.protocol.MobileRequest;
import br.com.anteros.mobile.core.protocol.MobileResponse;
import br.com.anteros.mobile.core.synchronism.engine.SynchronismManager;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Entity;

@SuppressWarnings("serial")
@Entity
@DiscriminatorValue(value = "PARAMETRO")
public class ParameterSynchronism extends Synchronism {

	public static final int SUBSTITUITION = 999999;

	public static final int INPUT = 0;

	public static final int OUTPUT = 1;
	
	public static final int INPUT_OUTPUT = 2;

	@Column(name="PARAMETER_DATA_TYPE")
	private Long parameterDataType;

	@Column(name="PARAMETER_TYPE")
	private Long parameterType;
	
	@Column(name="SEQUENCE_PARAMETER")
	private Long sequence;

	public MobileResponse execute(SynchronismManager synchronismManager,
			MobileRequest mobileRequest, MobileAction mobileAction)
			throws Exception {
		return null;
	}

	public Long getParameterDataType() {
		return parameterDataType;
	}

	public void setParameterDataType(Long parameterDataType) {
		this.parameterDataType = parameterDataType;
	}

	public Long getParameterType() {
		return parameterType;
	}

	public void setParameterType(Long parameterType) {
		this.parameterType = parameterType;
	}
	
	public void setParameterType(int parameterType) {
		this.parameterType = new Long(parameterType);
	}

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	@Override
	public String toString() {
		return "PARÂMETRO "+getId()+" - "+getDescription()+" -> "+getName();
	}
}
