package com.caffeinatedrat.SimpleWebSockets;

/**
 * 
 * @author CaffeinatedRat
 * 
 */
public interface IApplicationLayer {

    void onTextFrame(String text, TextResponse response);
    void onBinaryFrame(byte[] data, BinaryResponse response);
    void onClose();
    void onPing(byte[] data);
    void onPong();
    
}
