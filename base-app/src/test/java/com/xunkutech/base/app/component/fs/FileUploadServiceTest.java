//package com.xunkutech.base.app.component.fs;
//
//import com.xunkutech.base.Application4Test;
//import org.junit.Before;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.MethodSorters;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.security.SecureRandom;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = Application4Test.class)
//@FixMethodOrder(MethodSorters.JVM)
//@ActiveProfiles("api")
//public class FileUploadServiceTest {
//
//    @Autowired
//    FileUploadService fileUploadService;
//
//    File randomFile;
//
//    @Before
//    public void setUp() throws Exception {
//        randomFile = File.createTempFile("test", null, null);
//        randomFile.deleteOnExit();
//        FileOutputStream os = new FileOutputStream(randomFile);
//
//        byte[] buf = new byte[1 << 16];
//        SecureRandom secureRandom = new SecureRandom();
//
//        for (int i = 0; i < 64; i++) {
//            secureRandom.nextBytes(buf);
//            os.write(buf);
//        }
//        os.close();
//    }
//
//
//    @Test
//    public void saveFile() throws Exception {
//        fileUploadService.saveFile(new FileInputStream(randomFile), "namespace1", "/a/b/c/xx.dat", null);
//    }
//
//    @Test(expected = com.xunkutech.base.app.exception.AppException.class)
//    public void fileDuplicated() throws Exception {
//        fileUploadService.saveFile(new FileInputStream(randomFile), "namespace1", "/a/b/c/xx.dat", null);
//        fileUploadService.saveFile(new FileInputStream(randomFile), "namespace1", "/a/b/c/xx.dat", null);
//    }
//
//}