package br.com.fenix.processatexto.processar.scriptGoogle

import br.com.fenix.processatexto.model.enums.Api
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ScriptGoogle {
    // Conta jhonnysallesnoschag@hotmail.com
    private const val contaPrincipal = "https://script.google.com/macros/s/AKfycbxJwcl6j65Cjm6uKKCJPh21q5hJbey1jy3piqrxeSXOi4frWRs/exec"
    private const val contaSecundaria = "https://script.google.com/macros/s/AKfycbwlcX-qGQ60pyAl9X_bgxzfPU72ZNPMbBnbx2Ep9JxPdD9BIr0/exec"

    //Contas MigracaoCordilheira
    private const val contaMigracao1 = "https://script.google.com/macros/s/AKfycbyPE6SHmeN7HhIOOBAWXbaPvcujs4L2Mpm_FkK9N_iOSbUD7tfobftkOstvbEQl-Xnm/exec"
    private const val contaMigracao2 = "https://script.google.com/macros/s/AKfycbzf9dIrKTncwQKS5TOCAI2QWmkEXP6T4EgMLeZGmsr-hnTpvMI/exec"
    private const val contaMigracao3 = "https://script.google.com/macros/s/AKfycbzdOorc6pYHHjBPhvgy6URo0BO3QcjePY_UqPqdugKt2bX8fWM/exec"
    private const val contaMigracao4 = "https://script.google.com/macros/s/AKfycbw3npy923ZROsyKyclugR3fcgYj0AnfENwwHntbov8geWey844c/exec"

    @Throws(IOException::class)
    fun translate(langFrom: String, langTo: String, text: String, conta: Api): String {
        // Script criado na conta https://script.google.com/home/start
        var urlStr = ""
        urlStr += when (conta) {
            Api.CONTA_PRINCIPAL -> contaPrincipal
            Api.CONTA_SECUNDARIA -> contaSecundaria
            Api.CONTA_MIGRACAO_1 -> contaMigracao1
            Api.CONTA_MIGRACAO_2 -> contaMigracao2
            Api.CONTA_MIGRACAO_3 -> contaMigracao3
            Api.CONTA_MIGRACAO_4 -> contaMigracao4
            else -> contaPrincipal
        }
        urlStr += "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8) + "&target=" + langTo + "&source=" + langFrom
        val url = URL(urlStr)
        val response = StringBuilder()
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.setRequestProperty("User-Agent", "Mozilla/5.0")
        val `in` = BufferedReader(InputStreamReader(con.inputStream, StandardCharsets.UTF_8))
        var inputLine: String?
        while (`in`.readLine().also { inputLine = it } != null) {
            response.append(inputLine)
        }
        `in`.close()
        return response.toString()
    }
}