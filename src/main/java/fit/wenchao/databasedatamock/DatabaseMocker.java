package fit.wenchao.databasedatamock;


import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
import fit.wenchao.commonComponentSpringBootStarter.ApplicationContextHolder;
import fit.wenchao.databasedatamock.annotation.MockRow;
import fit.wenchao.utils.basic.BasicUtils;
import fit.wenchao.utils.properties.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fit.wenchao.databasedatamock.VariableNameConversion.humpToLowerLine;
import static fit.wenchao.utils.basic.BasicUtils.arr;
import static fit.wenchao.utils.collection.SimpleFactories.ofMap;
import static java.util.Arrays.asList;

@Component
@Slf4j
public class DatabaseMocker {

    public static DatabaseMocker getInstance() {
        DatabaseMocker bean = ApplicationContextHolder.getApplicationContext().getBean(DatabaseMocker.class);
        return bean;
    }

    public static List<Type> getGenericSuperInterfaceParamTypes(Class targetClass, Class targetSuperInterfaceType) {
        Type[] genericInterfaces = targetClass.getGenericInterfaces();
        List<Type> list = new ArrayList<>();
        AtomicBoolean flag = new AtomicBoolean(false);
        Arrays.stream(genericInterfaces).forEach((genericInterface) -> {
            String rawTypeName = null;
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType genericInterface1 = (ParameterizedType) genericInterface;
                rawTypeName = genericInterface1.getRawType().getTypeName();
            } else {
                rawTypeName = genericInterface.getTypeName();
            }


            if (targetSuperInterfaceType.getTypeName().equals(rawTypeName)) {
                flag.set(true);
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType genericInterface1 = (ParameterizedType) genericInterface;
                    list.addAll(asList(genericInterface1.getActualTypeArguments()));
                    return;
                }
                throw new RuntimeException("target superInterface is not a genericType");
            }
        });
        if (!flag.get()) {
            throw new RuntimeException("target Class " + targetClass.getTypeName() + " dont extends or implements" +
                    targetSuperInterfaceType.getTypeName() + " interface");
        }
        return list;
    }

    public <T> void mock(Class<T> poClass) throws Exception {
        MockAnnotationProcessor mockAnnotationProcessor = new MockAnnotationProcessor();
        List<T> resultRows = mockAnnotationProcessor.produceRow(poClass);
        System.out.println(JSONObject.toJSONString(resultRows));

        insertRows(poClass, resultRows);
    }

    static class DataSourceSingleton{
        DataSource dataSource;
        private static final DataSourceSingleton dataSourceSingleton;
        static {
            Properties properties = PropertiesUtils.getProperties("druid.properties");
            DataSource dataSource = null;
            try {
                dataSource = DruidDataSourceFactory.createDataSource(properties);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dataSourceSingleton =  new DataSourceSingleton(dataSource);
        }

        private DataSourceSingleton(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public static Connection getConnection() throws SQLException {
            return dataSourceSingleton.dataSource.getConnection();
        }
    }

    private void autoRollbackTransaction( Consumer<Connection> biconsumer ) throws SQLException {
        Connection connection = DataSourceSingleton.getConnection();
        connection.setAutoCommit(false);
        try {
            biconsumer.accept(connection);
            connection.commit();
        } catch (Exception e) {
            log.error("exception: " + e.getMessage() + "\n now rollback all changes.");
            if (connection != null) {
                connection.rollback();
                connection.close();
            }
        }
    }

    private <T> void insertRows(Class<T> poClass, List<T> rows) throws Exception {
        Properties properties = PropertiesUtils.getProperties("druid.properties");
        DataSource dataSource = DruidDataSourceFactory.createDataSource(properties);

        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            connection.setAutoCommit(false);

            String tableName = getTableName(poClass);

            if (ifDelAllBeforeMock(poClass)) {
                deleteAll(connection, tableName);
            }

            Map<String, Object> map = getInsertSql(poClass, tableName);

            String sql = (String) map.get("sql");

            Boolean[] dontInsert = (Boolean[]) map.get("dontInsert");

            Object[][] objects = rows.stream()
                    .map(row -> getObjectValueArray(row, dontInsert))
                    .collect(Collectors.toList())
                    .toArray(new Object[0][0]);

            QueryRunner queryRunner = new QueryRunner();
            queryRunner.insertBatch(connection, sql, new MapListHandler(), objects);

            log.info("mock completed, now commit changes.");
            connection.commit();
        } catch (Exception e) {
            log.error("exception: " + e.getMessage() + "\n now rollback all changes.");
            if (connection != null) {
                connection.rollback();
                connection.close();
            }
        }
    }

    private void deleteAll(Connection connection, String tableName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("delete from `" + tableName + "`");
        preparedStatement.execute();
    }

    public Map<String, Object> getInsertSql(Class poClass, String tableName) {
        Field[] declaredFields = poClass.getDeclaredFields();
        StringBuilder sql = new StringBuilder("insert into `");
        sql.append(tableName).append("`").append(" (");
        Boolean[] dontInsert = new Boolean[declaredFields.length];
        Arrays.fill(dontInsert, false);
        AtomicInteger count = new AtomicInteger();
        Arrays.stream(declaredFields).forEach((field -> {
            String name = field.getName();
            if (!"serialVersionUID".equals(name)) {
                String s = humpToLowerLine(name);
                sql.append("`" + s + "`").append(",");
            } else {
                dontInsert[count.get()] = true;
            }
            count.getAndIncrement();
        }));
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") ");
        sql.append("values (");

        int dontInsertCount = (int) Arrays.stream(dontInsert).filter((item) -> item).count();

        for (int i = 0; i < declaredFields.length - dontInsertCount; i++) {
            sql.append("?,");
        }
        sql.deleteCharAt(sql.length() - 1);

        sql.append(");");


        boolean matches = Pattern.matches("^ *insert +into +`.+` *\\( *(`.+`, *)*`.+` * *\\) * values *\\((.+, *)* *.+ *\\) *; *$", sql.toString());

        if (!matches) {
            throw new IllegalArgumentException("sql: " + sql.toString() + "is not a valid insert statement");
        }
        return ofMap(
                "sql", sql.toString(),
                "dontInsert", dontInsert
        );

    }

    public static void main(String[] args) {
        String test = "  insert into `goods_eva`(`id`, `goods_id`, `consumer_id`, `star`, `comment`, `comment_time`, `order_id`) VALUES (1,2,3,4,'[value-5]','[value-6]',6);";

        boolean matches = Pattern.matches("^ *insert +into +`.+` *\\( *(`.+`, *)*`.+` * *\\) * values *\\((.+, *)* *.+ *\\) *; *$", test);

        System.out.println(matches);
    }

    private Object[] getObjectValueArray(Object obj, Boolean[] dontInsert) {
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        List<Object> list = new ArrayList<>();
        try {
            BasicUtils.hloop(arr(declaredFields), (idx, field, state) -> {
                if (!dontInsert[idx]) {
                    list.add(getFieldValue(field, obj));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.toArray(new Object[0]);
    }

    private Object getFieldValue(Field field, Object obj) {
        field.setAccessible(true);
        Object o = null;
        try {
            o = field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }

    private <T> String getTableName(Class<T> poClass) {
        TableName annotation = poClass.getAnnotation(TableName.class);
        if (annotation != null && annotation.value() != "") {
            return annotation.value();
        }
        throw new IllegalArgumentException("Need to know the table name," +
                " use mybatis @TableName to specify tablename");
    }

    private <T> boolean ifDelAllBeforeMock(Class<T> poClass) {
        MockRow annotation = poClass.getAnnotation(MockRow.class);
        return annotation.delBeforeMock();
    }

    /**
     * ResultSetHandler 的实现
     * 将 ResultSet 转换为 List<Map<String, Object>>
     * 这个类是线程安全的
     */
    static class MapListHandler extends AbstractListHandler<Map<String, Object>> {

        /**
         * RowProcessor 实现类，将 ResultSet 的一条记录转换为 Map<String, Object>
         */
        private final RowProcessor convert;

        /**
         * 创建一个新的 MapListHandler 实例
         * 使用 BasicRowProcessor 作为转换器
         */
        public MapListHandler() {
            this(new BasicRowProcessor());
        }

        /**
         * 创建一个新的 ArrayListHandler 实例
         * 可以自定义 RowProcessor 实现类
         */
        public MapListHandler(RowProcessor convert) {
            super();
            this.convert = convert;
        }

        /**
         * 将 ResultSet 中的一行数据转换为 Map<String, Object>
         */
        @Override
        protected Map<String, Object> handleRow(ResultSet rs) throws SQLException {
            return this.convert.toMap(rs);
        }

    }


}
