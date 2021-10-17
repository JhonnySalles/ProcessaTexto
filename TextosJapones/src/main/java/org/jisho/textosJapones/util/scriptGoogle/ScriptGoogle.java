package org.jisho.textosJapones.util.scriptGoogle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.jisho.textosJapones.model.enums.Api;


public class ScriptGoogle {

	// Conta jhonnysallesnoschag@hotmail.com
	final private static String contaPrincipal = "https://script.google.com/macros/s/AKfycbxJwcl6j65Cjm6uKKCJPh21q5hJbey1jy3piqrxeSXOi4frWRs/exec";
	final private static String contaSecundaria = "https://script.google.com/macros/s/AKfycbwlcX-qGQ60pyAl9X_bgxzfPU72ZNPMbBnbx2Ep9JxPdD9BIr0/exec";
	//Contas MigracaoCordilheira
	final private static String contaMigracao1 = "https://script.google.com/macros/s/AKfycbyPE6SHmeN7HhIOOBAWXbaPvcujs4L2Mpm_FkK9N_iOSbUD7tfobftkOstvbEQl-Xnm/exec";
	final private static String contaMigracao2 = "https://script.google.com/macros/s/AKfycbzf9dIrKTncwQKS5TOCAI2QWmkEXP6T4EgMLeZGmsr-hnTpvMI/exec";
	final private static String contaMigracao3 = "https://script.google.com/macros/s/AKfycbzdOorc6pYHHjBPhvgy6URo0BO3QcjePY_UqPqdugKt2bX8fWM/exec";
	final private static String contaMigracao4 = "https://script.google.com/macros/s/AKfycbw3npy923ZROsyKyclugR3fcgYj0AnfENwwHntbov8geWey844c/exec";

	public static String translate(String langFrom, String langTo, String text, Api conta) throws IOException {
		// Script criado na conta https://script.google.com/home/start
		String urlStr = "";

		switch (conta) {
		case CONTA_PRINCIPAL:
			urlStr += contaPrincipal;
			break;
		case CONTA_SECUNDARIA:
			urlStr += contaSecundaria;
			break;
		case CONTA_MIGRACAO_1:
			urlStr += contaMigracao1;
			break;
		case CONTA_MIGRACAO_2:
			urlStr += contaMigracao2;
			break;
		case CONTA_MIGRACAO_3:
			urlStr += contaMigracao3;
			break;
		case CONTA_MIGRACAO_4:
			urlStr += contaMigracao4;
			break;
		default:
			urlStr += contaPrincipal;
		}
				
		urlStr += "?q=" + URLEncoder.encode(text, "UTF-8") + "&target=" + langTo + "&source=" + langFrom;

		URL url = new URL(urlStr);
		StringBuilder response = new StringBuilder();
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

}