package fit.wenchao.databasedatamock;

import fit.wenchao.databasedatamock.annotation.MockField;
import fit.wenchao.databasedatamock.annotation.MockRow;
import fit.wenchao.databasedatamock.customMode.WithMockMode;
import fit.wenchao.databasedatamock.mockMode.*;
import fit.wenchao.utils.basic.BasicUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fit.wenchao.utils.string.StrUtils.ft;
import static java.util.Arrays.asList;

public class MockAnnotationProcessor {

    static List<MockMode> mockModeList = new ArrayList<>();

    private <T> MockRow checkMockRowAnnoExistsAndField(Class<T> tClass) {
        MockRow mockRowAnno = tClass.getAnnotation(MockRow.class);
        if (mockRowAnno == null) {
            throw new IllegalArgumentException("you should indicate mock row" +
                    " count with annotation MockRow");
        }
        int num = mockRowAnno.value();
        if (num <= 0) {
            throw new IllegalArgumentException("Mock row must > 0");
        }
        return mockRowAnno;
    }

    private <T> T mockOneRow(Class<T> tClass, int num) throws InstantiationException, IllegalAccessException {
        T newRow = tClass.newInstance();
        Field[] declaredFields = tClass.getDeclaredFields();
        //mock each field
        Arrays.stream(declaredFields).forEach((targetField) -> {
            scanAndMockEachField(targetField, newRow, num);
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
                                          T newRow, int num) {
        List<MockMode> mockModeList = asList(
                new BaseMockMode(),
                new RangeMockMode(),
                new FixedMockMode(),
                new StepMockMode());

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

        Annotation[] annotations = targetField.getAnnotations();
        try {
            BasicUtils.hloop(BasicUtils.arr(annotations), (idx, annotation, state) -> {
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

    }

    public <T> List<T> produceRow(Class<T> tClass) throws InstantiationException, IllegalAccessException {
        List<T> list = new ArrayList<>();
        MockRow mockRowAnno = checkMockRowAnnoExistsAndField(tClass);
        int num = mockRowAnno.value();
        //mock num obj
        for (int i = 0; i < num; i++) {
            T newRow = mockOneRow(tClass, num);
            list.add(newRow);
        }
        return list;
    }
}
