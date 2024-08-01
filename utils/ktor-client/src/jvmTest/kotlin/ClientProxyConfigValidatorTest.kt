package me.him188.ani.utils.ktor

import io.ktor.util.network.address
import io.ktor.util.network.port
import java.net.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals


class ClientProxyConfigValidatorTest {

    @Test
    fun `parse http proxy`() {
        ClientProxyConfigValidator.parseProxy("http://127.0.0.1").run {
            assertEquals(Proxy.Type.HTTP, type())
            assertEquals("127.0.0.1", address().address)
            assertEquals(80, address().port)
        }
    }

    @Test
    fun `parse http proxy with port`() {
        ClientProxyConfigValidator.parseProxy("http://127.0.0.1:88").run {
            assertEquals(Proxy.Type.HTTP, type())
            assertEquals("127.0.0.1", address().address)
            assertEquals(88, address().port)
        }
    }

    @Test
    fun `parse socks proxy`() {
        ClientProxyConfigValidator.parseProxy("socks://127.0.0.1").run {
            assertEquals(Proxy.Type.SOCKS, type())
            assertEquals("127.0.0.1", address().address)
            assertEquals(1080, address().port)
        }
    }

    @Test
    fun `parse socks proxy with port`() {
        ClientProxyConfigValidator.parseProxy("socks://127.0.0.1:88").run {
            assertEquals(Proxy.Type.SOCKS, type())
            assertEquals("127.0.0.1", address().address)
            assertEquals(88, address().port)
        }
    }

    @Test
    fun `parse socks5 proxy`() {
        ClientProxyConfigValidator.parseProxy("socks5://127.0.0.1:88").run {
            assertEquals(Proxy.Type.SOCKS, type())
            assertEquals("127.0.0.1", address().address)
            assertEquals(88, address().port)
        }
    }
}