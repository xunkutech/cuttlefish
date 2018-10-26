package com.xunkutech.base.app.component.validate;

import com.xunkutech.base.Application4Test;
import com.xunkutech.base.app.util.InvalidException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application4Test.class)
@FixMethodOrder(MethodSorters.JVM)
@ActiveProfiles("api")
public class SimpleCaptchaValidatorTest {

    @Autowired
    SimpleCaptchaValidator simpleCaptcha;

    String hash;

    String uuid;

    @Before
    public void setUp() throws Exception {
        uuid = "abc";
    }

    @Test
    public void newCode() throws Exception {
        String code = simpleCaptcha.newCode();
        assertNotNull(code);
        assertEquals(6, code.length());
    }

    @Test(expected = ExpireException.class)
    public void checkExpire() throws Exception {
        simpleCaptcha.checkExpire(Instant.now().minusSeconds(601));
    }

    @Test
    public void createHash() throws Exception {
        hash = simpleCaptcha.createHash(new AbstractValidator.SimplePayload(),
                "abcdef".getBytes(), uuid.getBytes());
    }

    @Test
    public void validateHash() throws Exception {
        createHash();

        simpleCaptcha.validateHash(null, hash, "abcdef".getBytes(), uuid.getBytes());
    }

    @Test(expected = InvalidException.class)
    public void validateHashFailed1() throws Exception {
        createHash();
        simpleCaptcha.validateHash(null, hash, "123456".getBytes(), uuid.getBytes());
    }

    @Test(expected = InvalidException.class)
    public void validateHashFailed2() throws Exception {
        createHash();
        simpleCaptcha.validateHash(null, "x" + hash + "x", "abcdef".getBytes(), uuid.getBytes());
    }

    @Test(expected = InvalidException.class)
    public void validateHashFailed3() throws Exception {
        createHash();
        simpleCaptcha.validateHash(null, hash, "abcdef".getBytes(), "bbb".getBytes());
    }

}