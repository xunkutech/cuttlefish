package com.xunkutech.base.model;

import com.xunkutech.base.model.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;

public class IModelEntityTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testResolve() throws Exception {
        SampleEntityBase<SampleModel<SamplePayload>, SamplePayload> entity = new SampleEntityBase<>();
        Type type = entity.resolveModelType();


        JsonUtils.fromJson("{}", SampleEntity.class);
        entity.printJson();
        return;
    }

}


class SampleModel<T> extends AbstractModelBean<T> {
}

class SamplePayload {

}

@Getter
@Setter
class SampleEntity extends SampleEntityBase<SampleModel<SamplePayload>, SamplePayload> {

}

@Getter
@Setter
class SampleEntityBase<M extends AbstractModelBean<P>, P> extends AbstractModelEntity<M, P, NonPersistentPayloadEntity> {
    private NonPersistentPayloadEntity payloadEntity;
    private M model;
}