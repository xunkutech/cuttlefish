package com.xunkutech.base.model;

import com.xunkutech.base.model.enums.EntityStatus;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;

public interface IPayloadEntity extends IBaseEntity {

    String getJsonText();

    void setJsonText(String jsonText);


    @SuppressWarnings("unchecked")
    static <EP extends IPayloadEntity>
    EP newEntity(Class<?> clz, String id) {

        EP payloadEntity = null;

        try {
            payloadEntity = (EP) clz.getConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        payloadEntity.setPrimaryCode(id);

        payloadEntity.setCreatedDate(Instant.now());
        payloadEntity.setEntityStatus(EntityStatus.ACTIVE);
        payloadEntity.setEnable(true);

        return payloadEntity;
    }
}
