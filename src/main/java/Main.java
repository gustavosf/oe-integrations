import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


public class Main extends HttpServlet {

  //*************************************************************************//
  //***  OVERRIDES   ********************************************************//
  //*************************************************************************//

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // Processa o header para verificar qual o evento que causou o hook
    Map<String, String> headers = getHeadersInfo(req);
    String hook = headers.get("X-Github-Event");
    String message;

    // Roteia para o método certo gerar a mensagem
    if (hook == null) {
      message = "Did not receive an event hook";
    } else if (hook.equals("pull_request")) {
      message = processPullRequest(getJSONPayload(req));
    } else {
      message = "Event hook \"" + hook + "\" not implemented yet";
    }

    // Mostra um feedback na tela, só para debug caso necessário
    PrintWriter writer = resp.getWriter();
    writer.print(message);

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

  //*************************************************************************//
  //***  HOOK PROCESSORS   **************************************************//
  //*************************************************************************//

  /**
   * Processa um evento de Pull Request
   * @param  payload Payload enviado pelo github
   * @return         Mensagem que o bot deverá enviar ao canal
   */
  protected String processPullRequest(JSONObject payload) {

    // Só envia mensagem caso seja uma abertura de pull
    if (!payload.optString("action").equals("opened")) return "";

    JSONObject pullRequest = payload.optJSONObject("pull_request");
    JSONObject user = pullRequest.optJSONObject("user");
    JSONObject repo = payload.optJSONObject("repository");
    JSONObject head = pullRequest.optJSONObject("head");
    JSONObject base = pullRequest.optJSONObject("base");
    String message;

    // Monta a mensagem a ser enviada para o slack
    message = ":octocat: New pull request for the repo *";
    message = message + repo.optString("name");
    message = message + "*: ";
    message = message + pullRequest.optString("html_url");
    message = message + "\n:octocat: *";
    message = message + user.optString("login");
    message = message + "* wants to merge *";
    message = message + pullRequest.optString("number");
    message = message + " commit(s)* into `";
    message = message + base.optString("label");
    message = message + "` from `";
    message = message + head.optString("label");
    message = message + "`\n:octocat: *Pull description:* ";
    message = message + pullRequest.optString("title");

    // Gera uma instância do bot
    SlackBot bot = new SlackBot(System.getenv("SLACK_HOST"),
            System.getenv("SLACK_TOKEN"));

    // Descobre para que canal está associado este repositório
    JSONObject channels;
    try {
      channels = new JSONObject(System.getenv("SLACK_REPO_CHANNEL"));
    } catch (JSONException e) {
      return "You need to specify a relation between repositories" +
              "and the respective slack channel to send the message";
    }

    String channel = channels.optString(repo.optString("full_name"));
    if (channel.equals("")) {
      return "There is no channel configured to receive updates from " +
              repo.optString("full_name");
    }

    bot.message(channel, message);
    return message;

  }

  //*************************************************************************//
  //***  FUNÇÕES AUXILIARES   ***********************************************//
  //*************************************************************************//

  /**
   * Retorna o paylaod enviado pelo webhook
   * @return Payload
   */
  private JSONObject getJSONPayload(HttpServletRequest req) {

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
  private Map<String, String> getHeadersInfo(HttpServletRequest req) {

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
