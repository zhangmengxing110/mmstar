package com.mqi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = { "com.mqi.address"})
public class AddressToolsApplication
{
    public static void main(String[] args) {
        SpringApplication.run(AddressToolsApplication.class, args);
    }
}
