package com.xunkutech.base.app.component.fs;

import com.qiniu.util.Auth;
import com.xunkutech.base.app.component.ComponentException;
import com.xunkutech.base.dao.component.fs.FileMetaEntityRepository;
import com.xunkutech.base.dao.exception.DataDuplicatedException;
import com.xunkutech.base.model.component.fs.FileMeta;
import com.xunkutech.base.model.util.CodecUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileUploadService implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${qiniu.ak:p50zXjV65uIa_X_1QZzdZMTlHfxdYWXW3Vdrv_V8}")
    private String qiniuAK;

    @Value("${qiniu.sk:rVu4D53626pfhQ8C7Lv0mgcfPP94ouc8YTI9mbRe}")
    private String qiniuSK;

    @Value("${qiniu.bucket:test}")
    private String qiniuBucket;

    @Value("${file.tmp-dir:/tmp}")
    private String tmpDirName;

    @Value("${file.tmp-prefix:app}")
    private String tmpFilePrefix;

    @Value("${file.target-dir:/tmp}")
    private String targetDirName;

    @Autowired
    FileMetaEntityRepository fileMetaEntityRepository;

    private Auth qiniuAuth;

    private File tmpDir;

    private File targetDir;

    private Tika tika;

    public void saveFile(MultipartFile mpFile, String namespace, String canonicalPath) {
        File tmpFile = null;

        try {
            tmpFile = File.createTempFile(tmpFilePrefix, null, tmpDir);
            tmpFile.deleteOnExit();
            String etag;
            try (OutputStream os = new FileOutputStream(tmpFile);
                 InputStream is = mpFile.getInputStream()) {
                etag = QETag.calcETag(is, os);
            }
            File targetFile = new File(targetDir, etag);
            String contentType;
            try {
                contentType = tika.detect(tmpFile);    //Files.probeContentType(tmpFile.getPath());
            } catch (Exception e) {
                logger.info("Can not detect file content type");
                if (null != mpFile.getContentType() && !mpFile.getContentType().isEmpty()) {
                    contentType = mpFile.getContentType();
                } else {
                    contentType = "application/octet-stream";
                }
            }
            if (!targetFile.exists()) {
                if (!tmpFile.renameTo(targetFile)) throw new IOException();
                String symbolicLinkName = targetFile.getPath() + "." + CodecUtils.urlSafeEncode(contentType);
                Path path = FileSystems.getDefault().getPath(symbolicLinkName);
                Files.createSymbolicLink(path, FileSystems.getDefault().getPath(etag));
            }
            FileMeta fileMeta = new FileMeta();
            fileMeta.setContentType(contentType);
            fileMeta.setCanonicalPath(canonicalPath);
            fileMeta.setEtag(etag);
            fileMeta.setNamespace(namespace);
            fileMeta.setOriginName(mpFile.getOriginalFilename());
            fileMeta.setHash(FileMeta.calHash(namespace, canonicalPath));
            fileMetaEntityRepository.persist(fileMeta);
        } catch (IOException e) {
            throw ComponentException.FILE_SAVE_ERROR.fire();
        } catch (DataDuplicatedException e) {
            throw ComponentException.FILE_DUPLICATED.fire();
        } finally {
            if (null != tmpFile) {
                tmpFile.delete();
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        qiniuAuth = Auth.create(qiniuAK, qiniuSK);
        tmpDir = new File(tmpDirName);
        if (!(tmpDir.exists()
                && tmpDir.isDirectory()
                && tmpDir.canWrite()
                && tmpDir.canRead())) {
            logger.error("Temp directory is invalid: {}", tmpDirName);
        }

        targetDir = new File(targetDirName);
        if (!(targetDir.exists()
                && targetDir.isDirectory()
                && targetDir.canWrite()
                && targetDir.canRead())) {
            logger.error("Upload directory is invalid: {}", targetDirName);
        }
        tika = new Tika();
    }
}
