package org.abondar.experimental.delivery.api.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CertificateUtil {

    public static String publicKey() throws IOException {
        return read("public_key.pem");
    }

    public static String privateKey() throws IOException {
        return read("private_key.pem");
    }


    private static String read(String filename) throws IOException {
        var file = new File(filename);

        if (file.exists()) {
            return String.join("\n", Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
        }

        return "";
    }


}
