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
package br.com.anteros.mobile.core.protocol;

import java.util.Vector;

public class MobileRequest {

	public static final String ACTION_QUEUE = "AQ";
	public static final String ACTION_EXECUTE_QUEUE = "AE";
	public static final String ACTION_EXECUTE_IMMEDIATE = "AI";
	private String application;
	private String userAgent;
	private String clientId;
	private String requestMode = ACTION_EXECUTE_IMMEDIATE;
	private Vector<MobileAction> actions = new Vector<MobileAction>();

	public MobileRequest() {
	}

	public MobileRequest(Vector vector) {
		setFormattedActions(vector);
	}

	public MobileRequest(String application, String userAgent, String clientId, String requestMode) {
		this.application = application;
		this.userAgent = userAgent;
		this.clientId = clientId;
		this.requestMode = requestMode;

	}

	public void setFormattedHeader(String header) {
		String[] fields = header.split("\\|");
		if (fields.length == 4) {
			setApplication(fields[0]);
			setUserAgent(fields[1]);
			setClientId(fields[2]);
			setRequestMode(fields[3]);
		}
	}

	public String getFormattedHeader() {
		return new StringBuffer().append(getApplication()).append("|").append(getUserAgent()).append("|")
				.append(getClientId()).append("|").append(getRequestMode()).toString();
	}

	public Vector<MobileAction> getActions() {
		return actions;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void addAction(MobileAction mobileAction) {
		actions.add(mobileAction);
	}

	public void removeMobileAction(MobileAction mobileAction) {
		actions.remove(mobileAction);
	}

	public MobileAction getMobileActionByName(String name) {
		for (int i = 0; i < actions.size(); i++) {
			MobileAction mobileAction = actions.get(i);
			if (mobileAction.getName().equals(name))
				return mobileAction;
		}
		return null;

	}

	public MobileAction getMobileAction(int key) {
		return actions.get(key);
	}

	public Vector getFormatedActions() {
		Vector vector = new Vector();
		for (int i = 0; i < actions.size(); i++) {
			MobileAction action = actions.get(i);
			Vector actionFields = action.getFormatedParameters();
			for (int j = 0; j < actionFields.size(); j++)
				vector.add(actionFields.get(j));
		}
		return vector;
	}

	public void setFormattedActions(Vector vector) {
		MobileAction mobileAction;
		for (int i = 0; i < vector.size(); i++) {
			String[] action = (String[]) vector.get(i);
			String actionName = action[0];
			mobileAction = getMobileActionByName(actionName);
			if (mobileAction == null) {
				mobileAction = new MobileAction(actionName);
				addAction(mobileAction);
			}
			String[] params = new String[action.length - 1];
			for (int j = 1; j < action.length; j++)
				params[j - 1] = action[j];
			mobileAction.addParameter(params);
		}
	}

	public void showDetails() {

		System.out.println("     Aplicação= " + getApplication());
		System.out.println("     Agente= " + getUserAgent());
		System.out.println("     Id Cliente= " + getClientId());
		System.out.println("     Modo Requisição= " + getRequestMode());
		for (int x = 0; x < this.getActions().size(); x++) {
			MobileAction mobileAction = this.getActions().get(x);
			mobileAction.showDetails();
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("     Aplicação= ").append(getApplication()).append("\n");
		sb.append("     Agente= ").append(getUserAgent()).append("\n");
		sb.append("     Id Cliente= ").append(getClientId()).append("\n");
		sb.append("     Modo Requisição= " + getRequestMode());
		for (int x = 0; x < this.getActions().size(); x++) {
			MobileAction mobileAction = this.getActions().get(x);
			sb.append(mobileAction.toString());
		}
		return sb.toString();
	}

	public String getRequestMode() {
		return requestMode;
	}

	public void setRequestMode(String requestMode) {
		this.requestMode = requestMode;
	}
}
