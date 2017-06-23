package com.ul.ts.products.mdlholder.cardsim;

import com.ul.ts.products.mdlholder.utils.BitUtils;
import com.ul.ts.products.mdlholder.utils.Bytes;
import com.ul.ts.products.mdlholder.utils.HexStrings;

import java.util.Arrays;
import java.util.Map;

abstract class BasicCard implements APDUInterface {
    protected static final byte[] FAIL_INCORRECT_DATA = new byte[]{(byte)0x6A, (byte)0x80};
    protected static final byte[] FAIL_FUNCTION_NOT_SUPPORTED = new byte[]{(byte)0x6A, (byte)0x81};
    protected static final byte[] FAIL_FILE_NOT_FOUND = new byte[]{(byte)0x6A, (byte)0x82};
    protected static final byte[] FAIL_INCORRECT_P1_P2 = new byte[]{(byte)0x6A, (byte)0x86};
    protected static final byte[] FAIL_SECURITY = new byte[]{(byte)0x69, (byte)0x82};
    protected static final byte[] SUCCESS = new byte[]{(byte)0x90, 0x00};

    // File Management commands
    private static final byte[] SELECT_FILE = new byte[]{0x00, (byte)0xA4};
    private static final byte[] READ_BINARY = new byte[]{0x00, (byte)0xB0};

    // Communication preferences
    protected int maxDataLength;
    // Internal state
    private byte currentFile;
    protected Map<Byte, byte[]> fileContent;

    BasicCard(int maxDataLength) {
        this.maxDataLength = maxDataLength;
    }

    abstract protected byte[] getAID();

    @Override
    public byte[] send(byte[] command) {
        if (command.length < 4) {
            return FAIL_INCORRECT_P1_P2;
        }

        byte[] header = new byte[]{command[0], command[1]};

        byte[] response = processCommand(header, command);

        if (response == null) {
            return FAIL_FUNCTION_NOT_SUPPORTED;
        };

        return response;
    }

    protected byte[] processCommand(final byte[] header, final byte[] command) {
        if (Arrays.equals(header, SELECT_FILE)) {
            return processSelectFile(command);
        }
        if (Arrays.equals(header, READ_BINARY)) {
            return processReadBinaryCommand(command);
        }
        return null;
    }

    protected byte[] processSelectFile(final byte[] command) {
        byte cla = command[0];
        byte ins = command[1];
        byte p1 = command[2];
        byte p2 = command[3];
        byte length = command[4];
        byte[] aid = Arrays.copyOfRange(command, 5, 5+length);

        if (Arrays.equals(aid, getAID())) {
            return SUCCESS;
        } else {
            return FAIL_FILE_NOT_FOUND;
        }
    }

    abstract boolean mayReadFile(byte currentFile);

    protected byte[] processReadBinaryCommand(byte[] command) {
        byte p1 = command[2];
        byte p2 = command[3];
        byte p3 = command[4];

        int offset = Bytes.bytesToInt(p1, p2);

        int length = p3 == 0x00 ? maxDataLength : p3;

        int SPIMode = BitUtils.extractBitField(Bytes.toInt(p1), 8, 6);

        if (SPIMode == 0b00000100) {
            currentFile = (byte)BitUtils.extractBitField(Bytes.toInt(p1), 5, 1);
            offset = Bytes.toInt(p2);
        }

        if (!mayReadFile(currentFile)) {
            return FAIL_SECURITY;
        }

        if (! fileContent.containsKey(currentFile)) {
            return FAIL_FILE_NOT_FOUND;
        }

        if (fileContent.get(currentFile).length > offset) {
            return Bytes.concatenate(
                    Bytes.sub(fileContent.get(currentFile), offset, length),
                    SUCCESS
            );
        } else {
            return FAIL_INCORRECT_P1_P2;
        }
    }

    public void setMaxDataLength(final int max_data_length) {
        this.maxDataLength = max_data_length;
    }
}
