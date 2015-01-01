package com.cheatdatabase.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

/**
 * Sendet HTTP-POST Daten an eine bestimmte URL. Copyright (c) 2010, 2011<br>
 * 
 * @author Dominik Erbsland
 * @version 1.0
 * @see http://www.anddev.org/http_post_sdk_10-t3493.html
 */
public class SendHTTPPostData {
	private boolean isAuthenticated;
	private ArrayList<BasicNameValuePair> pairs;
	private DefaultHttpClient httpclient;
	private HttpPost httppost;
	private InputStream conteudo;
	private String retornoConexao;

	public void parametrosHttp(String url, Map<String, String> variables) {
		this.httpclient = new DefaultHttpClient();
		this.httppost = new HttpPost(url);
		this.pairs = new ArrayList<BasicNameValuePair>();
		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (Iterator<String> i = keys.iterator(); i.hasNext();) {
				String key = i.next();
				pairs.add(new BasicNameValuePair(key, variables.get(key)));
			}
		}
	}

	public String go() {
		try {
			UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(pairs, "ISO-8859-1");

			/** Assign the POST data to the entity */
			httppost.setEntity(p_entity);

			/** Perform the actual HTTP POST */
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			conteudo = entity.getContent();
			this.retornoConexao = convertStreamToString(conteudo);
			Log.d("HttpPostConnection", ">>>>>>>>>>>>>>> " + retornoConexao);
			int status_code = response.getStatusLine().getStatusCode();
			httpclient.getCookieStore();
			if (status_code >= 300) {
				this.isAuthenticated = false;
			} else {
				this.isAuthenticated = true;
			}
		} catch (UnsupportedEncodingException uee) {
			// Woops
		} catch (IOException ioe) {
			// Woops
		} catch (IllegalStateException ise) {
			// woops
		}
		return retornoConexao;
	}

	public String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}
}