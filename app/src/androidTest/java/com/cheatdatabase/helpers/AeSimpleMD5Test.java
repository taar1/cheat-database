package com.cheatdatabase.helpers;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

public class AeSimpleMD5Test {

    @Test
    public void MD5() {
        String md5 = null;
        try {
            md5 = AeSimpleMD5.MD5("test_string_here");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        assertEquals(md5, "801ba3d79bf9509c54dc82cd600b39a1");
    }
}