package intelimen.oebot;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Main extends HttpServlet {

  //*************************************************************************//
  //***  OVERRIDES   ********************************************************//
  //*************************************************************************//

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  //*************************************************************************//
  //***  FUNÇÕES AUXILIARES   ***********************************************//
  //*************************************************************************//

  /**
   * Retorna o paylaod enviado pelo webhook
   * @return Payload
   */
  protected JSONObject getJSONPayload(HttpServletRequest req) {

    StringBuffer buffer = new StringBuffer();
    String line;

    try {
      BufferedReader reader = req.getReader();
      while ((line = reader.readLine()) != null)
        buffer.append(line);
    } catch (Exception e) { /*report an error*/ }

    JSONObject jsonPayload;
    try {
      jsonPayload = new JSONObject(buffer.toString());
    } catch (JSONException e) {
      jsonPayload = null;
    }

    return jsonPayload;

  }

  /**
   * Retorna os headers da requisição
   * @return Headers
   */
  protected Map<String, String> getHeadersInfo(HttpServletRequest req) {

    Map<String, String> map = new HashMap<String, String>();
    Enumeration headerNames = req.getHeaderNames();

    while (headerNames.hasMoreElements()) {
      String key = (String) headerNames.nextElement();
      String value = req.getHeader(key);
      map.put(key, value);
    }

    return map;

  }

}
