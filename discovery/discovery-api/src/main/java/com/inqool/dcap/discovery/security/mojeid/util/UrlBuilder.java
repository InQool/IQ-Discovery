package com.inqool.dcap.discovery.security.mojeid.util;

import javax.servlet.http.HttpServletResponse;

/**
 * Builder pro sestaveni URL adresy. 
 * @author ACTIVE24, s.r.o.
 */
public class UrlBuilder {	
	//=============== ATRIBUTY ==================================================
	private StringBuffer url;

	//============== KOSTRUKTORY A TOVARNI METODY ===============================
	public UrlBuilder() {
		this.url = new StringBuffer();
	}
	
	public UrlBuilder(String url) {
		this();
		this.url.append(url);
	}

	
	
	//============== METODY TRIDY ===============================================

	//============== VEREJNE METODY INSTANCE ====================================
	/**
	 * Pripoji parametr k URL.
	 * @param name
	 * @param value
	 * @return
	 */
	public UrlBuilder appendParam(String name, String value){
		if( url.indexOf("?") == -1 ) {
			this.url.append("?");
		}else{
			this.url.append("&");
		}
		
		this.url.append(name).append("=").append(value);
		return this;			
	}
	
	/**
	 * Nastavi uplne novou URL, smaze predchozi hodnotu URL.
	 * @param url
	 */
	public void setUrl(String url){
		this.url = new StringBuffer(url);
	}
	
	/**
	 * Vrati sestavenou URL.
	 * @return
	 */
	public String build(){
		return this.url.toString();			
	}
	
	/**
	 * Vrati sestavenou a encodovanou URL. 
	 * @param httpResp
	 * @return
	 */
	public String buildAndEncode(HttpServletResponse httpResp){
		return httpResp.encodeRedirectURL( this.url.toString() );			
	}
	

	//============== SOUKROME METODY INSTANCE ===================================

	//============== VNORENE A VNITRNI TRIDY ====================================

	//============== OSTATNÍ (MAIN A AUTOMATICKY GENEROVANE METODY) =============
}
