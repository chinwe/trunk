import java.lang.System.Logger.Level;

/*
 * 新增的System.LoggerFinder用来管理JDK使用的日志记录器实现。
 */
public class Logger {

    private static final System.Logger LOGGER = System.getLogger("Logger");

    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "Run!");
    }
}
