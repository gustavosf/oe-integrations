package intelimen.oebot.integration;

import intelimen.oebot.Main;
import intelimen.oebot.SlackBot;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Servlet para integrações entre Github e Slack
 */
public class Github extends Main {

    //***********************************************************************//
    //***  OVERRIDES   ******************************************************//
    //***********************************************************************//

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

        resp.getWriter().print("Hello github!");

    }

    //***********************************************************************//
    //***  HOOK PROCESSORS   ************************************************//
    //***********************************************************************//

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
}
