package net.townyreborn.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import org.junit.jupiter.api.Test;

class TransferPayloadTest {
    @Test
    void createsBungeeConnectPayloadForConfiguredServer() throws Exception {
        byte[] payload = TransferPayload.connect("TownyReborn");

        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload))) {
            assertEquals("Connect", input.readUTF());
            assertEquals("TownyReborn", input.readUTF());
            assertEquals(0, input.available());
        }
    }
}
