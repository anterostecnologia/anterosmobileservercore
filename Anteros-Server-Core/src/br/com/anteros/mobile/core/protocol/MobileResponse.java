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
public class MobileResponse {
	/*
	 * VECTOR[0]
	 */
	private String status;

	/*
	 * VECTOR[1]{TABLENAME, FIELDS...} TableName|Fields
	 */
	private String tableName;
	private String[] fields;

	/*
	 * DADOS A PARTIR DO REGISTRO 2 (VECTOR[2])
	 */
	private Vector data;
	public static final String OK = "OK";

	public MobileResponse() {
		this.fields = new String[0];
		this.data = new Vector();
		this.status = "OK";
		this.tableName = "";
	}

	public MobileResponse(Vector retornoFinal) {
		this();
		setFormattedParameters(data);
	}

	public MobileResponse(String status, String tableName) {
		this();
		this.status = status;
		this.tableName = tableName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String[] getFields() {
		return fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public Vector getData() {
		return data;
	}

	public void setData(Vector data) {
		this.data = data;
	}

	public Vector getFormattedParameters() {
		/*
		 * [Status] -> 0 [TableName] -> 1 [Fiel1,Field2,FieldN] -> 2
		 * [Data1,Data2,DataN] -> 3 a N
		 */
		Vector vector = new Vector();
		vector.addElement(new String[] { status });
		vector.addElement(new String[] { tableName });
		if (fields.length > 0) {
			String[] tableData = new String[fields.length];
			for (int i = 0; i < fields.length; i++)
				tableData[i] = fields[i];
			vector.addElement(tableData);
			for (int i = 0; i < data.size(); i++)
				vector.addElement(data.elementAt(i));
		}
		return vector;
	}

	public void setFormattedParameters(Vector vector) {
		/*
		 * [Status] -> 0 [TableName] -> 1 [Fiel1,Field2,FieldN] -> 2
		 * [Data1,Data2,DataN] -> 3 a N
		 */

		/*
		 * Verifica se o Vetor está preenchido
		 */
		if (!(vector == null)) {
			if (vector.size() > 0) {
				/*
				 * verifica se o vector do status não é nulo
				 */
				if (!(vector.elementAt(0) == null)) {
					/*
					 * Seta o Status
					 */
					String[] dataTable = (String[]) vector.elementAt(0);
					this.setStatus(dataTable[0]);
				}
				/*
				 * verifica se o vetor da tabela não é nulo
				 */
				if (!(vector.elementAt(1) == null)) {
					String[] dataTable = (String[]) vector.elementAt(1);
					/*
					 * seta o nome da Tabela
					 */
					this.setTableName(dataTable[0]);
					/*
					 * Verifica se vector com os fields não é nulo
					 */
					if (!(vector.elementAt(2) == null)) {
						dataTable = (String[]) vector.elementAt(2);
						/*
						 * Verifica se o Array de Strings com os fields não é
						 * nulo
						 */
						if (!(dataTable == null)) {
							if (dataTable.length > 0) {
								fields = dataTable;
								for (int i = 3; i < vector.size(); i++)
									data.addElement(vector.elementAt(i));
							}
						}
					}
				}
			}
		}

	}

	public void showDetails() {

		System.out.println("Status= " + status);
		System.out.println("TableName= " + tableName);
		System.out.println("Fields=");
		for (int i = 0; i < fields.length; i++) {
			System.out.print("    " + fields[i]);
			if (i < (fields.length - 1))
				System.out.print("|");
		}
		System.out.println("");
		System.out.println("Data=");
		for (int i = 0; i < this.getData().size(); i++) {
			String[] values = (String[]) this.getData().elementAt(i);

			System.out.print("    ");
			for (int j = 0; j < values.length; j++) {
				System.out.print(values[j]);
				if (j < (values.length - 1))
					System.out.print("|");
			}
			System.out.println("");
		}
		System.out.println("");
	}

	public String getShowDetailsHtml() {

		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("<body>");
		if (status.equals("OK"))
			sb.append("<b>Status</b><h4 style='color:blue'>" + status + "</h4><br>");
		else
			sb.append("<b>Status</b><h4 style='color:red'>" + status + "</h4><br>");

		sb.append("<b>TableName</b>= " + tableName + "<br>");
		sb.append("<b>Fields</b>=<br><FONT FACE='Courier New' SIZE='2'>");
		for (int i = 0; i < fields.length; i++) {
			sb.append("    " + fields[i]);
			if (i < (fields.length - 1))
				sb.append("|");
		}
		sb.append("</FONT><br>");
		sb.append("<b>Data</b>=<br><FONT FACE='Courier New' SIZE='2'>");
		for (int i = 0; i < this.getData().size(); i++) {
			String[] values = (String[]) this.getData().elementAt(i);
			sb.append("    ");
			for (int j = 0; j < values.length; j++) {
				sb.append(values[j]);
				if (j < (values.length - 1))
					sb.append("|");
			}
			sb.append("<br>");

		}
		sb.append("</FONT><br>");
		sb.append("</body>");
		sb.append("</html>");

		return sb.toString();
	}
}
