package net.townyreborn.transfer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

final class TransferPayload {
    private TransferPayload() {
    }

    static byte[] connect(String serverName) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeUTF("Connect");
                output.writeUTF(serverName);
            }
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not create the Velocity transfer payload", exception);
        }
    }
}
