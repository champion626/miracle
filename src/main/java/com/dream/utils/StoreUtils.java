package com.dream.utils;

import com.dream.boot.exception.BusinessException;
import com.dream.common.dto.enums.EnumCommonError;
import com.dream.common.dto.file.FileDTO;
import com.dream.common.dto.file.Storage;
import com.dream.common.dto.file.StorageException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class StoreUtils {


    private  static  final Logger logger= LoggerFactory.getLogger(StoreUtils.class);

    public enum FileSizeUnits {
        KB, MB, GB, PB
    }


    //计算文件大小
    public static BigInteger calcFileSize(long byteSize, FileSizeUnits units) {
        if (units == FileSizeUnits.KB) {
            return BigInteger.valueOf(byteSize).divide(BigInteger.valueOf(FileUtils.ONE_KB));
        } else if (units == FileSizeUnits.MB) {
            return BigInteger.valueOf(byteSize).divide(BigInteger.valueOf(FileUtils.ONE_MB));
        } else if (units == FileSizeUnits.GB) {
            return BigInteger.valueOf(byteSize).divide(BigInteger.valueOf(FileUtils.ONE_GB));
        } else if (units == FileSizeUnits.PB) {
            return BigInteger.valueOf(byteSize).divide(BigInteger.valueOf(FileUtils.ONE_GB * 1000));
        }
        return BigInteger.valueOf(-1);
    }

    public static String getFileType(MultipartFile file) {
        return file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
    }
    
    public static FileDTO save(Storage storage, MultipartFile file, String bucket) throws IOException {
        List<String> unSupportFileSuffix = Arrays.asList(new String[] {"exe", "dmg", "sh", "dat", "com", "sys"});;
        String suffix = getFileType(file);
        if (unSupportFileSuffix.contains(suffix)) throw new BusinessException(EnumCommonError.NotSupported_File_Type(suffix));
        String fileOriginName = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));
        String fileName = Hex.encodeHexString(DigestUtils.md5(file.getInputStream())) + "." + suffix;
        String fileUrl = "/files/" + bucket + "/" + Hex.encodeHexString(DigestUtils.md5(file.getInputStream())) + "." + suffix;
        try {
            storage.save(bucket, fileName, file.getInputStream(), 0, null);
        } catch (StorageException e) {
            logger.error("上传文件出错:{}",e.getMessage());
            throw new RuntimeException("can not save");
        }
        return new FileDTO(fileOriginName, suffix, fileUrl);
    }

}
