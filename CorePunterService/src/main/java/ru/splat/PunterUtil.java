package ru.splat;

/**
 * Created by Rustam on 12/17/2016.
 */
public class PunterUtil {

    /**
     * @param count количество параметров
     * @param sql sql запрос
     */
    public static String addSQLParametrs(int count, String sql) {
        StringBuilder s = new StringBuilder();
        int i = 1;
        while (i < count) {
            s.append("?,");
            i++;
        }
        s.append("?");
        return sql.replace("?", s.toString());
    }
}
