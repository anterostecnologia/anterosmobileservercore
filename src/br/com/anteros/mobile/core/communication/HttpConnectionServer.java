package br.com.anteros.mobile.core.communication;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.mobile.core.protocol.MobileRequest;
import br.com.anteros.mobile.core.protocol.MobileResponse;
import br.com.anteros.mobile.core.zip.ZOutputStream;
import br.com.anteros.mobile.core.zip.ZStream;

@SuppressWarnings("all")
public abstract class HttpConnectionServer extends HttpServlet {

	private static Logger log = LoggerFactory.getLogger(HttpConnectionServer.class);

	public HttpConnectionServer() {
		isGet = false;
	}

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		if ("IPhone".equalsIgnoreCase(request.getHeader("user-agent"))
				|| "Android".equalsIgnoreCase(request.getHeader("user-agent"))) {
			
			response.setContentType("application/x-gzip");
			response.setCharacterEncoding("UTF-8");
		} else {
			response.setContentType("application/octetc-stream");
			response.setCharacterEncoding("ISO-8859-1");
		}

		processPost(request, response);
	}

	public abstract MobileResponse executeAction(HttpSession session, MobileRequest mobileRequest,
			HttpServletRequest request, HttpServletResponse response);

	public String sendDataMobile(String option, Vector records) {
		StringBuilder data = new StringBuilder();
		data.append(option).append("*");
		for (int i = 0; i < records.size(); i++) {
			String fields[] = (String[]) (String[]) (String[]) records.elementAt(i);
			for (int j = 0; j < fields.length; j++) {
				data.append(fields[j]);
				if (j < fields.length - 1) {
					data.append("$");
					continue;
				}
				if (i < records.size() - 1)
					data.append("|");
			}
		}

		return data.append("#").toString();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		processRequest(request, response);
	}

	public String getServletInfo() {
		return "";
	}

	private void processPost(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		MobileRequest mobileRequest = null;
		DataInputStream in = null;
		StringBuilder resultMsg = new StringBuilder();
		try {
			Vector opPost = new Vector();
			int numOtion = 0;
			StringBuilder sb = new StringBuilder();

			if ("IPhone".equals(request.getHeader("user-agent"))) {
				numOtion = 1;
				BufferedReader br = request.getReader();

				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = br.read(charBuffer)) != -1)
					sb.append(charBuffer, 0, bytesRead);

				String body = sb.toString();
				System.out.println("Corpo : " + body);

				String[] split = body.split("<>");
				String option = split[0];
				String data = split[1];

				String fields[] = { option, data };

				System.out.println("Opcao : " + option);
				System.out.println("Dados : " + data);
				opPost.addElement(fields);
			} else {
				in = new DataInputStream(request.getInputStream());
				numOtion = in.readInt();
				for (int k = 0; k < numOtion; k++) {

					// Option
					String option = in.readUTF();

					// Data
					int length = in.readInt();
					byte[] b = new byte[length];
					in.readFully(b);
					String data = new String(b, "UTF-8");

					String fields[] = { option, data };
					opPost.addElement(fields);
				}
			}

			opPost.trimToSize();
			for (int j = 0; j < numOtion; j++) {
				String options[] = (String[]) (String[]) opPost.elementAt(j);
				String option = options[0];
				String msg = options[1];
				Vector vectorRecords = null;
				if (!msg.equalsIgnoreCase("nothing")) {
					StringTokenizer records = new StringTokenizer(msg, "|");
					String resultFields[] = null;
					int i = 0;
					vectorRecords = new Vector();
					for (; records.hasMoreTokens(); vectorRecords.addElement(resultFields)) {
						StringTokenizer fields = new StringTokenizer(records.nextToken(), "$");
						resultFields = new String[fields.countTokens()];
						for (i = 0; fields.hasMoreTokens(); i++) {
							String str = fields.nextToken();
							if (str.equals("_"))
								resultFields[i] = "";
							else
								resultFields[i] = str;
						}
					}
				}

				mobileRequest = new MobileRequest();
				mobileRequest.setFormattedHeader(option);
				mobileRequest.setFormattedActions(vectorRecords);

				MobileResponse mobileResponse = executeAction(session, mobileRequest, request, response);

				log.debug("Executou Requisição" + " ##" + mobileRequest.getClientId());

				log.debug("Montando string de retorno" + " ##" + mobileRequest.getClientId());
				if (mobileResponse != null) {
					String ret = sendDataMobile(mobileResponse.getStatus(), mobileResponse.getFormattedParameters());
					resultMsg.append(ret);
				}
			}

			resultMsg = new StringBuilder(resultMsg.toString().trim());
			// resultMsg = resultMsg.substring(0, resultMsg.length() - 1);

			System.out.println(response.getHeaderNames());
			if ("IPhone".equalsIgnoreCase(request.getHeader("user-agent"))
					|| "Android".equalsIgnoreCase(request.getHeader("user-agent"))) {
				response.setHeader("Set-Cookie", "JSESSIONID=" + session.getId());
				log.debug("Obtendo array de bytes" + " ##" + mobileRequest.getClientId());
				byte[] bytesUtf8 = resultMsg.toString().getBytes("UTF-8");
				log.debug("Escrevendo resposta" + " ##" + mobileRequest.getClientId());
				if ("gzip".equals(request.getHeader("accept-encoding"))) {
					response.setHeader("Content-Encoding", "gzip");
					response.setHeader("Accept-Encoding", "gzip");
					GZIPOutputStream zOut = new GZIPOutputStream(response.getOutputStream());
					BufferedOutputStream daos = new BufferedOutputStream(zOut);
					daos.write(bytesUtf8);
					log.debug("Enviando... ##" + mobileRequest.getClientId());
					daos.flush();
					daos.close();
				} else {
					BufferedOutputStream daos = new BufferedOutputStream(response.getOutputStream());
					daos.write(bytesUtf8);
					log.debug("Enviando... ##" + mobileRequest.getClientId());
					daos.flush();
					daos.close();
				}
			} else {
				log.debug("Obtendo array de bytes" + " ##" + mobileRequest.getClientId());
				byte[] bytesIso = resultMsg.toString().getBytes("ISO-8859-1");
				log.debug("Escrevendo resposta" + " ##" + mobileRequest.getClientId());
				ZOutputStream zOut = new ZOutputStream(response.getOutputStream(), ZStream.Z_BEST_SPEED);
				zOut.write(bytesIso);
				log.debug("Enviando... ");
				zOut.flush();
				zOut.close();
			}
			log.debug("Tamanho Descompactado " + resultMsg.toString().getBytes().length + " bytes ##"
					+ mobileRequest.getClientId());

			log.debug("Enviado" + " ##" + mobileRequest.getClientId());
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Erro: " + ex.getMessage() + " ##" + (mobileRequest != null ? mobileRequest.getClientId() : ""));
		}

	}

	private boolean isGet;
}
