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
package br.com.anteros.mobile.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import br.com.anteros.mobile.core.synchronism.exception.ParameterConvertionException;
import br.com.anteros.mobile.core.synchronism.model.ParameterSynchronism;
import br.com.anteros.persistence.parameter.NamedParameter;
import br.com.anteros.persistence.parameter.SubstitutedParameter;
import br.com.anteros.persistence.session.type.LobType;
import br.com.anteros.persistence.sql.datasource.JDBCDataSource;
import br.com.anteros.persistence.util.StringUtils;

public class ConvertTypes {
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	public static String replaceParametersSubst(String sql, ParameterSynchronism[] parameters, String[] values)
			throws ParameterConvertionException {
		ParameterSynchronism param = null;
		String result = sql;
		String value = "";
		try {
			for (int i = 0; i < parameters.length; i++) {
				param = (ParameterSynchronism) parameters[i];
				value = values[i];
				if (param.getParameterType() == ParameterSynchronism.SUBSTITUITION)
					result = StringUtils.replaceAll(result, ":" + param.getName(), value);
			}
		} catch (Exception e) {
			throw new ParameterConvertionException(param.getName(), value);
		}
		return result;
	}

	public static NamedParameter[] convertNamedParametersStrings(ParameterSynchronism[] parameters, String[] values)
			throws ParameterConvertionException {
		if ((values == null) || (parameters == null))
			return new NamedParameter[] {};
		ArrayList<NamedParameter> result = new ArrayList<NamedParameter>();
		int i = 0;
		String value = "";
		for (ParameterSynchronism parameterSynchronism : parameters) {
			try {
				if (parameterSynchronism.getParameterType() != ParameterSynchronism.SUBSTITUITION) {
					value = values[i];
					Object newValue = convertType(parameterSynchronism.getParameterDataType().intValue(), value);

					result.add(new NamedParameter(parameterSynchronism.getName(), newValue));
				}
				i++;
			} catch (Exception e) {
				throw new ParameterConvertionException(parameterSynchronism.getName(), value);
			}
		}

		return result.toArray(new NamedParameter[] {});
	}

	public static Object[] convertParametersStrings(ParameterSynchronism[] parameters, String[] values)
			throws ParameterConvertionException {

		if ((values == null) || (parameters == null))
			return new Object[] {};

		ArrayList<Object> result = new ArrayList<Object>();

		int i = 0;
		String value = "";
		for (ParameterSynchronism param : parameters) {
			try {
				value = values[i];
				Object newValue = convertType(param.getParameterDataType().intValue(), value);
				result.add(newValue);
				i++;
			} catch (Exception e) {
				throw new ParameterConvertionException(param.getName(), value);
			}
		}

		return result.toArray(new Object[] {});
	}

	protected static Object convertType(int type, String paramVal) {
		Object result = paramVal;
		java.util.Date d;
		switch (type) {
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			result = paramVal;
			break;

		case Types.DATE:
			if (!StringUtils.isEmpty(paramVal)) {
				try {
					if (paramVal.contains(":")) {
						d = timeStampFormat.parse(paramVal);
						result = new Timestamp(d.getTime());
					} else {
						d = dateFormat.parse(paramVal);
						result = new java.sql.Date(d.getTime());
					}

				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			} else
				result = null;
			break;

		case Types.BIGINT:
			if (!StringUtils.isEmpty(paramVal))
				result = new BigInteger(paramVal);
			else
				result = null;
			break;

		case Types.SMALLINT:
			if (!StringUtils.isEmpty(paramVal))
				result = Short.parseShort(paramVal);
			else
				result = null;
			break;

		case Types.TINYINT:
			if (!StringUtils.isEmpty(paramVal))
				result = Byte.parseByte(paramVal);
			else
				result = null;
			break;

		case Types.NUMERIC:
			if (!StringUtils.isEmpty(paramVal))
				result = new BigDecimal(paramVal);
			else
				result = null;
			break;

		case Types.DECIMAL:
			if (!StringUtils.isEmpty(paramVal))
				result = new BigDecimal(paramVal);
			else
				result = null;
			break;
		case Types.INTEGER:
			if (!StringUtils.isEmpty(paramVal))
				result = Integer.parseInt(paramVal);
			else
				result = null;
			break;

		case Types.FLOAT:
			if (!StringUtils.isEmpty(paramVal))
				result = Float.parseFloat(paramVal);
			else
				result = null;
			break;

		case Types.DOUBLE:
			if (!StringUtils.isEmpty(paramVal))
				result = Double.parseDouble(paramVal);
			else
				result = null;
			break;

		case Types.TIME:
			if (!StringUtils.isEmpty(paramVal)) {
				try {
					result = new Time(timeFormat.parse(paramVal).getTime());
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			} else
				result = null;
			break;
		case Types.BLOB:
		case Types.CLOB:
			if (!StringUtils.isEmpty(paramVal)) {
				try {
					result = new LobType(Base64.decode(paramVal), type);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else
				result = null;
			break;

		case Types.TIMESTAMP:
			if (!StringUtils.isEmpty(paramVal)) {
				try {
					if (paramVal.contains(":"))
						result = new Timestamp(timeStampFormat.parse(paramVal).getTime());
					else
						result = new Timestamp(dateFormat.parse(paramVal).getTime());
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			} else
				result = null;
			break;
		}
		return result;
	}
}
