package dev.kozhevnikov.autotests.utils.db;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Класс для работы с базами данных.
 */
@Component
@Slf4j
@Lazy
public class DbUtils {
    /**
     * Выполняет обновление в базе данных с использованием массива с типами параметров.
     *
     * @param sql          строка SQL запроса
     * @param typeInt      массив с типами параметров
     * @param params       список параметров
     * @param jdbcTemplate объект JdbcTemplate для выполнения операций с базой данных
     * @return количество обновленных записей в базе данных
     */
    @SneakyThrows
    public int update(String sql, int[] typeInt, List<String> params, JdbcTemplate jdbcTemplate) {
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreatorFactory(sql, typeInt)
                .newPreparedStatementCreator(params);
        return jdbcTemplate.update(preparedStatementCreator);
    }

    /**
     * Выполняет обновление в базе данных с использованием предоставленного строкового SQL запроса и объекта JdbcTemplate.
     *
     * @param sql          строковый SQL запрос для выполнения обновления
     * @param jdbcTemplate объект JdbcTemplate для выполнения операций с базой данных
     * @return количество обновленных записей в базе данных
     */
    public int update(String sql, JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.update(sql);
    }

    /**
     * Выполняет запрос к базе данных с использованием метода {@link JdbcTemplate#queryForList}
     *
     * @param sql          строковый SQL запрос для выполнения запроса к базе данных
     * @param jdbcTemplate объект JdbcTemplate для выполнения операций с базой данных
     * @return список Map, где ключи являются строками, а значения - объектами, представляющий результат запроса к базе данных
     */
    public List<Map<String, Object>> get(String sql, JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForList(sql);
    }
}