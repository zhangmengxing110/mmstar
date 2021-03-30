package com.mqi;

import static org.junit.Assert.assertTrue;

import com.mqi.address.addressutil.AddressParseUtils;
import com.mqi.address.addressutil.ParseResult;
import com.mqi.address.jsonutil.JacksonUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class AppTest 
{

    @After
    public void after(){
        System.out.println("after()");
    }
    /**
     * Rigorous Test :-)
     */
    @Test

    public void shouldAnswerWithTrue() throws IOException {
        ParseResult address1 = AddressParseUtils.parseAddress("张彤，13311111111，黑龙江省 大兴安岭地区 加格达奇区 铁路南小区29号楼4单元5658sf");
        System.out.println(JacksonUtil.BeanToJson(address1));

        ParseResult address2 = AddressParseUtils.parseAddress("北京 北京市 顺义区 胜利街道宜宾南区2-2-401 李俊南 18210997754");
        System.out.println(JacksonUtil.BeanToJson(address2));
    }
}
