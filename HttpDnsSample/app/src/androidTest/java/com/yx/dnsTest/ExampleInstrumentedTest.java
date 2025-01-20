package com.yx.dnsTest;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tencent.msdk.dns.MSDKDnsResolver;
import com.tencent.msdk.dns.DnsConfig;
import com.tencent.msdk.dns.core.IpSet;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See <a href="http://d.android.com/tools/testing">testing documentation</a>.
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private MSDKDnsResolver dnsResolver;
    private static final String hostname = "www.tencent.com";
    private static final String hostnames = "www.tencent.com,www.qq.com";

    @Before
    public void setUp() {
        // 获取应用的上下文
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // 配置 DnsConfig
        DnsConfig dnsConfigBuilder = new DnsConfig.Builder()
                .dnsId("") // 填写您的dnsId
                .desHttp()
                .dnsKey("") // 填写dnsId对应的dnsKey
                .logLevel(Log.VERBOSE)
                .timeoutMills(2000)
                .build();

        // 初始化 MSDKDnsResolver
        dnsResolver = MSDKDnsResolver.getInstance();
        dnsResolver.init(appContext, dnsConfigBuilder);
    }

    @Test
    public void singleHostname() {
        /*
          HttpDns 同步解析接口，单域名解析
          首先查询缓存，若存在则返回结果，若不存在则进行同步域名解析请求
          解析完成返回最新解析结果
          返回值字符串以“;”分隔，“;”前为解析得到的 IPv4 地址（解析失败填“0”），“;”后为解析得到的 IPv6 地址（解析失败填“0”）
          返回示例：121.14.77.221;2402:4e00:1020:1404:0:9227:71a3:83d2
         */
        String ips = dnsResolver.getAddrByName(hostname);

        // 验证返回的 IP 地址不为空，您可以根据需要添加更多的断言来验证返回的 IP 地址是否符合预期
        assertNotNull("The IP address should not be null", ips);
    }

    @Test
    public void MultipleHostname() {
        /*
          HttpDns 同步解析接口，多域名同时解析
          首先查询缓存，若存在则返回结果，若不存在则进行同步域名解析请求
          解析完成返回最新解析结果
          返回值 ipSet 即解析得到的 IP 集合
          ipSet.v4Ips 为解析得到 IPv4 集合, 可能为 null
          ipSet.v6Ips 为解析得到 IPv6 集合, 可能为 null
          返回结果示例：IpSet{v4Ips=[www.baidu.com:14.215.177.39, www.baidu.com:14.215.177.38, www.youtube.com:104.244.45.246], v6Ips=[www.youtube.com.:2001::1f0:5610], ips=null}
         */
        IpSet ips = MSDKDnsResolver.getInstance().getAddrsByName(hostnames);

        String[] v4Ips = ips.v4Ips;
        String[] v6Ips = ips.v6Ips;

        // 你可以使用下边示例方式将返回结果的结构进行转换
        Map<String, List<String>> hostnameV4IpsMap = new HashMap<>();
        if (v4Ips != null) {
            for (String item : v4Ips) {
                String[] parts = item.split(":");
                String hostname = parts[0];
                String ip = parts[1];
                hostnameV4IpsMap.computeIfAbsent(hostname, k -> new ArrayList<>()).add(ip);
            }
        }

        // 验证返回的 IP 地址不为空，您可以根据需要添加更多的断言来验证返回的 IP 地址是否符合预期
        assertNotNull("The IP address should not be null", ips);
        assertNotNull("The IP address should not be null", hostnameV4IpsMap.get("www.tencent.com"));
    }

    @Test
    public void singleHostnameAsync() {
        /*
          HttpDns 异步解析接口，单域名解析
          首先查询缓存，若存在则返回结果，若不存在则进行同步域名解析请求
          解析完成返回最新解析结果
          返回值字符串以“;”分隔，“;”前为解析得到的 IPv4 地址（解析失败填“0”），“;”后为解析得到的 IPv6 地址（解析失败填“0”）
          返回示例：121.14.77.221;2402:4e00:1020:1404:0:9227:71a3:83d2
         */
        MSDKDnsResolver.getInstance().getAddrByNameAsync(hostname, String.valueOf(System.currentTimeMillis()));
        // 异步回调
        MSDKDnsResolver.getInstance().setHttpDnsResponseObserver((tag, domain, ipResultSemicolonSep) -> {
            assertNotNull("The IP address should not be null", ipResultSemicolonSep);
        });
    }

    @Test
    public void MultipleHostnameAsync() {
        /*
          HttpDns 异步解析接口，批量域名解析
          首先查询缓存，若存在则返回结果，若不存在则进行同步域名解析请求
          解析完成返回最新解析结果
          返回结果示例：IpSet{v4Ips=[www.baidu.com:14.215.177.39, www.baidu.com:14.215.177.38, www.youtube.com:104.244.45.246], v6Ips=[www.youtube.com.:2001::1f0:5610], ips=null}
         */
        MSDKDnsResolver.getInstance().getAddrsByNameAsync(hostnames, String.valueOf(System.currentTimeMillis()));
        // 异步回调
        MSDKDnsResolver.getInstance().setHttpDnsResponseObserver((tag, domain, ipResultSemicolonSep) -> {
            IpSet ips = (IpSet) ipResultSemicolonSep;
            String[] v4Ips = ips.v4Ips;
            String[] v6Ips = ips.v6Ips;

            Map<String, List<String>> hostnameV4IpsMap = new HashMap<>();
            if (v4Ips != null) {
                for (String item : v4Ips) {
                    String[] parts = item.split(":");
                    String hostname = parts[0];
                    String ip = parts[1];
                    hostnameV4IpsMap.computeIfAbsent(hostname, k -> new ArrayList<>()).add(ip);
                }
            }

            assertNotNull("The IP address should not be null", ips);
            assertNotNull("The IP address should not be null", hostnameV4IpsMap.get("www.tencent.com"));
        });
    }

    @Test
    public void HostCache() {
        // 首次解析
        MSDKDnsResolver.getInstance().getAddrByName(hostname);

        // 二次解析
        long start_time = System.currentTimeMillis();
        MSDKDnsResolver.getInstance().getAddrByName(hostname);
        long secondElapse = System.currentTimeMillis() - start_time;

        // 已解析过的域名，域名ttl缓存有效期内，再次解析该域名，实际不会产生请求，二次解析时延小于10ms
        assertTrue("Second DNS There is cache time consumption < 10ms",  secondElapse < 10);
    }

    @Test
    public void clearHostCache() {
        /*
         * 清除指定域名缓存
         * @param domain 域名,多个域名用,分割（如"www.qq.com,tencent.com"）
         */
        MSDKDnsResolver.getInstance().clearHostCache(hostname);

        // 首次解析
        MSDKDnsResolver.getInstance().getAddrByName(hostname);

        //  清除所有缓存
        MSDKDnsResolver.getInstance().clearHostCache();

        // 二次解析
        long start_time = System.currentTimeMillis();
        MSDKDnsResolver.getInstance().getAddrByName(hostname);
        long secondElapse = System.currentTimeMillis() - start_time;

        // 清除缓存后，下一次请求该域名，会实际发起解析请求，解析时延 > 50ms
        assertTrue("Second DNS There is cache time consumption < 10ms",  secondElapse > 50);
    }
}