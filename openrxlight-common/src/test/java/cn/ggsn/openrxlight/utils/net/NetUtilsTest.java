package cn.ggsn.openrxlight.utils.net;

import org.junit.jupiter.api.Test;

public class NetUtilsTest {
    @Test
    void testGetHostname() {
        System.out.println(NetUtils.getHostname());
    }

    @Test
    void testGetHostAddress() {
        System.out.println(NetUtils.getHostAddress());
    }
}
