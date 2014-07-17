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

@SuppressWarnings("all")
public class MobileAction {
	private String name;

	private Vector parameters = new Vector<String[]>();;

	public MobileAction() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MobileAction(String name) {
		this.name = name;
	}

	public Vector<String[]> getParameters() {
		return parameters;
	}

	public Vector getFormatedParameters() {
		Vector vector = new Vector();
		for (int i = 0; i < parameters.size(); i++) {
			String[] fields = new String[((String[]) parameters.get(i)).length + 1];
			fields[0] = this.getName();
			for (int j = 1; j < fields.length; j++)
				fields[j] = ((String[]) parameters.get(i))[j - 1];
			vector.add(fields);
		}

		return vector;
	}

	public void addParameter(String[] parameter) {
		this.parameters.add(parameter);
	}

	public void showDetails() {
		Vector v = this.getFormatedParameters();
		System.out.println("          Ação= " + this.getName());
		System.out.println("          ");
		for (int i = 0; i < v.size(); i++) {
			String[] s = (String[]) v.get(i);
			for (int j = 1; j < s.length; j++)
				System.out.print(s[j] + "|");
			System.out.println("          ");
		}
		System.out.println("");
	}

	@Override
	public String toString() {
		Vector v = this.getFormatedParameters();
		StringBuffer sb = new StringBuffer();
		sb.append("          Ação= ").append(this.getName()).append("\n");
		for (int i = 0; i < v.size(); i++) {
			String[] s = (String[]) v.get(i);
			for (int j = 1; j < s.length; j++)
				sb.append(s[j] + "|");
		}
		return sb.toString();
	}

}
