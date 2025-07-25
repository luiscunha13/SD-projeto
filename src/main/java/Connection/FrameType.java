package Connection;

public enum FrameType {
    Login,
    Register,
    Get,
    Put,
    MultiGet,
    MultiPut,
    GetWhen,
    Close;

    public byte toByte() {
        return (byte) ordinal();
    }

    public static FrameType fromByte(byte type) {
        return switch (type) {
            case 0 -> Login;
            case 1 -> Register;
            case 2 -> Get;
            case 3 -> Put;
            case 4 -> MultiGet;
            case 5 -> MultiPut;
            case 6 -> GetWhen;
            case 7 -> Close;
            default -> null;
        };
    }
}
