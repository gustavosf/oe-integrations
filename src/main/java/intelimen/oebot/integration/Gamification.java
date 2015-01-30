package intelimen.oebot.integration;

import intelimen.oebot.Main;
import intelimen.oebot.SlackBot;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Servlet para integrações entre o Gamification e Slack
 */
public class Gamification extends Main {

    //***********************************************************************//
    //***  OVERRIDES   ******************************************************//
    //***********************************************************************//

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Processa o header para verificar qual o evento que causou o hook
        Map<String, String> headers = getHeadersInfo(req);
        String hook = headers.get("X-Gamification-Event");
        String message;

        // Roteia para o método certo gerar a mensagem
        if (hook == null) {
            message = "Did not receive an event hook";
        } else if (hook.equals("king_of_the_day")) {
            message = processKingOfTheDay(getJSONPayload(req));
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

        resp.getWriter().print("Hello gamification!");

    }

    //***********************************************************************//
    //***  HOOK PROCESSORS   ************************************************//
    //***********************************************************************//

    /**
     * Processa um evento de Pull Request
     * @param  payload Payload enviado pelo gamification
     * @return         Mensagem que o bot deverá enviar ao canal
     */
    protected String processKingOfTheDay(JSONObject payload) {

        String name = payload.optString("name");
        String message = payload.optString("message");
        String channel = payload.optString("channel");

        if (channel == null) {
            return "You need to specify a relation between repositories" +
                    "and the respective slack channel to send the message";
        }

        if (name == null) {
            return "You need to specify a name for the new king!";
        }

        if (message == null) {
            // Caso o usuário não passe uma mensagem, monta uma genérica
            message = ":gamification: Temos um novo Rei no dia de hoje! :crown:\n";
            message += ":gamification: Long live *" + name + "*!";
            message += ":gamification: :beer::metal::crab::beer::dancers::crown::clap::beer::trollface::crown::beers::dancers::trophy::crown::beers:";
        }

        // Gera uma instância do bot e manda a mensagem
        SlackBot.getInstance().message(channel, message);

        return message;

    }
}
