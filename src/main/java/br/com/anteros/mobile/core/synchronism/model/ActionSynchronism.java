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
import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.mobile.core.protocol.MobileAction;
import br.com.anteros.mobile.core.protocol.MobileRequest;
import br.com.anteros.mobile.core.protocol.MobileResponse;
import br.com.anteros.mobile.core.synchronism.engine.SynchronismManager;
import br.com.anteros.mobile.core.synchronism.exception.ObjectSynchNotFoundException;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Entity;

@SuppressWarnings("serial")
@Entity
@DiscriminatorValue(value = "ACAO")
public class ActionSynchronism extends Synchronism implements ISynchronism {

	private static Logger log = LoggerFactory.getLogger(ActionSynchronism.class);

	public static final int ACTION_IMPORT = 0;

	public static final int ACTION_EXPORT = 1;

	public MobileResponse execute(SynchronismManager synchronismManager, MobileRequest mobileRequest,
			MobileAction mobileAction, Charset charset) throws Exception {
		if (getItems() == null)
			throw new ObjectSynchNotFoundException(this.getName());
		if (getItems() != Collections.EMPTY_SET) {
			Iterator<Synchronism> it = getItems().iterator();
			if (it.hasNext()) {
				Synchronism synchronism = it.next();
				return synchronism.execute(synchronismManager, mobileRequest, mobileAction, charset);
			}
		}

		log.error(new StringBuffer("Erro executando ação ").append(mobileAction.getName())
				.append(". Não foi encontrado nenhum objeto para execução.")
				.append(" ##" + mobileRequest.getClientId()).toString());
		MobileResponse mobileResponse = new MobileResponse("", "");
		mobileResponse
				.setStatus(new StringBuffer("Erro executando ação ")
						.append(mobileAction.getName())
						.append(". Não foi encontrado nenhum objeto para execução. Verifique se a ação foi configurada corretamente.")
						.toString());
		return mobileResponse;
	}

	public Long getApplicationId() {
		return getObjectOwner().getId();
	}
	
	@Override
	public String toString() {
		return "AÇÃO "+getId()+" - "+getDescription()+" -> "+getName();
	}

}
