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

import br.com.anteros.mobile.core.protocol.MobileAction;
import br.com.anteros.mobile.core.protocol.MobileRequest;
import br.com.anteros.mobile.core.protocol.MobileResponse;
import br.com.anteros.mobile.core.synchronism.engine.SynchronismManager;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Entity;

@SuppressWarnings("serial")
@Entity
@DiscriminatorValue(value = "CAMPO")
public class FieldSynchronism extends Synchronism {

	@Column(name="SQL_FIELDNAME", length=100)
	private String sqlFieldName;

	@Column(name="FIELD_TYPE")
	private Long fieldType;

	public String getSqlFieldName() {
		return sqlFieldName;
	}

	public void setSqlFieldName(String sqlFieldName) {
		this.sqlFieldName = sqlFieldName;
	}

	public Long getFieldType() {
		return fieldType;
	}

	public void setFieldType(Long fieldType) {
		this.fieldType = fieldType;
	}

	public MobileResponse execute(SynchronismManager synchronismManager,
			MobileRequest mobileRequest, MobileAction mobileAction, Charset charset)
			throws Exception {
		return null;
	}
	
	@Override
	public String toString() {
		return "CAMPO "+getId()+" - "+getDescription()+"-> "+getName();
	}

}
