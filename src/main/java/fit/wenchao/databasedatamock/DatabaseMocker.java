package fit.wenchao.databasedatamock;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import fit.wenchao.commonComponentSpringBootStarter.ApplicationContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;

@Component
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

    public <T> void mock(Class<? extends IService<T>> daoClass) throws InstantiationException, IllegalAccessException {
        MockAnnotationProcessor mockAnnotationProcessor = new MockAnnotationProcessor();
        List<Type> genericSuperInterfaceParamTypes = getGenericSuperInterfaceParamTypes(daoClass, IService.class);
        Class<T> poClass = (Class<T>) genericSuperInterfaceParamTypes.get(0);
        List<T> resultRows = mockAnnotationProcessor.produceRow(poClass);
        System.out.println(JSONObject.toJSONString(resultRows));
        IService<T> dao = ApplicationContextHolder.getApplicationContext().getBean(daoClass);
        mockAnnotationProcessor.insertRows(dao, resultRows);
    }

}
