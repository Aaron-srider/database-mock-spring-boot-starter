package fit.wenchao.databasedatamock;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import fit.wenchao.databasedatamock.annotation.*;
import fit.wenchao.databasedatamock.constant.AppendEnum;
import fit.wenchao.databasedatamock.constant.MockModeEnum;
import fit.wenchao.databasedatamock.testPo.dao.po.GoodsPubApplicationPO;

import java.lang.reflect.Field;
import java.util.*;

import static fit.wenchao.databasedatamock.NanoIdUtils.randomNanoId;

public class MockAnnotationProcessor {

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        List<GoodsPubApplicationPO> goodsPubApplicationPOS = new MockAnnotationProcessor().produceRow(GoodsPubApplicationPO.class);

        String s = JSONObject.toJSONString(goodsPubApplicationPOS);
        System.out.println(s);
    }

    public <T> void insertRows(IService<T> dao, List<T> mockRows) {
        dao.saveBatch(mockRows);
    }

    private <T> MockRow checkMockRowAnnoExistsAndField(Class<T> tClass) {
        MockRow mockRowAnno = tClass.getAnnotation(MockRow.class);
        if (mockRowAnno == null) {
            throw new IllegalArgumentException("you should indicate mock row" +
                    " count with annotation MockRow");
        }
        int num = mockRowAnno.num();
        if (num <= 0) {
            throw new IllegalArgumentException("Mock row must > 0");
        }
        return mockRowAnno;
    }

    private <T> T mockOneRow(Class<T> tClass, Map<Field, BaseInfo> baseMap) throws InstantiationException, IllegalAccessException {
        T newRow = tClass.newInstance();
        Field[] declaredFields = tClass.getDeclaredFields();
        //mock each field
        Arrays.stream(declaredFields).forEach((item) -> {
            scanAndMockEachField(item, baseMap, newRow);
        });
        return newRow;
    }


    private void processBaseMode(Class fieldType, Field targetField,
                                 Map<Field, BaseInfo> baseMap,
                                 Object targetObj) {
        MockString mockStringAnno = null;
        if (!fieldType.equals(String.class) || (mockStringAnno = targetField.getAnnotation(MockString.class)) == null) {
            throw new IllegalArgumentException("MockField mode 'BASE'," +
                    " only support String.class and should be used along " +
                    "with MockString");
        }



        String base;
        AppendEnum appendStrategy = AppendEnum.NONE;
        int appendLen;
        if ((baseMap.get(targetField) == null)) {
            base = mockStringAnno.base();
            appendStrategy = mockStringAnno.append();
            appendLen = mockStringAnno.appendLen();

            BaseInfo baseInfo = new BaseInfo(base, appendStrategy, appendLen);
            baseMap.put(targetField, baseInfo);
        }

        BaseInfo baseInfo = baseMap.get(targetField);
        base = baseInfo.getBase();
        appendLen = baseInfo.getAppendLen();
        appendStrategy = baseInfo.getAppendStrategy();
        if (appendStrategy.equals(AppendEnum.NANOID)) {
            setFieldValue(targetField,targetObj, base + randomNanoId(appendLen) );
        } else {
            throw new IllegalArgumentException("Append only support AppendEnum.NANOID" +
                    " for now");
        }
    }

    private void processFixedMode(Class fieldType,
                                  Field targetField,
                                  Object targetObj) {
        if (fieldType.equals(Integer.class)) {
            MockInt annotation2 = targetField.getAnnotation(MockInt.class);
            if (annotation2 == null) {
                throw new IllegalArgumentException("Integer " +
                        "field should be annotated with MockInt");
            }

            int value = annotation2.value();
            setFieldValue(targetField,targetObj, value );
        } else if (fieldType.equals(Boolean.class)) {
            MockBoolean annotation2 = targetField.getAnnotation(MockBoolean.class);
            if (annotation2 == null) {
                throw new IllegalArgumentException("Boolean " +
                        "field should be annotated with MockBoolean");
            }
            boolean value = annotation2.value();
            setFieldValue(targetField,targetObj, value );

        } else {
            throw new IllegalArgumentException("Fixed Mode only" +
                    " support Integer and Boolean field");
        }
    }

    private int mockRangeInt(MockInt mockIntAnno){
        int i1 = mockIntAnno.randomMin();
        int i2 = mockIntAnno.randomMax();
        if (i1 > i2) {
            throw new IllegalArgumentException("randomMax should" +
                    " be bigger than randomMin");
        }
        int newRandom = (new Random().nextInt(i2)) + i1;
        return newRandom;
    }
    private boolean mockRangeBoolean(MockBoolean mockBoolean){
        int i1 = new Random().nextInt(2);
        boolean newBoolean = (i1 != 0);

        return newBoolean;
    }


    private static void setFieldValue(Field targetField, Object obj, Object value){
        try {
            targetField.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }



    private void processRangeModeField(
                                       Field targetField,
                                       Object targetObj) {
        Class fieldType = targetField.getType();
        if (fieldType.equals(Integer.class)) {
            MockInt mockIntAnno = targetField.getAnnotation(MockInt.class);
            if (mockIntAnno == null) {
                throw new IllegalArgumentException("Integer field with " +
                        "Range mode should be annotated with MockInt");
            }
            int newRandom = mockRangeInt(mockIntAnno);
            setFieldValue(targetField, targetObj, newRandom );
        } else if (fieldType.equals(Boolean.class)) {
            MockBoolean mockBoolean = targetField.getAnnotation(MockBoolean.class);
            if (mockBoolean == null) {
                throw new IllegalArgumentException("Boolean field with " +
                        "Range mode should be annotated with MockBoolean");
            }
            boolean newBoolean = mockRangeBoolean(mockBoolean);
            setFieldValue(targetField,targetObj, newBoolean );
        } else {
            throw new IllegalArgumentException("Range Mode only" +
                    " support Integer and Boolean field");
        }
    }


    private <T> void scanAndMockEachField(Field item,
                                          Map<Field, BaseInfo> baseMap,
                                          T newRow) {
        item.setAccessible(true);
        MockField annotation1 = item.getAnnotation(MockField.class);
        if (annotation1 != null) {
            MockModeEnum mode = annotation1.mode();
            Class<?> fieldType = item.getType();

            //base mode
            if (mode.equals(MockModeEnum.BASE)) {
                processBaseMode(fieldType, item, baseMap, newRow);
            }

            //fixed mode
            if (mode.equals(MockModeEnum.FIXED)) {
                processFixedMode(fieldType, item, newRow);
            }

            //range mode
            if (mode.equals(MockModeEnum.RANGE)) {
                processRangeModeField(item, newRow);
            }
        }
    }

    public <T> List<T> produceRow(Class<T> tClass) throws InstantiationException, IllegalAccessException {
        List<T> list = new ArrayList<>();
        MockRow mockRowAnno = checkMockRowAnnoExistsAndField(tClass);
        int num = mockRowAnno.num();
        Map<Field, BaseInfo> baseMap = new HashMap<>();
        //mock num obj
        for (int i = 0; i < num; i++) {
            T newRow = mockOneRow(tClass, baseMap);
            list.add(newRow);
        }
        return list;
    }

}
