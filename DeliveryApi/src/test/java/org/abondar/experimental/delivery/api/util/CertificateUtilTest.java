package org.abondar.experimental.delivery.api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CertificateUtilTest {

    @Test
    public void privateKeyTest() throws Exception{
         var cert = CertificateUtil.privateKey();
         assertFalse(cert.isEmpty());
    }


    @Test
    public void publicKeyTest() throws Exception{
        var cert = CertificateUtil.publicKey();
        assertFalse(cert.isEmpty());
    }
}
