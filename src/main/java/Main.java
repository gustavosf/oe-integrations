import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class Main extends HttpServlet {
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    Map<String, String> headers = getHeadersInfo(req);

    String hook = headers.get("X-GitHub-Event");

    if (hook == "pull_request") processPullRequest(getJSONPayload(req));

  }

  protected void processPullRequest(JSONObject payload) {

    // int someInt = payload.getInt("intParamName");
    // String someString = payload.getString("stringParamName");
    // JSONObject nestedObj = payload.getJSONObject("nestedObjName");
    // JSONArray arr = payload.getJSONArray("arrayParamName");

    // String responseMessage;
    // responseMessage = ""

  }

  /**
   * Retorna o paylaod enviado pelo webhook
   * @return Payload
   */
  private JSONObject getJSONPayload(HttpServletRequest req) {
    StringBuffer jb = new StringBuffer();
    String line = null;

    try {
      BufferedReader reader = req.getReader();
      while ((line = reader.readLine()) != null)
        jb.append(line);
    } catch (Exception e) { /*report an error*/ }

    try {
      JSONObject jsonPayload = JSONObject.fromObject(jb.toString());
    } catch (ParseException e) {
      // crash and burn
      throw new IOException("Error parsing JSON request string");
    }

    return jsonPayload;
  }

  /**
   * Retorna os headers da requisição
   * @return Headers
   */
  private Map<String, String> getHeadersInfo(HttpServletRequest req) {
    Map<String, String> map = new HashMap<String, String>();
    Enumeration headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String key = (String) headerNames.nextElement();
      String value = request.getHeader(key);
      map.put(key, value);
    }

    return map;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    resp.getWriter().print("Hello fella!");

  }

  public static void main(String[] args) throws Exception {
    Server server = new Server(Integer.valueOf(System.getenv("PORT")));
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);
    context.addServlet(new ServletHolder(new Main()),"/*");
    server.start();
    server.join();
  }
}
