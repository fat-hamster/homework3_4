package Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseAuthService implements AuthService {

    private final DBAccess dbAccess;
    private static final Logger LOG = LogManager.getLogger(BaseAuthService.class);

    public BaseAuthService() {
        dbAccess = new DBAccess();
    }

    @Override
    public void start() {
        dbAccess.connect();
        LOG.info("Сервис авторизации запущен");
    }

    @Override
    public void stop() {
        dbAccess.closeConnection();
        LOG.info("Сервис авторизации остановлен");
    }

    @Override
    public String getNickByLoginAndPass(String login, String password) {
        return dbAccess.getNick(login, password);
    }

    @Override
    public boolean changeNick(String oldNick, String newNick) {
        return dbAccess.changeNick(oldNick, newNick);
    }

    @Override
    public String getError() {
        return dbAccess.getError();
    }
}
