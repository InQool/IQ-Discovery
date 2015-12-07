package com.inqool.dcap.discovery.security.mojeid.data;
import java.io.Serializable;

/**
 * Prepravka pro vstupni URL parametry pro prihlaseni pomoci mojeID.
 * @author ACTIVE24, s.r.o.
 */
public class CustomerRequest implements Serializable {
	//=============== ATRIBUTY ==================================================
	private static final long serialVersionUID = 6375686398674893259L;
	private String frontendUrl;
	// ... dalsi potrebne atributy pro prihlaseni pomoci mojeID, napr.: 
	//  private String sessionId;

	
	
	//============== KOSTRUKTORY A TOVARNI METODY ===============================
	public CustomerRequest(){
	}

	
	
	//============== METODY TRIDY ===============================================

	//============== VEREJNE METODY INSTANCE ====================================	
	public String getFrontendUrl() {
		return frontendUrl;
	}

	public void setFrontendUrl(String frontendUrl) {
		this.frontendUrl = frontendUrl;
	}


	@Override
	public int hashCode() {
		final int prime = 13;
		int result = 178;
		result = prime * result
				+ ((frontendUrl == null) ? 0 : frontendUrl.hashCode());		
		return result;
	}

	@Override
	public String toString() {
		return CustomerRequest.class.getName() +" [frontendUrl=" + frontendUrl + "]";
	}
	
	
	//============== SOUKROME METODY INSTANCE ===================================

	//============== VNORENE A VNITRNI TRIDY ====================================

	//============== OSTATNÍ (MAIN A AUTOMATICKY GENEROVANE METODY) =============
}