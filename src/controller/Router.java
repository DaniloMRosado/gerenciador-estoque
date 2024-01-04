package controller;

import java.io.IOException;

import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import model.AuthService;
import model.User;
import model.UserRepository;
import model.UserValidator;

public class Router {

    public Router(HttpServer server) {
        server.createContext("/", new HomeRouter());
        server.createContext("/home", new HomeRouter());
        server.createContext("/login", new LoginRouter());
        server.createContext("/cadastro", new CadastroRouter());
        server.createContext("/estoque", new EstoqueRouter());
    }

    public class EstoqueRouter implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Map<String, String> parameters = RouterUtils.getParameters(exchange);
                RouterUtils.addProduct(exchange, parameters.get("produto"));
            }
        }
    }

    public class HomeRouter implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            RouterUtils.sendHomeHTML(exchange);
        }

    }

    public class LoginRouter implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {

                RouterUtils.sendLoginHTML(exchange);

            } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

                Map<String, String> parameters = RouterUtils.getParameters(exchange);
                AuthService authService = new AuthService(new UserRepository());
                boolean userAuthenticated;
                userAuthenticated = authService.authenticateUser(parameters.get("email"), parameters.get("senha"));

                if (userAuthenticated) {
                    RouterUtils.sendEstoqueHTML(exchange);
                } else {
                    RouterUtils.sendMessageLoginPage(exchange, "Credenciais Incorretas");
                }
            }
        }

    }

    public class CadastroRouter implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {

                RouterUtils.sendCadastroHTML(exchange);

            } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

                Map<String, String> parameters = RouterUtils.getParameters(exchange);

                AuthService authService = new AuthService(new UserRepository());

                boolean confirmPasswordOK = UserValidator.confirmPassword(parameters.get("senha"),
                        parameters.get("confirme-senha"));

                if (authService.isEmailAlreadyRegistered(parameters.get("email"))) {
                    RouterUtils.sendMessageSignUpPage(exchange, "Email ja registrado");
                } else if (confirmPasswordOK) {

                    authService = new AuthService(new UserRepository());
                    boolean registered;
                    registered = authService
                            .registerNewUser(new User(parameters.get("email"), parameters.get("senha")));

                    if (registered) {
                        RouterUtils.sendEstoqueHTML(exchange);
                    }

                } else {

                    RouterUtils.sendMessageSignUpPage(exchange, "Confirme a senha corretamente");

                }
            }
        }
    }

}
