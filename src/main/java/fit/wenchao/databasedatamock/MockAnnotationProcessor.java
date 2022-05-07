package fit.wenchao.databasedatamock;

import com.alibaba.fastjson.JSONObject;
import fit.wenchao.databasedatamock.annotation.MockField;
import fit.wenchao.databasedatamock.annotation.MockRow;
import fit.wenchao.databasedatamock.customMode.WithMockMode;
import fit.wenchao.databasedatamock.mockMode.BaseMockMode;
import fit.wenchao.databasedatamock.mockMode.FixedMockMode;
import fit.wenchao.databasedatamock.mockMode.MockMode;
import fit.wenchao.databasedatamock.mockMode.RangeMockMode;
import fit.wenchao.databasedatamock.testPo.dao.po.GoodsPubApplicationPO;
import fit.wenchao.utils.basic.BasicUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fit.wenchao.utils.basic.BasicUtils.arr;
import static fit.wenchao.utils.string.StrUtils.ft;
import static java.util.Arrays.asList;

public class MockAnnotationProcessor {

    static List<MockMode> mockModeList = new ArrayList<>();

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        List<GoodsPubApplicationPO> goodsPubApplicationPOS = new MockAnnotationProcessor().produceRow(GoodsPubApplicationPO.class);

        String s = JSONObject.toJSONString(goodsPubApplicationPOS);
        System.out.println(s);
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

    private <T> T mockOneRow(Class<T> tClass) throws InstantiationException, IllegalAccessException {
        T newRow = tClass.newInstance();
        Field[] declaredFields = tClass.getDeclaredFields();
        //mock each field
        Arrays.stream(declaredFields).forEach((targetField) -> {
            scanAndMockEachField(targetField, newRow);
        });
        return newRow;
    }

    private static void setFieldValue(Field targetField, Object obj, Object value) {
        try {
            targetField.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    private <T> void scanAndMockEachField(Field targetField,
                                          T newRow) {
        List<MockMode> mockModeList = asList(
                new BaseMockMode(),
                new RangeMockMode(),
                new FixedMockMode());

        targetField.setAccessible(true);
        MockField mockField = targetField.getAnnotation(MockField.class);
        if (mockField != null) {
            mockModeList.stream().forEach((mockMode -> {
                if (mockMode.supports(mockField)) {
                    Object o = mockMode.mockValue(targetField);
                    setFieldValue(targetField, newRow, o);
                }
            }));
        }

        //List<CustomMockFieldAnnotationProvider> list = asList(
        //        new FixedIntValueProvider()
        //);

        Annotation[] annotations = targetField.getAnnotations();
        try {
            BasicUtils.hloop(arr(annotations), (idx, annotation, state) -> {
                WithMockMode annotation1 = annotation.annotationType().getAnnotation(WithMockMode.class);
                if (annotation1 != null) {
                    Class<? extends MockMode> clazz = annotation1.clazz();
                    MockMode mockMode = clazz.newInstance();
                    if (!mockMode.supports(annotation)) {
                        throw new RuntimeException(ft("class {} do not support anno" +
                                        "tation {}", mockMode.getClass(),
                                annotation.annotationType()));
                    }
                    Object o = mockMode.mockValue(targetField);
                    setFieldValue(targetField, newRow, o);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        //Arrays.stream(annotations).forEach((annotation -> {
        //    WithMockMode annotation1 = annotation.annotationType().getAnnotation(WithMockMode.class);
        //    if(annotation1!=null){
        //        Class<? extends MockMode> clazz = annotation1.clazz();
        //        try {
        //            MockMode mockMode = clazz.newInstance();
        //
        //        } catch (InstantiationException | IllegalAccessException e) {
        //            e.printStackTrace();
        //        }
        //
        //    }
        //}));

        //list.forEach((customMockFieldAnnotationProvider) -> {
        //    Class<? extends Annotation> customAnnotationClass =
        //            customMockFieldAnnotationProvider.getAnnotation();
        //    Annotation customAnnotation = targetField.getAnnotation(customAnnotationClass);
        //    targetField.getAnnotations();
        //    if (targetField.getAnnotation(customAnnotationClass) != null) {
        //        mockModeList.forEach((mockMode -> {
        //            if (mockMode.supports(customAnnotation)) {
        //                Object o = mockMode.mockValue(targetField);
        //                setFieldValue(targetField, newRow, o);
        //            }
        //        }));
        //    }
        //});
    }

    public <T> List<T> produceRow(Class<T> tClass) throws InstantiationException, IllegalAccessException {
        List<T> list = new ArrayList<>();
        MockRow mockRowAnno = checkMockRowAnnoExistsAndField(tClass);
        int num = mockRowAnno.num();
        //mock num obj
        for (int i = 0; i < num; i++) {
            T newRow = mockOneRow(tClass);
            list.add(newRow);
        }
        return list;
    }

}
